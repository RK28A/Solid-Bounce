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

/**
 * Sprint module — keeps the player sprinting automatically.
 */
object ModuleSprint : Module("Sprint", Category.MOVEMENT) {

    private val onlyForward by boolean("OnlyForward", true)
    private val allowSneaking by boolean("AllowSneaking", false)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val p = player

        // Forward movement (zza > 0 means pressing forward).
        val movingForward = if (onlyForward) p.zza > 0f else (p.zza != 0f || p.xxa != 0f)
        val hasHunger = p.foodData.foodLevel > 6f || p.abilities.mayfly
        val sneaking = p.isShiftKeyDown && !allowSneaking
        val blindOrCollided = p.isUsingItem

        if (movingForward && hasHunger && !sneaking && !blindOrCollided) {
            p.isSprinting = true
        }
    }
}
