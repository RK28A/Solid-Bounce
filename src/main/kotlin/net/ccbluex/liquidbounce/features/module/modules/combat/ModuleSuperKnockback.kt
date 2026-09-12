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

/**
 * SuperKnockback — increases knockback dealt by performing a "W-tap" (resetting sprint on hit).
 *
 * Works together with the Sprint module, which re-enables sprint on the next tick.
 */
object ModuleSuperKnockback : Module("SuperKnockback", Category.COMBAT) {

    @Suppress("unused")
    val onAttack = handler<AttackEvent> {
        if (player.isSprinting) {
            player.isSprinting = false
        }
    }
}
