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
 * Remaining Movement modules. Modules marked "scaffold" register/toggle and expose options,
 * with deep behavior pending a dedicated mixin (movement/collision internals), noted per module.
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/** AirJump — lets the player jump again mid-air. */
object ModuleAirJump : Module("AirJump", Category.MOVEMENT) {
    private var wasDown = false

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val down = mc.options.keyJump.isDown
        if (down && !wasDown && !player.onGround()) {
            val m = player.deltaMovement
            player.setDeltaMovement(m.x, 0.42, m.z)
        }
        wasDown = down
    }
}

/** HighJump — increases jump height. */
object ModuleHighJump : Module("HighJump", Category.MOVEMENT) {
    private val power by float("Power", 0.6f, 0.1f..2.0f)
    private var wasDown = false

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val down = mc.options.keyJump.isDown
        if (down && !wasDown && player.onGround()) {
            val m = player.deltaMovement
            player.setDeltaMovement(m.x, power.toDouble(), m.z)
        }
        wasDown = down
    }
}

/** AntiLevitation — negates the levitation effect. TODO: MixinLivingEntity travel(). */
object ModuleAntiLevitation : Module("AntiLevitation", Category.MOVEMENT)

/** AutoDodge — dodges incoming projectiles. TODO: prediction util. */
object ModuleAutoDodge : Module("AutoDodge", Category.MOVEMENT)

/** AvoidHazards — steers away from harmful blocks. TODO: block-scan util. */
object ModuleAvoidHazards : Module("AvoidHazards", Category.MOVEMENT)

/** BlockBounce — bounces off blocks. TODO: MixinPlayerMove. */
object ModuleBlockBounce : Module("BlockBounce", Category.MOVEMENT)

/** BlockWalk — walk on top of specific blocks. TODO: MixinEntity collision. */
object ModuleBlockWalk : Module("BlockWalk", Category.MOVEMENT)

/** BugUp — glitches upward through blocks. TODO: MixinPlayerMove. */
object ModuleBugUp : Module("BugUp", Category.MOVEMENT)

/** ElytraFly — enhanced elytra flight. TODO: MixinPlayer aiStep. */
object ModuleElytraFly : Module("ElytraFly", Category.MOVEMENT)

/** Freeze — freezes the player in place. TODO: MixinPlayerMove. */
object ModuleFreeze : Module("Freeze", Category.MOVEMENT)

/** InventoryMove — allows movement while a GUI is open. TODO: MixinKeyboardInput. */
object ModuleInventoryMove : Module("InventoryMove", Category.MOVEMENT)

/** LiquidWalk — walk on liquids (Jesus). TODO: MixinEntity/fluid collision. */
object ModuleLiquidWalk : Module("LiquidWalk", Category.MOVEMENT)

/** LongJump — extends jump distance. TODO: MixinPlayerMove. */
object ModuleLongJump : Module("LongJump", Category.MOVEMENT) {
    private val distance by float("Distance", 4.0f, 1.0f..10.0f)
}

/** NoClip — passes through blocks. TODO: MixinEntity collision. */
object ModuleNoClip : Module("NoClip", Category.MOVEMENT)

/** NoJumpDelay — removes the vanilla jump cooldown. TODO: MixinLivingEntity jumpTicks. */
object ModuleNoJumpDelay : Module("NoJumpDelay", Category.MOVEMENT)

/** NoPush — ignores entity/block push. TODO: MixinEntity pushOut. */
object ModuleNoPush : Module("NoPush", Category.MOVEMENT)

/** NoSlow — removes item-use / soulsand / web slowdown. TODO: MixinPlayer aiStep. */
object ModuleNoSlow : Module("NoSlow", Category.MOVEMENT)

/** NoWeb — ignores cobweb slowdown. TODO: MixinEntity margin/collision. */
object ModuleNoWeb : Module("NoWeb", Category.MOVEMENT)

/** Parkour — auto-jumps at block edges. TODO: MixinPlayerMove + block-scan util. */
object ModuleParkour : Module("Parkour", Category.MOVEMENT)

/** PerfectHorseJump — perfect horse jump charge. TODO: MixinPlayer horse jump. */
object ModulePerfectHorseJump : Module("PerfectHorseJump", Category.MOVEMENT)

/** ReverseStep — steps down instead of up. TODO: MixinEntity step. */
object ModuleReverseStep : Module("ReverseStep", Category.MOVEMENT)

/** SafeWalk — prevents walking off block edges while sneaking-safe. TODO: MixinPlayerMove safeWalk. */
object ModuleSafeWalk : Module("SafeWalk", Category.MOVEMENT)

/** Step — step up full blocks like a slab. TODO: MixinEntity step height. */
object ModuleStep : Module("Step", Category.MOVEMENT) {
    private val height by float("Height", 1.0f, 0.5f..2.5f)
}

/** Strafe — smooth strafe/air control. TODO: MixinPlayerMove strafe. */
object ModuleStrafe : Module("Strafe", Category.MOVEMENT)

/** TerrainSpeed — speed based on terrain. TODO: MixinPlayerMove. */
object ModuleTerrainSpeed : Module("TerrainSpeed", Category.MOVEMENT)

/** VehicleFly — fly with a boat/horse/etc. TODO: MixinEntity vehicle travel. */
object ModuleVehicleFly : Module("VehicleFly", Category.MOVEMENT)
