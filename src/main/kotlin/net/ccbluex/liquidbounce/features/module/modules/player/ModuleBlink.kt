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

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.network.protocol.Packet

/**
 * Blink — holds all outgoing packets while enabled, then flushes them when disabled,
 * making the player appear frozen server-side and then "teleport".
 */
object ModuleBlink : Module("Blink", Category.PLAYER) {

    private val queued = mutableListOf<Packet<*>>()

    @Suppress("unused")
    val onPacket = handler<PacketEvent> { event ->
        if (event.origin == TransferOrigin.SEND) {
            queued.add(event.packet)
            event.cancelEvent()
        }
    }

    override fun disable() {
        val connection = mc.connection
        val pending = ArrayList(queued)
        queued.clear()

        if (connection != null) {
            for (packet in pending) {
                connection.send(packet)
            }
        }
    }
}
