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
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.events.AttackEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.SwordItem

/**
 * AutoWeapon — switches to the best melee weapon in the hotbar when attacking.
 */
object ModuleAutoWeapon : Module("AutoWeapon", Category.COMBAT) {

    @Suppress("unused")
    val onAttack = handler<AttackEvent> {
        val inventory = player.inventory
        var bestSlot = -1
        var bestScore = -1

        for (slot in 0..8) {
            val item = inventory.getItem(slot).item
            val score = when (item) {
                is SwordItem -> 3
                is AxeItem -> 2
                else -> if (inventory.getItem(slot).isEmpty) -1 else 0
            }
            if (score > bestScore) {
                bestScore = score
                bestSlot = slot
            }
        }

        if (bestSlot != -1 && bestSlot != inventory.selected) {
            inventory.selected = bestSlot
        }
    }
}
