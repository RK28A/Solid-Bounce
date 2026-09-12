/*
 * This file is part of Solid-Bounce, a Forge 1.20.1 port of LiquidBounce.
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Forge port modifications Copyright (c) 2025 Solid-Bounce contributors.
 *
 * Enchantment seed cracking / prediction.
 *
 * How it works (vanilla mechanics):
 *  - Every player carries an "xpSeed" that fully determines what an enchanting table offers.
 *  - The server SYNCS that seed to the client as data slot 3 of the EnchantmentMenu, so we can
 *    simply read it off the wire instead of guessing it.
 *  - Whenever the player enchants something, the server re-rolls the seed with
 *    `xpSeed = player.random.nextInt()`, consuming exactly one step of the player's LCG.
 *  - Dropping an item advances that same LCG by exactly 4 steps (the spawned ItemEntity's
 *    random motion), which is what lets a player "steer" the next seed by throwing items away.
 *
 * So: crack the LCG state from an observed seed, then search for the number of item drops that
 * makes the NEXT seed produce the enchantment we want.
 */
package net.ccbluex.liquidbounce.utils.enchant

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.EnchantmentInstance

/** One slot of an enchanting table offer. */
data class EnchantOffer(
    val slot: Int,
    val cost: Int,
    val enchantments: List<EnchantmentInstance>
)

/** Result of a search: how many items to drop and what it yields. */
data class DropPlan(
    val drops: Int,
    val predictedSeed: Int,
    val offer: EnchantOffer
)

object EnchantCracker {

    // java.util.Random LCG constants.
    private const val MULTIPLIER = 0x5DEECE66DL
    private const val ADDEND = 0xBL
    private const val MASK = (1L shl 48) - 1

    /** Steps the player RNG advances per dropped item stack. */
    const val RNG_STEPS_PER_DROP = 4

    private fun nextState(state: Long): Long = (state * MULTIPLIER + ADDEND) and MASK

    private fun advance(state: Long, steps: Int): Long {
        var current = state
        repeat(steps) { current = nextState(current) }
        return current
    }

    /** The int that `Random.nextInt()` returns for a state that has already been advanced. */
    private fun outputOf(state: Long): Int = (state ushr 16).toInt()

    /**
     * All LCG states that could have produced [observedSeed] from a `nextInt()` call.
     * The output pins the top 32 bits of the state, leaving 2^16 candidates.
     */
    fun candidatesFor(observedSeed: Int): LongArray {
        val high = (observedSeed.toLong() and 0xFFFFFFFFL) shl 16
        return LongArray(1 shl 16) { low -> high or low.toLong() }
    }

    /** The LCG state after [drops] item drops (no enchant). */
    fun stateAfterDrops(state: Long, drops: Int): Long =
        advance(state, drops * RNG_STEPS_PER_DROP)

    /** The LCG state left behind after [drops] item drops followed by one enchant. */
    fun stateAfterEnchant(state: Long, drops: Int): Long =
        nextState(advance(state, drops * RNG_STEPS_PER_DROP))

    /**
     * Narrows [candidates] to those that, after [drops] item drops followed by one enchant,
     * would produce [observedSeed] — and rolls the survivors forward so they describe the
     * state as it is *now*.
     */
    fun narrow(candidates: LongArray, drops: Int, observedSeed: Int): LongArray =
        candidates.asSequence()
            .filter { predictSeed(it, drops) == observedSeed }
            .map { stateAfterEnchant(it, drops) }
            .toList()
            .toLongArray()

    /** The xpSeed the player will get if they drop [drops] items and then enchant once. */
    fun predictSeed(state: Long, drops: Int): Int =
        outputOf(nextState(advance(state, drops * RNG_STEPS_PER_DROP)))

    /**
     * Reproduces exactly what EnchantmentMenu shows for a given seed, bookshelf count and item.
     */
    fun simulateOffers(xpSeed: Int, bookshelves: Int, stack: ItemStack): List<EnchantOffer> {
        val random = RandomSource.create()

        // Vanilla first rolls the three costs from the bare seed.
        random.setSeed(xpSeed.toLong())
        val costs = IntArray(3)
        for (slot in 0..2) {
            var cost = EnchantmentHelper.getEnchantmentCost(random, slot, bookshelves, stack)
            if (cost < slot + 1) {
                cost = 0
            }
            costs[slot] = cost
        }

        // Then each slot's enchantment list is rolled from (seed + slot).
        return (0..2).map { slot ->
            val cost = costs[slot]
            if (cost <= 0) {
                EnchantOffer(slot, 0, emptyList())
            } else {
                random.setSeed((xpSeed + slot).toLong())
                val list: List<EnchantmentInstance> =
                    EnchantmentHelper.selectEnchantment(random, stack, cost, false)
                EnchantOffer(slot, cost, list)
            }
        }
    }

    /**
     * Searches for the smallest number of item drops that makes [slot] offer [enchantmentId]
     * at (at least) [minLevel].
     *
     * @param slot 0-2, or null to accept any slot.
     */
    fun findDropsFor(
        state: Long,
        enchantmentId: String,
        minLevel: Int,
        bookshelves: Int,
        stack: ItemStack,
        slot: Int?,
        maxDrops: Int
    ): DropPlan? {
        val wanted = enchantmentId.lowercase().removePrefix("minecraft:")

        for (drops in 0..maxDrops) {
            val seed = predictSeed(state, drops)
            val offers = simulateOffers(seed, bookshelves, stack)

            for (offer in offers) {
                if (slot != null && offer.slot != slot) continue

                val match = offer.enchantments.any { instance ->
                    val key = BuiltInRegistries.ENCHANTMENT.getKey(instance.enchantment)
                    key != null && key.path.equals(wanted, ignoreCase = true) && instance.level >= minLevel
                }
                if (match) {
                    return DropPlan(drops, seed, offer)
                }
            }
        }
        return null
    }

    /** Human-readable rendering of an offer. */
    fun describe(offer: EnchantOffer): String {
        if (offer.enchantments.isEmpty()) {
            return "slot ${offer.slot + 1}: (empty)"
        }
        val names = offer.enchantments.joinToString(", ") { instance ->
            val key = BuiltInRegistries.ENCHANTMENT.getKey(instance.enchantment)
            "${key?.path ?: "?"} ${instance.level}"
        }
        return "slot ${offer.slot + 1} (${offer.cost} lvl): $names"
    }
}
