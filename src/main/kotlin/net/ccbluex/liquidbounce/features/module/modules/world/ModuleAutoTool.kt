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
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

/**
 * AutoTool — automatically selects the fastest hotbar tool for the block being mined.
 */
object ModuleAutoTool : Module("AutoTool", Category.WORLD) {

    private val onlyWhileMining by boolean("OnlyWhileMining", true)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val hit = mc.hitResult
        if (hit == null || hit.type != HitResult.Type.BLOCK) {
            return@handler
        }
        if (onlyWhileMining && !mc.options.keyAttack.isDown) {
            return@handler
        }

        val state = world.getBlockState((hit as BlockHitResult).blockPos)
        val inventory = player.inventory

        var bestSlot = -1
        var bestSpeed = -1.0f
        for (slot in 0..8) {
            val speed = inventory.getItem(slot).getDestroySpeed(state)
            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = slot
            }
        }

        if (bestSlot != -1 && bestSlot != inventory.selected) {
            inventory.selected = bestSlot
        }
    }
}
