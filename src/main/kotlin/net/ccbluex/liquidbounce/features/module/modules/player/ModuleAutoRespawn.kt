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
import net.minecraft.client.gui.screens.DeathScreen
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket

/**
 * AutoRespawn — instantly respawns after death.
 */
object ModuleAutoRespawn : Module("AutoRespawn", Category.PLAYER) {

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent>(ignoreCondition = true) {
        if (!enabled) return@handler

        val player = mc.player ?: return@handler
        if (player.isDeadOrDying || player.health <= 0f || mc.screen is DeathScreen) {
            mc.connection?.send(
                ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
            )
            if (mc.screen is DeathScreen) {
                mc.setScreen(null)
            }
        }
    }
}
