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

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.KeyMapping

/**
 * AutoClicker — auto-attacks at a configurable CPS while the attack button is held.
 *
 * Uses [KeyMapping.click] to queue vanilla left-clicks (no mixin required). A more precise
 * randomized/jitter CPS mode is planned for a later step.
 */
object ModuleAutoClicker : Module("AutoClicker", Category.COMBAT) {

    private val minCps by int("MinCPS", 8, 1..20)
    private val maxCps by int("MaxCPS", 12, 1..20)
    private val requireWeapon by boolean("RequireWeapon", false)

    private var nextClickTime = 0L

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (mc.screen != null) return@handler

        val attackKey = mc.options.keyAttack
        if (!attackKey.isDown) {
            return@handler
        }

        if (requireWeapon && player.mainHandItem.isEmpty) {
            return@handler
        }

        val now = System.currentTimeMillis()
        if (now < nextClickTime) {
            return@handler
        }

        KeyMapping.click(attackKey.key)

        val cps = if (maxCps <= minCps) minCps else (minCps..maxCps).random()
        nextClickTime = now + (1000L / cps.coerceAtLeast(1))
    }
}
