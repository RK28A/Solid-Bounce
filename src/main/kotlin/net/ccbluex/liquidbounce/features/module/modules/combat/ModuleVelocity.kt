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

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.network.protocol.game.ClientboundExplodePacket
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket

/**
 * Velocity — reduces or cancels knockback.
 *
 * v1: full cancel of the server-sent motion for the local player (the classic "0/0" velocity).
 * Percentage scaling needs mutation of the (final) packet fields via a mixin/accessor and is
 * ported later.
 */
object ModuleVelocity : Module("Velocity", Category.COMBAT) {

    @Suppress("unused")
    val onPacket = handler<PacketEvent> { event ->
        if (event.origin != TransferOrigin.RECEIVE) {
            return@handler
        }

        when (val packet = event.packet) {
            is ClientboundSetEntityMotionPacket -> {
                if (packet.id == player.id) {
                    event.cancelEvent()
                }
            }

            is ClientboundExplodePacket -> {
                event.cancelEvent()
            }
        }
    }
}
