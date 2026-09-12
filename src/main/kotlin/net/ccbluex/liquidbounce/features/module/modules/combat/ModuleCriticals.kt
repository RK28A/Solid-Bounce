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
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * Criticals — sends a mini-jump packet sequence on attack so the server registers a critical hit.
 *
 * Packet-based; works while standing on the ground and not in a fluid/vehicle.
 */
object ModuleCriticals : Module("Criticals", Category.COMBAT) {

    @Suppress("unused")
    val onAttack = handler<AttackEvent> {
        if (!player.onGround() || player.isInWater || player.isInLava || player.vehicle != null) {
            return@handler
        }

        val x = player.x
        val y = player.y
        val z = player.z
        val net = connection

        net.send(ServerboundMovePlayerPacket.Pos(x, y + 0.11, z, false))
        net.send(ServerboundMovePlayerPacket.Pos(x, y + 0.1103, z, false))
        net.send(ServerboundMovePlayerPacket.Pos(x, y + 0.0022, z, false))
        net.send(ServerboundMovePlayerPacket.Pos(x, y, z, false))
    }
}
