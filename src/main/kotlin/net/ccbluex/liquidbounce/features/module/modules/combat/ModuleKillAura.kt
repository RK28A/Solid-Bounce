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
import net.ccbluex.liquidbounce.utils.aiming.RotationUtil
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player

/**
 * KillAura — automatically targets and attacks nearby entities.
 *
 * v1: single-target, rotates the player (non-silent) and attacks at a fixed CPS.
 * Silent rotations, target-switching, predictions and modes are ported later.
 */
object ModuleKillAura : Module("KillAura", Category.COMBAT) {

    private val range by float("Range", 4.2f, 1.0f..6.0f)
    private val cps by int("CPS", 8, 1..20)
    private val rotate by boolean("Rotate", true)
    private val targetPlayersOnly by boolean("PlayersOnly", false)
    private val ignoreArmorStands by boolean("IgnoreArmorStands", true)

    private var lastAttack = 0L

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val target = world.entitiesForRendering()
            .filterIsInstance<LivingEntity>()
            .filter { entity ->
                entity !== player &&
                    entity.isAlive &&
                    player.distanceTo(entity) <= range &&
                    (!ignoreArmorStands || entity !is ArmorStand) &&
                    (!targetPlayersOnly || entity is Player)
            }
            .minByOrNull { player.distanceTo(it) } ?: return@handler

        if (rotate) {
            val rotation = RotationUtil.getRotations(player.eyePosition, target.eyePosition)
            player.yRot = rotation.yaw
            player.xRot = rotation.pitch
        }

        val now = System.currentTimeMillis()
        if (now - lastAttack >= 1000L / cps.coerceAtLeast(1)) {
            mc.gameMode?.attack(player, target)
            player.swing(InteractionHand.MAIN_HAND)
            lastAttack = now
        }
    }
}
