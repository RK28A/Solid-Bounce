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

/**
 * EnchantCracker — works out how many items you need to throw on the ground so the enchanting
 * table offers the enchantment you want.
 *
 * Usage:
 *  1. Enable the module and open an enchanting table — the seed is read off the wire.
 *  2. Enchant one item. That re-rolls the seed and locks the RNG state (the first observation
 *     leaves 65536 candidates, the second narrows it to one).
 *  3. Set Enchantment/Level/Bookshelves and the module reports "throw N items".
 *
 * Every drop you make afterwards is counted automatically, so the advice stays in sync.
 */
object ModuleEnchantCracker : Module("EnchantCracker", Category.MISC) {

    /** The EnchantmentMenu syncs its seed as data slot 3 (0-2 are the three level costs). */
    private const val SEED_DATA_SLOT = 3

    private val enchantment by text("Enchantment", "sharpness")
    private val level by int("Level", 5, 1..5)
    private val slotOption by int("Slot", 0, 0..3)
    private val bookshelves by int("Bookshelves", 15, 0..15)
    private val maxDrops by int("MaxDrops", 300, 1..5000)

    /** Possible states of the player's LCG right now. */
    private var candidates: LongArray = LongArray(0)
    private var dropsSinceSeed = 0
    private var lastSeed: Int? = null

    @Suppress("unused")
    val onPacket = handler<PacketEvent> { event ->
        when (event.origin) {
            TransferOrigin.RECEIVE -> {
                val packet = event.packet
                if (packet is ClientboundContainerSetDataPacket && packet.id == SEED_DATA_SLOT) {
                    if (mc.player?.containerMenu is EnchantmentMenu) {
                        onSeedObserved(packet.value)
                    }
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

    private fun onSeedObserved(seed: Int) {
        if (seed == lastSeed) {
            // The menu re-syncs the same seed whenever the input slot changes; ignore.
            return
        }
        lastSeed = seed

        if (candidates.isEmpty()) {
            candidates = EnchantCracker.candidatesFor(seed)
            chat("§bEnchantCracker§7: seed §f$seed§7 — ${candidates.size} RNG candidates. Enchant once more to lock it.")
        } else {
            val narrowed = EnchantCracker.narrow(candidates, dropsSinceSeed, seed)
            if (narrowed.isEmpty()) {
                candidates = EnchantCracker.candidatesFor(seed)
                chat("§bEnchantCracker§7: lost track (unexpected RNG use), restarting the crack.")
            } else {
                candidates = narrowed
                chat("§bEnchantCracker§7: RNG locked — ${candidates.size} candidate(s).")
            }
        }

        dropsSinceSeed = 0
        report()
    }

    /** Computes and prints the plan for the configured target. */
    fun report() {
        if (candidates.size != 1) {
            chat("§bEnchantCracker§7: RNG not locked yet (${candidates.size} candidates). Enchant one item.")
            return
        }
        val player = mc.player ?: return
        val stack = player.mainHandItem
        if (stack.isEmpty) {
            chat("§bEnchantCracker§7: hold the item you want to enchant.")
            return
        }

        // Roll the state forward past the drops already made since the seed was observed,
        // so the number we report is always "from now on".
        val state = EnchantCracker.stateAfterDrops(candidates[0], dropsSinceSeed)

        val wantedSlot = if (slotOption == 0) null else slotOption - 1
        val plan = EnchantCracker.findDropsFor(
            state = state,
            enchantmentId = enchantment,
            minLevel = level,
            bookshelves = bookshelves,
            stack = stack,
            slot = wantedSlot,
            maxDrops = maxDrops
        )

        if (plan == null) {
            chat("§bEnchantCracker§7: no result for §f$enchantment $level§7 within $maxDrops drops.")
            return
        }

        chat(
            "§bEnchantCracker§7: throw §f${plan.drops}§7 more item(s), then enchant → " +
                EnchantCracker.describe(plan.offer)
        )
    }

    override fun enable() {
        candidates = LongArray(0)
        dropsSinceSeed = 0
        lastSeed = null
        chat("§bEnchantCracker§7: open an enchanting table to read the seed.")
    }
}
