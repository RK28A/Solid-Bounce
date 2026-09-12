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
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.world.InteractionHand

/**
 * AntiAFK — performs periodic actions so the player is not kicked for being idle.
 */
object ModuleAntiAFK : Module("AntiAFK", Category.PLAYER) {

    private val intervalTicks by int("Interval", 40, 5..200)
    private val rotate by boolean("Rotate", true)
    private val swing by boolean("Swing", true)

    private var ticks = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (++ticks < intervalTicks) {
            return@handler
        }
        ticks = 0

        if (rotate) {
            player.yRot += 30f
        }
        if (swing) {
            player.swing(InteractionHand.MAIN_HAND)
        }
    }
}
