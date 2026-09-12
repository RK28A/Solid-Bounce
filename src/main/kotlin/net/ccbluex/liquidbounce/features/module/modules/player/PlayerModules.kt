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
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.interaction.InteractionUtil
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtil
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import kotlin.math.abs

/** AntiExploit — drops absurd packets that are used to crash or lag clients. */
object ModuleAntiExploit : Module("AntiExploit", Category.PLAYER) {
    private const val MAX_MOTION = 10_000

    @Suppress("unused")
    val onPacket = handler<PacketEvent> { event ->
        if (event.origin != TransferOrigin.RECEIVE) {
            return@handler
        }
        val packet = event.packet
        if (packet is ClientboundSetEntityMotionPacket) {
            if (abs(packet.xa) > MAX_MOTION || abs(packet.ya) > MAX_MOTION || abs(packet.za) > MAX_MOTION) {
                event.cancelEvent()
            }
        }
    }
}

/** AutoBreak — keeps mining whatever block the crosshair is on. */
object ModuleAutoBreak : Module("AutoBreak", Category.PLAYER) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val hit = mc.hitResult
        if (hit == null || hit.type != HitResult.Type.BLOCK) {
            return@handler
        }
        val blockHit = hit as BlockHitResult
        InteractionUtil.continueBreaking(blockHit.blockPos, blockHit.direction)
        player.swing(InteractionHand.MAIN_HAND)
    }
}

/** AutoFish — reels in and recasts when the bobber dips. */
object ModuleAutoFish : Module("AutoFish", Category.PLAYER) {
    private val recastDelay by int("RecastDelay", 15, 1..60)

    private var recastIn = -1

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (recastIn > 0) {
            recastIn--
            return@handler
        }
        if (recastIn == 0) {
            recastIn = -1
            InteractionUtil.useItem()
            return@handler
        }

        val hook = player.fishing ?: return@handler
        // A bite pulls the bobber sharply downward.
        if (hook.deltaMovement.y < -0.08 && hook.isInWater) {
            InteractionUtil.useItem()
            recastIn = recastDelay
        }
    }

    override fun disable() {
        recastIn = -1
    }
}

/** AutoTotem — keeps a totem of undying in the offhand. */
object ModuleAutoTotem : Module("AutoTotem", Category.PLAYER) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (mc.screen is AbstractContainerScreen<*>) {
            return@handler
        }
        if (!player.offhandItem.isEmpty) {
            return@handler
        }
        val slot = InventoryUtil.findInventorySlot { it.`is`(Items.TOTEM_OF_UNDYING) } ?: return@handler
        InventoryUtil.swapToOffhand(slot)
    }
}

/** ChestStealer — empties an open container into the player inventory. */
object ModuleChestStealer : Module("ChestStealer", Category.PLAYER) {
    private val delay by int("Delay", 2, 0..20)
    private var cooldown = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val screen = mc.screen as? AbstractContainerScreen<*> ?: return@handler
        if (cooldown > 0) {
            cooldown--
            return@handler
        }

        val menu = screen.menu
        // Everything before the last 36 slots belongs to the container itself.
        val containerSlots = menu.slots.size - 36
        for (slot in 0 until containerSlots) {
            if (menu.slots[slot].hasItem()) {
                InventoryUtil.quickMove(slot)
                cooldown = delay
                return@handler
            }
        }
    }

    override fun disable() {
        cooldown = 0
    }
}

/** InventoryCleaner — throws away junk items. */
object ModuleInventoryCleaner : Module("InventoryCleaner", Category.PLAYER) {
    private val delay by int("Delay", 4, 0..40)
    private var cooldown = 0

    private val junk: Set<Item> = setOf(
        Items.ROTTEN_FLESH, Items.POISONOUS_POTATO, Items.SPIDER_EYE,
        Items.DIRT, Items.COARSE_DIRT, Items.GRAVEL, Items.COBBLESTONE,
        Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.TUFF,
        Items.WHEAT_SEEDS, Items.FLINT, Items.STICK
    )

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (cooldown > 0) {
            cooldown--
            return@handler
        }
        val slot = InventoryUtil.findInventorySlot { stack ->
            !stack.isEmpty && junk.any { stack.`is`(it) }
        } ?: return@handler

        InventoryUtil.dropInventorySlot(slot)
        cooldown = delay
    }

    override fun disable() {
        cooldown = 0
    }
}

/** Eagle — sneaks while bridging so blocks can be placed off the edge. */
object ModuleEagle : Module("Eagle", Category.PLAYER) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (!player.onGround() || player.mainHandItem.isEmpty) {
            return@handler
        }
        if (world.getBlockState(player.blockPosition().below()).isAir) {
            player.isShiftKeyDown = true
        }
    }
}

/** Reach — extends attack/interaction range. Read by MixinMultiPlayerGameMode. */
object ModuleReach : Module("Reach", Category.PLAYER) {
    private val reach by float("Reach", 3.5f, 3.0f..6.0f)

    /** Read by MixinMultiPlayerGameMode. */
    fun reachDistance(): Float = reach
}

/** Zoot — zoom (narrows the FOV). Read by MixinGameRenderer. */
object ModuleZoot : Module("Zoot", Category.PLAYER) {
    private val factor by float("Factor", 3.0f, 1.5f..10.0f)

    /** Read by MixinGameRenderer. */
    fun zoomFactor(): Double = factor.toDouble()
}

/** FastUse — removes the item-use cooldown. Driven by MixinMinecraft's rightClickDelay reset. */
object ModuleFastUse : Module("FastUse", Category.PLAYER)

// --------------------------------------------------------------------------------------------
// Still pending their dedicated hook.
// --------------------------------------------------------------------------------------------

/** AutoPlay — plays mini-games automatically. TODO: per-game logic. */
object ModuleAutoPlay : Module("AutoPlay", Category.PLAYER)

/** NoRotateSet — ignores server-forced rotations. TODO: MixinClientPacketListener. */
object ModuleNoRotateSet : Module("NoRotateSet", Category.PLAYER)

/** Regen — health regeneration tricks. TODO: packet/inventory logic. */
object ModuleRegen : Module("Regen", Category.PLAYER)
