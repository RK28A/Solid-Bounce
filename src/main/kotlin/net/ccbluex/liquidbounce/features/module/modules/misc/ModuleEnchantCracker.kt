/*
 * This file is part of Solid-Bounce, a Forge 1.20.1 port of LiquidBounce.
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Forge port modifications Copyright (c) 2025 Solid-Bounce contributors.
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.enchant.EnchantCracker
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.world.inventory.EnchantmentMenu
import net.minecraft.world.item.ItemStack
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * EnchantCracker — works out how many item stacks you must throw on the ground so the enchanting
 * table offers the enchantment you want.
 *
 * Based on the mechanics documented by https://github.com/Earthcomputer/EnchantmentCracker :
 * the server only reveals 12 bits of the xpSeed, so the full seed is brute-forced against the
 * levels and clues on screen; two consecutive seeds then pin down the player's 48-bit LCG state,
 * and each dropped stack advances that LCG by 4 steps.
 *
 * Workflow:
 *  1. Enable it, put the item in an enchanting table — the first seed gets cracked.
 *  2. Enchant one item. The second seed locks the player RNG.
 *  3. It then prints "throw N item(s), then enchant".
 */
object ModuleEnchantCracker : Module("EnchantCracker", Category.MISC) {

    /** EnchantmentMenu syncs the (masked) seed as data slot 3; 0-2 are the three level costs. */
    private const val SEED_DATA_SLOT = 3

    private val enchantment by text("Enchantment", "sharpness")
    private val level by int("Level", 5, 1..5)
    private val slotOption by int("Slot", 0, 0..3)
    private val bookshelves by int("Bookshelves", 15, 0..15)
    private val maxDrops by int("MaxDrops", 300, 1..5000)

    // --- cracking state -------------------------------------------------------------------

    private var reportedSeed: Int? = null
    private var lastSignature: String? = null

    /** The most recent fully-cracked xpSeed. */
    private var lastXpSeed: Int? = null

    /** Possible states of the player's LCG right now (size 1 == locked). */
    private var playerSeeds: LongArray = LongArray(0)

    private var dropsSinceSeed = 0

    private val cracking = AtomicBoolean(false)
    private val messages = ConcurrentLinkedQueue<String>()

    // --- events ---------------------------------------------------------------------------

    @Suppress("unused")
    val onPacket = handler<PacketEvent> { event ->
        when (event.origin) {
            TransferOrigin.RECEIVE -> {
                val packet = event.packet
                if (packet is ClientboundContainerSetDataPacket && packet.id == SEED_DATA_SLOT) {
                    reportedSeed = packet.value
                }
            }

            TransferOrigin.SEND -> {
                val packet = event.packet
                if (packet is ServerboundPlayerActionPacket) {
                    val action = packet.action
                    if (action == ServerboundPlayerActionPacket.Action.DROP_ITEM ||
                        action == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
                    ) {
                        dropsSinceSeed++
                    }
                }
            }
        }
    }

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        while (true) {
            val message = messages.poll() ?: break
            chat(message)
        }

        val menu = mc.player?.containerMenu as? EnchantmentMenu ?: return@handler
        val reported = reportedSeed ?: return@handler
        if (menu.costs.all { it == 0 }) return@handler
        if (cracking.get()) return@handler

        val signature = buildString {
            append(reported).append('|')
            append(menu.costs.joinToString(",")).append('|')
            append(menu.enchantClue.joinToString(",")).append('|')
            append(menu.levelClue.joinToString(","))
        }
        if (signature == lastSignature) return@handler
        lastSignature = signature

        startCrack(
            reported,
            menu.costs.copyOf(),
            menu.enchantClue.copyOf(),
            menu.levelClue.copyOf(),
            menu.getSlot(0).item.copy()
        )
    }

    // --- cracking -------------------------------------------------------------------------

    private fun startCrack(reported: Int, costs: IntArray, clueIds: IntArray, clueLevels: IntArray, stack: ItemStack) {
        if (stack.isEmpty) return
        if (!cracking.compareAndSet(false, true)) return

        val shelves = bookshelves
        messages += "§bEnchantCracker§7: cracking the xpSeed (2^20 candidates)…"

        Thread {
            try {
                val seeds = EnchantCracker.crackXpSeed(reported, costs, clueIds, clueLevels, shelves, stack)
                when {
                    seeds.isEmpty() ->
                        messages += "§bEnchantCracker§7: no seed matched — check the §fBookshelves§7 setting."

                    seeds.size > 1 ->
                        messages += "§bEnchantCracker§7: ${seeds.size} seeds still possible, change the item and retry."

                    else -> onXpSeedCracked(seeds[0])
                }
            } catch (e: Throwable) {
                messages += "§bEnchantCracker§7: crack failed (${e.javaClass.simpleName})."
            } finally {
                cracking.set(false)
            }
        }.apply {
            name = "SolidBounce-EnchantCracker"
            isDaemon = true
            start()
        }
    }

    private fun onXpSeedCracked(seed: Int) {
        val previous = lastXpSeed
        if (previous == seed) return
        lastXpSeed = seed

        if (previous == null) {
            messages += "§bEnchantCracker§7: seed §f$seed§7 found. Enchant one item to lock the player RNG."
            dropsSinceSeed = 0
            return
        }

        val candidates = if (playerSeeds.isEmpty()) {
            EnchantCracker.playerSeedCandidates(previous)
        } else {
            playerSeeds
        }

        val narrowed = EnchantCracker.narrowPlayerSeeds(candidates, dropsSinceSeed, seed)
        dropsSinceSeed = 0

        if (narrowed.isEmpty()) {
            playerSeeds = LongArray(0)
            messages += "§bEnchantCracker§7: lost the RNG trail, restarting from this seed."
            return
        }

        playerSeeds = narrowed
        messages += "§bEnchantCracker§7: player RNG locked (${narrowed.size} candidate(s))."
        messages += planMessage()
    }

    // --- planning -------------------------------------------------------------------------

    /** Computes the advice line for the configured target. */
    fun planMessage(): String {
        if (playerSeeds.size != 1) {
            return "§bEnchantCracker§7: RNG not locked yet (${playerSeeds.size} candidates) — enchant one item."
        }
        val player = mc.player ?: return "§bEnchantCracker§7: no player."

        val menu = player.containerMenu as? EnchantmentMenu
        val stack = menu?.getSlot(0)?.item?.takeIf { !it.isEmpty } ?: player.mainHandItem
        if (stack.isEmpty) {
            return "§bEnchantCracker§7: put the item in the table (or hold it)."
        }

        // Roll past the drops already made so the number we print is "from now on".
        val state = EnchantCracker.stateAfterDrops(playerSeeds[0], dropsSinceSeed)
        val wantedSlot = if (slotOption == 0) null else slotOption - 1

        val plan = EnchantCracker.findDropsFor(
            state = state,
            enchantmentId = enchantment,
            minLevel = level,
            bookshelves = bookshelves,
            stack = stack,
            slot = wantedSlot,
            maxDrops = maxDrops
        ) ?: return "§bEnchantCracker§7: no result for §f$enchantment $level§7 within $maxDrops drops."

        return "§bEnchantCracker§7: throw §f${plan.drops}§7 item stack(s), then enchant → ${EnchantCracker.describe(plan)}"
    }

    fun report() = chat(planMessage())

    override fun enable() {
        reportedSeed = null
        lastSignature = null
        lastXpSeed = null
        playerSeeds = LongArray(0)
        dropsSinceSeed = 0
        messages.clear()
        chat("§bEnchantCracker§7: open an enchanting table with your item to start.")
    }
}
