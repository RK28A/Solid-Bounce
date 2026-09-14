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
package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.entity.player.PlayerModelPart
import kotlin.random.Random

/** Derp — spins the rotation the server sees while leaving your own view alone. */
object ModuleDerp : Module("Derp", Category.FUN) {
    private val spinSpeed by float("SpinSpeed", 40.0f, 1.0f..180.0f)

    private var yaw = 0f

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        yaw = (yaw + spinSpeed) % 360f
        mc.connection?.send(
            ServerboundMovePlayerPacket.Rot(yaw, player.xRot, player.onGround())
        )
    }
}

/** SkinDerp — randomly flickers the visible skin layers. */
object ModuleSkinDerp : Module("SkinDerp", Category.FUN) {
    private val intervalTicks by int("Interval", 4, 1..40)

    private var ticks = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (++ticks < intervalTicks) return@handler
        ticks = 0

        for (part in PlayerModelPart.entries) {
            mc.options.toggleModelPart(part, Random.nextBoolean())
        }
    }

    override fun disable() {
        // Put every layer back on so we do not leave the skin mangled.
        for (part in PlayerModelPart.entries) {
            mc.options.toggleModelPart(part, true)
        }
    }
}

/** DankBobbing — exaggerates the walking bob. */
object ModuleDankBobbing : Module("DankBobbing", Category.FUN) {
    private val amount by float("Amount", 5.0f, 1.0f..20.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        player.bob = player.bob * amount
    }
}

/** HandDerp — keeps the arm swinging wildly. */
object ModuleHandDerp : Module("HandDerp", Category.FUN) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        player.attackAnim = Random.nextFloat()
    }

    override fun disable() {
        mc.player?.attackAnim = 0f
    }
}
