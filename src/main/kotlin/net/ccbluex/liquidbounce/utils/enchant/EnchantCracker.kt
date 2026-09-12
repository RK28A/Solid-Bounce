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
 * Enchantment seed cracking, following the approach of
 * https://github.com/Earthcomputer/EnchantmentCracker (and its clientcommands successor).
 *
 * Vanilla mechanics this relies on:
 *  - A player's "xpSeed" fully determines what an enchanting table offers.
 *  - The server only tells the client `xpSeed & -16`, truncated to a short: we learn bits 4..15
 *    and must brute-force the other 20 bits (2^16 high bits x 2^4 low bits), validating each
 *    candidate by replaying vanilla's offer generation against the levels and clues on screen.
 *  - Enchanting re-rolls the seed with `player.random.nextInt()`, consuming one LCG step.
 *  - Dropping an item stack advances that same LCG by exactly 4 steps (the item entity's random
 *    motion), which is what lets a player steer the next seed by throwing items away.
 *  - Two consecutive xpSeeds therefore pin down the player's 48-bit LCG state.
 */
package net.ccbluex.liquidbounce.utils.enchant

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.EnchantmentInstance

/** A full replay of what EnchantmentMenu would show for one seed. */
class MenuSimulation(
    val costs: IntArray,
    val clueIds: IntArray,
    val clueLevels: IntArray,
    val lists: List<List<EnchantmentInstance>>
)

/** Result of a search: how many items to drop and what the chosen slot would then offer. */
data class DropPlan(
    val drops: Int,
    val predictedSeed: Int,
    val slot: Int,
    val cost: Int,
    val enchantments: List<EnchantmentInstance>
)

object EnchantCracker {

    // java.util.Random / LegacyRandomSource LCG constants.
    private const val MULTIPLIER = 0x5DEECE66DL
    private const val ADDEND = 0xBL
    private const val MASK = (1L shl 48) - 1

    /** Steps the player RNG advances per dropped item stack. */
    const val RNG_STEPS_PER_DROP = 4

    /** Bits of the xpSeed the server actually reveals (xpSeed & -16, truncated to a short). */
    private const val REPORTED_MASK = 0xFFF0

    // ------------------------------------------------------------------ LCG maths

    private fun nextState(state: Long): Long = (state * MULTIPLIER + ADDEND) and MASK

    private fun advance(state: Long, steps: Int): Long {
        var current = state
        repeat(steps) { current = nextState(current) }
        return current
    }

    /** What `Random.nextInt()` returns for a state that has already been advanced. */
    private fun outputOf(state: Long): Int = (state ushr 16).toInt()

    /** All 48-bit LCG states that could have emitted [observedSeed] from a `nextInt()` call. */
    fun playerSeedCandidates(observedSeed: Int): LongArray {
        val high = (observedSeed.toLong() and 0xFFFFFFFFL) shl 16
        return LongArray(1 shl 16) { low -> high or low.toLong() }
    }

    /** The LCG state after [drops] item drops (no enchant). */
    fun stateAfterDrops(state: Long, drops: Int): Long = advance(state, drops * RNG_STEPS_PER_DROP)

    /** The LCG state left behind after [drops] item drops followed by one enchant. */
    fun stateAfterEnchant(state: Long, drops: Int): Long = nextState(stateAfterDrops(state, drops))

    /** The xpSeed the player gets after dropping [drops] items and then enchanting once. */
    fun predictSeed(state: Long, drops: Int): Int = outputOf(stateAfterEnchant(state, drops))

    /**
     * Narrows player-seed [candidates] to those that, after [drops] drops and one enchant, would
     * emit [observedSeed] — and rolls the survivors forward so they describe the state as it is now.
     */
    fun narrowPlayerSeeds(candidates: LongArray, drops: Int, observedSeed: Int): LongArray =
        candidates.asSequence()
            .filter { predictSeed(it, drops) == observedSeed }
            .map { stateAfterEnchant(it, drops) }
            .toList()
            .toLongArray()

    // ------------------------------------------------------------------ menu replay

    /**
     * Replays EnchantmentMenu.slotsChanged exactly: three costs rolled from the bare seed, then
     * each slot's enchantment list rolled from (seed + slot), then the displayed clue picked with
     * the same RandomSource.
     */
    fun simulateMenu(xpSeed: Int, bookshelves: Int, stack: ItemStack): MenuSimulation {
        val random = RandomSource.create()
        val costs = IntArray(3)
        val clueIds = IntArray(3) { -1 }
        val clueLevels = IntArray(3) { -1 }

        random.setSeed(xpSeed.toLong())
        for (slot in 0..2) {
            var cost = EnchantmentHelper.getEnchantmentCost(random, slot, bookshelves, stack)
            if (cost < slot + 1) {
                cost = 0
            }
            costs[slot] = cost
        }

        val lists = ArrayList<List<EnchantmentInstance>>(3)
        for (slot in 0..2) {
            if (costs[slot] <= 0) {
                lists.add(emptyList())
                continue
            }

            random.setSeed((xpSeed + slot).toLong())
            val list = EnchantmentHelper.selectEnchantment(random, stack, costs[slot], false)
            lists.add(list)

            if (list.isNotEmpty()) {
                val chosen = list[random.nextInt(list.size)]
                clueIds[slot] = BuiltInRegistries.ENCHANTMENT.getId(chosen.enchantment)
                clueLevels[slot] = chosen.level
            }
        }

        return MenuSimulation(costs, clueIds, clueLevels, lists)
    }

    /** Cheap first pass: only the three level costs. */
    private fun costsMatch(xpSeed: Int, bookshelves: Int, stack: ItemStack, expected: IntArray): Boolean {
        val random = RandomSource.create()
        random.setSeed(xpSeed.toLong())
        for (slot in 0..2) {
            var cost = EnchantmentHelper.getEnchantmentCost(random, slot, bookshelves, stack)
            if (cost < slot + 1) {
                cost = 0
            }
            if (cost != expected[slot]) {
                return false
            }
        }
        return true
    }

    /**
     * Brute-forces the full 32-bit xpSeed from the 12 bits the server reveals, validating each
     * candidate against the levels and enchantment clues currently on screen.
     */
    fun crackXpSeed(
        serverReported: Int,
        costs: IntArray,
        clueIds: IntArray,
        clueLevels: IntArray,
        bookshelves: Int,
        stack: ItemStack
    ): IntArray {
        val base = serverReported and REPORTED_MASK
        val matches = ArrayList<Int>()

        for (high in 0 until (1 shl 16)) {
            val highBits = high shl 16
            for (low in 0 until 16) {
                val candidate = highBits or base or low
                if (!costsMatch(candidate, bookshelves, stack, costs)) continue

                val simulation = simulateMenu(candidate, bookshelves, stack)
                if (simulation.clueIds.contentEquals(clueIds) &&
                    simulation.clueLevels.contentEquals(clueLevels)
                ) {
                    matches.add(candidate)
                }
            }
        }
        return matches.toIntArray()
    }

    // ------------------------------------------------------------------ planning

    /**
     * Finds the smallest number of item drops that makes [slot] (or any slot when null) offer
     * [enchantmentId] at at least [minLevel].
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
            val simulation = simulateMenu(seed, bookshelves, stack)

            for (candidateSlot in 0..2) {
                if (slot != null && candidateSlot != slot) continue
                val list = simulation.lists[candidateSlot]
                if (list.isEmpty()) continue

                val match = list.any { instance ->
                    val key = BuiltInRegistries.ENCHANTMENT.getKey(instance.enchantment)
                    key != null && key.path.equals(wanted, ignoreCase = true) && instance.level >= minLevel
                }
                if (match) {
                    return DropPlan(drops, seed, candidateSlot, simulation.costs[candidateSlot], list)
                }
            }
        }
        return null
    }

    fun describe(plan: DropPlan): String {
        val names = plan.enchantments.joinToString(", ") { instance ->
            val key = BuiltInRegistries.ENCHANTMENT.getKey(instance.enchantment)
            "${key?.path ?: "?"} ${instance.level}"
        }
        return "slot ${plan.slot + 1} (${plan.cost} lvl): $names"
    }
}
