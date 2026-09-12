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
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.inGame

/**
 * Spammer — sends a chat message on a fixed interval.
 */
object ModuleSpammer : Module("Spammer", Category.MISC) {

    private val delayTicks by int("Delay", 40, 1..400)
    private val message by text("Message", "Solid-Bounce")

    init {
        repeatable {
            waitTicks(delayTicks)
            if (inGame) {
                mc.connection?.sendChat(message)
            }
        }
    }
}
