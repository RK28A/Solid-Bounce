/*
 * This file is part of Solid-Bounce, a Forge 1.20.1 port of LiquidBounce.
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Original work Copyright (c) 2015 - 2024 CCBlueX.
 * Forge port modifications Copyright (c) 2025 Solid-Bounce contributors.
 */
package net.ccbluex.liquidbounce.utils.inventory

import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack

/**
 * Shared inventory helpers used by the auto-* modules (AutoArmor, AutoTotem, ChestStealer,
 * InventoryCleaner, AutoPot, ...).
 */
object InventoryUtil {

    /** Hotbar slots are inventory indices 0..8, the rest of the main inventory is 9..35. */
    const val HOTBAR_END = 8
    const val INVENTORY_END = 35
    const val OFFHAND_SWAP_BUTTON = 40

    /**
     * Converts a player-inventory index into the slot id used by the player's own container menu
     * (0 = crafting result, 1-4 crafting, 5-8 armor, 9-35 main inventory, 36-44 hotbar, 45 offhand).
     */
    fun toContainerSlot(inventorySlot: Int): Int =
        if (inventorySlot in 0..HOTBAR_END) inventorySlot + 36 else inventorySlot

    fun stackAt(slot: Int): ItemStack = mc.player?.inventory?.getItem(slot) ?: ItemStack.EMPTY

    fun findHotbarSlot(predicate: (ItemStack) -> Boolean): Int? =
        (0..HOTBAR_END).firstOrNull { predicate(stackAt(it)) }

    fun findInventorySlot(predicate: (ItemStack) -> Boolean): Int? =
        (0..INVENTORY_END).firstOrNull { predicate(stackAt(it)) }

    /** Picks the hotbar slot scoring highest, or null when nothing scores above [minScore]. */
    fun bestHotbarSlot(minScore: Double = Double.NEGATIVE_INFINITY, score: (ItemStack) -> Double): Int? {
        var bestSlot: Int? = null
        var best = minScore
        for (slot in 0..HOTBAR_END) {
            val value = score(stackAt(slot))
            if (value > best) {
                best = value
                bestSlot = slot
            }
        }
        return bestSlot
    }

    fun select(slot: Int) {
        val player = mc.player ?: return
        if (slot in 0..HOTBAR_END && player.inventory.selected != slot) {
            player.inventory.selected = slot
        }
    }

    /** Raw container click against the currently open menu. */
    fun click(containerSlot: Int, button: Int = 0, type: ClickType = ClickType.PICKUP) {
        val player = mc.player ?: return
        mc.gameMode?.handleInventoryMouseClick(player.containerMenu.containerId, containerSlot, button, type, player)
    }

    /** Moves an inventory slot straight into the offhand. */
    fun swapToOffhand(inventorySlot: Int) =
        click(toContainerSlot(inventorySlot), OFFHAND_SWAP_BUTTON, ClickType.SWAP)

    /** Quick-moves (shift-click) a container slot. */
    fun quickMove(containerSlot: Int) = click(containerSlot, 0, ClickType.QUICK_MOVE)

    /** Throws the whole stack of an inventory slot on the floor. */
    fun dropInventorySlot(inventorySlot: Int) =
        click(toContainerSlot(inventorySlot), 1, ClickType.THROW)
}
