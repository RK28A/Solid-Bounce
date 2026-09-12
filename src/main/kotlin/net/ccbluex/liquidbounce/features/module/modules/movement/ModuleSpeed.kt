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
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import kotlin.math.cos
import kotlin.math.sin

/**
 * Speed — basic horizontal speed boost while moving on ground.
 *
 * A simple strafe/boost implementation; timer- and bhop-based modes are ported later.
 */
object ModuleSpeed : Module("Speed", Category.MOVEMENT) {

    private val speed by float("Speed", 0.35f, 0.1f..2.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val forward = player.zza.toDouble()
        val strafe = player.xxa.toDouble()
        if (forward == 0.0 && strafe == 0.0) {
            return@handler
        }

        val yaw = Math.toRadians(player.yRot.toDouble())
        val vx = (forward * -sin(yaw) + strafe * cos(yaw)) * speed
        val vz = (forward * cos(yaw) + strafe * sin(yaw)) * speed

        val motion = player.deltaMovement
        player.setDeltaMovement(vx, motion.y, vz)
    }
}
