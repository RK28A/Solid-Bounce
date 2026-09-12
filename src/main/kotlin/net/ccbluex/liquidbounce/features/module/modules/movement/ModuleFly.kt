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
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fly — basic creative-style flight (vanilla motion), controlled with the movement keys plus
 * jump/sneak for vertical. Anticheat-friendly modes are ported later.
 */
object ModuleFly : Module("Fly", Category.MOVEMENT) {

    private val horizontalSpeed by float("Speed", 1.0f, 0.1f..5.0f)
    private val verticalSpeed by float("VerticalSpeed", 1.0f, 0.1f..5.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val motionY = when {
            mc.options.keyJump.isDown -> verticalSpeed.toDouble()
            mc.options.keyShift.isDown -> -verticalSpeed.toDouble()
            else -> 0.0
        }

        val yaw = Math.toRadians(player.yRot.toDouble())
        val forward = player.zza.toDouble()
        val strafe = player.xxa.toDouble()

        val vx = (forward * -sin(yaw) + strafe * cos(yaw)) * horizontalSpeed
        val vz = (forward * cos(yaw) + strafe * sin(yaw)) * horizontalSpeed

        player.deltaMovement = Vec3(vx, motionY, vz)
        player.fallDistance = 0f
    }

    override fun disable() {
        mc.player?.deltaMovement = Vec3.ZERO
    }
}
