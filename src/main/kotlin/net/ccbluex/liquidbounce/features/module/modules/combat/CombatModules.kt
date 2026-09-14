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
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.friend.FriendManager
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.misc.ModuleAntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.ModuleTeams
import net.ccbluex.liquidbounce.utils.aiming.RotationUtil
import net.ccbluex.liquidbounce.utils.interaction.InteractionUtil
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtil
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.Items

/** Nearest hostile-ish target helper shared by the combat modules. */
private fun nearestTarget(range: Float, playersOnly: Boolean): LivingEntity? {
    val mc = net.ccbluex.liquidbounce.utils.client.mc
    val self = mc.player ?: return null
    val level = mc.level ?: return null
    return level.entitiesForRendering()
        .filterIsInstance<LivingEntity>()
        .filter {
            it !== self && it.isAlive && it !is ArmorStand &&
                self.distanceTo(it) <= range && (!playersOnly || it is Player)
        }
        .filterNot { entity ->
            // Respect the friend list, the Teams module and the AntiBot filter.
            entity is Player && (
                FriendManager.isFriend(entity.gameProfile.name ?: "") ||
                    ModuleTeams.isTeammate(entity) ||
                    ModuleAntiBot.isBot(entity)
                )
        }
        .minByOrNull { self.distanceTo(it) }
}

/** Aimbot — aims at the nearest entity without attacking. */
object ModuleAimbot : Module("Aimbot", Category.COMBAT) {
    private val range by float("Range", 5.0f, 1.0f..8.0f)
    private val playersOnly by boolean("PlayersOnly", true)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val target = nearestTarget(range, playersOnly) ?: return@handler
        val rotation = RotationUtil.getRotations(player.eyePosition, target.eyePosition)
        player.yRot = rotation.yaw
        player.xRot = rotation.pitch
    }
}

/** AutoHead — aims at the target's head instead of its body. */
object ModuleAutoHead : Module("AutoHead", Category.COMBAT) {
    private val range by float("Range", 5.0f, 1.0f..8.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val target = nearestTarget(range, true) ?: return@handler
        val head = target.position().add(0.0, target.eyeHeight.toDouble() + 0.15, 0.0)
        val rotation = RotationUtil.getRotations(player.eyePosition, head)
        player.yRot = rotation.yaw
        player.xRot = rotation.pitch
    }
}

/** AutoArmor — equips armor pieces found in the inventory. */
object ModuleAutoArmor : Module("AutoArmor", Category.COMBAT) {
    private val delay by int("Delay", 4, 0..40)
    private var cooldown = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (cooldown > 0) {
            cooldown--
            return@handler
        }

        for (slot in 0..InventoryUtil.INVENTORY_END) {
            val stack = InventoryUtil.stackAt(slot)
            val item = stack.item
            if (item !is ArmorItem) continue

            val equipmentSlot: EquipmentSlot = item.equipmentSlot
            if (equipmentSlot.type != EquipmentSlot.Type.ARMOR) continue
            if (!player.getItemBySlot(equipmentSlot).isEmpty) continue

            InventoryUtil.quickMove(InventoryUtil.toContainerSlot(slot))
            cooldown = delay
            return@handler
        }
    }

    override fun disable() {
        cooldown = 0
    }
}

/** AutoSoup — uses soup when health drops. */
object ModuleAutoSoup : Module("AutoSoup", Category.COMBAT) {
    private val health by int("Health", 8, 1..20)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.health > health) return@handler
        val slot = InventoryUtil.findHotbarSlot {
            it.`is`(Items.MUSHROOM_STEW) || it.`is`(Items.RABBIT_STEW) || it.`is`(Items.BEETROOT_SOUP)
        } ?: return@handler

        InventoryUtil.select(slot)
        InteractionUtil.useItem()
    }
}

/** AutoGapple — eats a golden apple when health drops. */
object ModuleAutoGapple : Module("AutoGapple", Category.COMBAT) {
    private val health by int("Health", 10, 1..20)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.health > health) return@handler
        val slot = InventoryUtil.findHotbarSlot {
            it.`is`(Items.GOLDEN_APPLE) || it.`is`(Items.ENCHANTED_GOLDEN_APPLE)
        } ?: return@handler

        InventoryUtil.select(slot)
        mc.options.keyUse.setDown(true)
    }

    override fun disable() {
        mc.options.keyUse.setDown(false)
    }
}

/** AutoPot — throws a splash healing potion straight down when health drops. */
object ModuleAutoPot : Module("AutoPot", Category.COMBAT) {
    private val health by int("Health", 8, 1..20)
    private var cooldown = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (cooldown > 0) {
            cooldown--
            return@handler
        }
        if (player.health > health) return@handler

        val slot = InventoryUtil.findHotbarSlot { it.`is`(Items.SPLASH_POTION) } ?: return@handler
        InventoryUtil.select(slot)
        player.xRot = 90f
        InteractionUtil.useItem()
        cooldown = 20
    }

    override fun disable() {
        cooldown = 0
    }
}

/** AutoBalls — throws snowballs/eggs at the nearest target. */
object ModuleAutoBalls : Module("AutoBalls", Category.COMBAT) {
    private val range by float("Range", 12.0f, 1.0f..40.0f)
    private val delay by int("Delay", 4, 0..40)
    private var cooldown = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (cooldown > 0) {
            cooldown--
            return@handler
        }
        val target = nearestTarget(range, true) ?: return@handler
        val slot = InventoryUtil.findHotbarSlot {
            it.`is`(Items.SNOWBALL) || it.`is`(Items.EGG)
        } ?: return@handler

        val rotation = RotationUtil.getRotations(player.eyePosition, target.eyePosition)
        player.yRot = rotation.yaw
        player.xRot = rotation.pitch

        InventoryUtil.select(slot)
        InteractionUtil.useItem()
        cooldown = delay
    }

    override fun disable() {
        cooldown = 0
    }
}

/** AutoBow — charges and releases a bow at the nearest target. */
object ModuleAutoBow : Module("AutoBow", Category.COMBAT) {
    private val range by float("Range", 30.0f, 1.0f..60.0f)
    private val charge by int("ChargeTicks", 20, 5..40)

    private var charging = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.mainHandItem.item !is BowItem) {
            release()
            return@handler
        }
        val target = nearestTarget(range, true) ?: run {
            release()
            return@handler
        }

        val rotation = RotationUtil.getRotations(player.eyePosition, target.eyePosition)
        player.yRot = rotation.yaw
        player.xRot = rotation.pitch

        if (charging < charge) {
            charging++
            mc.options.keyUse.setDown(true)
        } else {
            release()
        }
    }

    private fun release() {
        if (charging > 0) {
            charging = 0
        }
        mc.options.keyUse.setDown(false)
    }

    override fun disable() = release()
}

/** AutoLeave — disconnects when health drops below the threshold. */
object ModuleAutoLeave : Module("AutoLeave", Category.COMBAT) {
    private val health by int("Health", 6, 1..20)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.health > health) return@handler

        runCatching {
            mc.player?.connection?.connection?.disconnect(Component.literal("Solid-Bounce AutoLeave"))
        }
        enabled = false
    }
}

/** FakeLag — holds outgoing packets and releases them in bursts. */
object ModuleFakeLag : Module("FakeLag", Category.COMBAT) {
    private val delayMs by int("Delay", 300, 0..1000)

    private val queued = mutableListOf<Packet<*>>()
    private var lastFlush = 0L

    @Suppress("unused")
    val onPacket = handler<PacketEvent> { event ->
        if (event.origin != TransferOrigin.SEND) return@handler
        queued.add(event.packet)
        event.cancelEvent()
    }

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val now = System.currentTimeMillis()
        if (now - lastFlush >= delayMs) {
            flush()
            lastFlush = now
        }
    }

    private fun flush() {
        val connection = mc.connection ?: run {
            queued.clear()
            return
        }
        val pending = ArrayList(queued)
        queued.clear()
        pending.forEach(connection::send)
    }

    override fun disable() {
        flush()
    }
}

// --------------------------------------------------------------------------------------------
// Still pending their dedicated hook.
// --------------------------------------------------------------------------------------------

/** Backtrack — rewinds entity positions. TODO: inbound packet replay buffer. */
object ModuleBacktrack : Module("Backtrack", Category.COMBAT) {
    private val delay by int("Delay", 100, 0..1000)
}

/** Hitbox — expands entity hitboxes. TODO: MixinEntity bounding box inflate. */
object ModuleHitbox : Module("Hitbox", Category.COMBAT) {
    private val size by float("Size", 0.1f, 0.0f..1.0f)
}

/** SwordBlock — 1.8-style visual sword blocking. TODO: held-item render mixin. */
object ModuleSwordBlock : Module("SwordBlock", Category.COMBAT)

/** TimerRange — speeds the game timer up only while an enemy is within range. */
object ModuleTimerRange : Module("TimerRange", Category.COMBAT) {
    private val range by float("Range", 5.0f, 1.0f..8.0f)
    private val speed by float("Speed", 1.5f, 0.1f..5.0f)

    /** Read by MixinTimer: 1.0 (no change) unless a target is close. */
    fun activeSpeed(): Float = if (nearestTarget(range, false) != null) speed else 1.0f
}
