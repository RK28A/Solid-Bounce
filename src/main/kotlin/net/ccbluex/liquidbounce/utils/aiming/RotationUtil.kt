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
package net.ccbluex.liquidbounce.utils.aiming

import net.minecraft.world.phys.Vec3
import kotlin.math.atan2
import kotlin.math.sqrt

/** A yaw/pitch pair (degrees). */
data class Rotation(val yaw: Float, val pitch: Float)

object RotationUtil {

    /**
     * Computes the yaw/pitch needed to look from [from] towards [to].
     */
    fun getRotations(from: Vec3, to: Vec3): Rotation {
        val diffX = to.x - from.x
        val diffY = to.y - from.y
        val diffZ = to.z - from.z
        val horizontal = sqrt(diffX * diffX + diffZ * diffZ)

        val yaw = (Math.toDegrees(atan2(diffZ, diffX)) - 90.0).toFloat()
        val pitch = (-Math.toDegrees(atan2(diffY, horizontal))).toFloat()
        return Rotation(yaw, pitch)
    }
}
