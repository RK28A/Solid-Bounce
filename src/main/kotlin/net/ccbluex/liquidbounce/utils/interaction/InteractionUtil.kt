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
package net.ccbluex.liquidbounce.utils.interaction

import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult

/**
 * Shared block/item interaction helpers used by the world modules (Nuker, Scaffold, AutoFarm,
 * Fucker, Ignite, ChestAura, ...).
 */
object InteractionUtil {

    fun startBreaking(pos: BlockPos, face: Direction = Direction.UP) {
        mc.gameMode?.startDestroyBlock(pos, face)
    }

    fun continueBreaking(pos: BlockPos, face: Direction = Direction.UP) {
        mc.gameMode?.continueDestroyBlock(pos, face)
    }

    fun stopBreaking() {
        mc.gameMode?.stopDestroyBlock()
    }

    /** Breaks a block in one call (creative) or keeps chipping at it (survival). */
    fun breakBlock(pos: BlockPos, face: Direction = Direction.UP, swing: Boolean = true) {
        startBreaking(pos, face)
        continueBreaking(pos, face)
        if (swing) {
            mc.player?.swing(InteractionHand.MAIN_HAND)
        }
    }

    fun useOn(hitResult: BlockHitResult, hand: InteractionHand = InteractionHand.MAIN_HAND, swing: Boolean = true) {
        val player = mc.player ?: return
        mc.gameMode?.useItemOn(player, hand, hitResult)
        if (swing) {
            player.swing(hand)
        }
    }

    fun useItem(hand: InteractionHand = InteractionHand.MAIN_HAND) {
        val player = mc.player ?: return
        mc.gameMode?.useItem(player, hand)
    }
}
