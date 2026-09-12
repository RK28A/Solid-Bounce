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
 *
 * Remaining Combat modules. Modules marked "scaffold" register and toggle correctly and
 * expose their options, but their deep behavior depends on a dedicated mixin/util that is
 * being ported incrementally (noted per module).
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.aiming.RotationUtil
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player

/** Aimbot — silently/visibly aims at the nearest entity without attacking. */
object ModuleAimbot : Module("Aimbot", Category.COMBAT) {
    private val range by float("Range", 5.0f, 1.0f..8.0f)
    private val playersOnly by boolean("PlayersOnly", true)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val target = world.entitiesForRendering()
            .filterIsInstance<LivingEntity>()
            .filter {
                it !== player && it.isAlive && it !is ArmorStand &&
                    player.distanceTo(it) <= range && (!playersOnly || it is Player)
            }
            .minByOrNull { player.distanceTo(it) } ?: return@handler

        val rotation = RotationUtil.getRotations(player.eyePosition, target.eyePosition)
        player.yRot = rotation.yaw
        player.xRot = rotation.pitch
    }
}

/** AutoArmor — auto-equips the best armor from the inventory. TODO: container-click util. */
object ModuleAutoArmor : Module("AutoArmor", Category.COMBAT)

/** AutoBalls — auto-throws snowballs/eggs at nearby enemies. TODO: use-item + rotation util. */
object ModuleAutoBalls : Module("AutoBalls", Category.COMBAT)

/** AutoBow — auto-charges and releases the bow when aimed at a target. TODO: use-item hook. */
object ModuleAutoBow : Module("AutoBow", Category.COMBAT)

/** AutoGapple — auto-eats golden apples when low. TODO: use-item + hotbar swap util. */
object ModuleAutoGapple : Module("AutoGapple", Category.COMBAT)

/** AutoHead — targets the head hitbox. TODO: shared target/aim system. */
object ModuleAutoHead : Module("AutoHead", Category.COMBAT)

/** AutoLeave — disconnects on configurable conditions (low health, players near). TODO: disconnect util. */
object ModuleAutoLeave : Module("AutoLeave", Category.COMBAT) {
    private val health by int("Health", 6, 1..20)
}

/** AutoPot — auto-throws splash potions. TODO: use-item + rotation util. */
object ModuleAutoPot : Module("AutoPot", Category.COMBAT)

/** AutoSoup — heals with soup on low health. TODO: use-item + hotbar swap util. */
object ModuleAutoSoup : Module("AutoSoup", Category.COMBAT) {
    private val health by int("Health", 8, 1..20)
}

/** Backtrack — delays entity position updates to hit their past position. TODO: packet buffer. */
object ModuleBacktrack : Module("Backtrack", Category.COMBAT) {
    private val delay by int("Delay", 100, 0..1000)
}

/** FakeLag — delays outbound packets in bursts (like Blink, released periodically). TODO: packet buffer. */
object ModuleFakeLag : Module("FakeLag", Category.COMBAT) {
    private val delay by int("Delay", 300, 0..1000)
}

/** Hitbox — expands entity hitboxes. TODO: MixinEntity margin (EntityMarginEvent). */
object ModuleHitbox : Module("Hitbox", Category.COMBAT) {
    private val size by float("Size", 0.1f, 0.0f..1.0f)
}

/** SwordBlock — 1.8-style visual sword blocking. TODO: item render/use mixin. */
object ModuleSwordBlock : Module("SwordBlock", Category.COMBAT)

/** TimerRange — speeds up game timer only within combat range. TODO: MixinTimer. */
object ModuleTimerRange : Module("TimerRange", Category.COMBAT) {
    private val range by float("Range", 5.0f, 1.0f..8.0f)
}
