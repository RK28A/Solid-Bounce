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
package net.ccbluex.liquidbounce.features.module.modules.movement

import com.mojang.blaze3d.platform.InputConstants
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

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

/** AntiLevitation — strips the levitation effect client-side so movement stays normal. */
object ModuleAntiLevitation : Module("AntiLevitation", Category.MOVEMENT) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.hasEffect(MobEffects.LEVITATION)) {
            player.removeEffect(MobEffects.LEVITATION)
        }
    }
}

/** Freeze — pins the player in place. */
object ModuleFreeze : Module("Freeze", Category.MOVEMENT) {
    private val keepY by boolean("KeepY", true)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val y = if (keepY) player.deltaMovement.y else 0.0
        player.setDeltaMovement(0.0, y, 0.0)
    }
}

/** BugUp — pushes the player upward while holding jump against a wall. */
object ModuleBugUp : Module("BugUp", Category.MOVEMENT) {
    private val power by float("Power", 0.2f, 0.05f..1.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (!mc.options.keyJump.isDown || !player.horizontalCollision) {
            return@handler
        }
        val m = player.deltaMovement
        player.setDeltaMovement(m.x, power.toDouble(), m.z)
        player.fallDistance = 0f
    }
}

/** LongJump — boosts horizontal speed at the moment the player leaves the ground. */
object ModuleLongJump : Module("LongJump", Category.MOVEMENT) {
    private val boost by float("Boost", 1.6f, 1.0f..4.0f)
    private var wasOnGround = true

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val onGround = player.onGround()
        if (wasOnGround && !onGround) {
            val m = player.deltaMovement
            player.setDeltaMovement(m.x * boost, m.y, m.z * boost)
        }
        wasOnGround = onGround
    }
}

/** Strafe — keeps air control by re-aiming momentum at the movement input. */
object ModuleStrafe : Module("Strafe", Category.MOVEMENT) {
    private val speed by float("Speed", 0.28f, 0.1f..1.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.onGround()) {
            return@handler
        }
        val forward = player.zza.toDouble()
        val strafe = player.xxa.toDouble()
        if (forward == 0.0 && strafe == 0.0) {
            return@handler
        }

        val yaw = Math.toRadians(player.yRot.toDouble())
        val vx = (forward * -sin(yaw) + strafe * cos(yaw)) * speed
        val vz = (forward * cos(yaw) + strafe * sin(yaw)) * speed
        player.setDeltaMovement(vx, player.deltaMovement.y, vz)
    }
}

/** Parkour — jumps automatically when walking off an edge. */
object ModuleParkour : Module("Parkour", Category.MOVEMENT) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (!player.onGround() || player.isShiftKeyDown) {
            return@handler
        }
        if (player.zza == 0f && player.xxa == 0f) {
            return@handler
        }

        // Look one block ahead of the current motion; jump if the ground disappears.
        val motion = player.deltaMovement
        val ahead = player.blockPosition().offset(
            Math.signum(motion.x).toInt(),
            -1,
            Math.signum(motion.z).toInt()
        )
        if (world.getBlockState(ahead).isAir) {
            val m = player.deltaMovement
            player.setDeltaMovement(m.x, 0.42, m.z)
        }
    }
}

/** SafeWalk — sneaks at block edges so the player cannot walk off. */
object ModuleSafeWalk : Module("SafeWalk", Category.MOVEMENT) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (!player.onGround()) {
            return@handler
        }
        val below = player.blockPosition().below()
        if (world.getBlockState(below).isAir) {
            player.isShiftKeyDown = true
        }
    }
}

/** ElytraFly — steers elytra flight with the look direction at a constant speed. */
object ModuleElytraFly : Module("ElytraFly", Category.MOVEMENT) {
    private val speed by float("Speed", 1.0f, 0.1f..3.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (!player.isFallFlying) {
            return@handler
        }
        val look = player.lookAngle
        player.setDeltaMovement(look.x * speed, look.y * speed, look.z * speed)
    }
}

/** VehicleFly — flies the vehicle the player is riding. */
object ModuleVehicleFly : Module("VehicleFly", Category.MOVEMENT) {
    private val speed by float("Speed", 0.8f, 0.1f..3.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val vehicle = player.vehicle ?: return@handler

        val motionY = when {
            mc.options.keyJump.isDown -> speed.toDouble()
            mc.options.keyShift.isDown -> -speed.toDouble()
            else -> 0.0
        }

        val yaw = Math.toRadians(player.yRot.toDouble())
        val forward = player.zza.toDouble()
        val strafe = player.xxa.toDouble()
        val vx = (forward * -sin(yaw) + strafe * cos(yaw)) * speed
        val vz = (forward * cos(yaw) + strafe * sin(yaw)) * speed

        vehicle.setDeltaMovement(Vec3(vx, motionY, vz))
        vehicle.fallDistance = 0f
    }
}

/** InventoryMove — keeps walking while a GUI is open by feeding the raw key state to the input. */
object ModuleInventoryMove : Module("InventoryMove", Category.MOVEMENT) {
    private val allowJump by boolean("AllowJump", true)

    private fun down(keyCode: Int): Boolean =
        InputConstants.isKeyDown(mc.window.window, keyCode)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val screen = mc.screen ?: return@handler
        // Never fight the chat/sign input where the keys are actual text.
        if (screen is net.minecraft.client.gui.screens.ChatScreen) {
            return@handler
        }

        val options = mc.options
        val forward = (if (down(options.keyUp.key.value)) 1f else 0f) -
            (if (down(options.keyDown.key.value)) 1f else 0f)
        val strafe = (if (down(options.keyLeft.key.value)) 1f else 0f) -
            (if (down(options.keyRight.key.value)) 1f else 0f)

        val input = player.input
        input.forwardImpulse = forward
        input.leftImpulse = strafe
        input.up = forward > 0f
        input.down = forward < 0f
        input.left = strafe > 0f
        input.right = strafe < 0f
        if (allowJump) {
            input.jumping = down(options.keyJump.key.value)
        }
    }
}

// --------------------------------------------------------------------------------------------
// Modules below still need a dedicated collision/movement mixin; they register and toggle but
// their effect lands with that mixin.
// --------------------------------------------------------------------------------------------

/** NoSlow — removes item-use slowdown. Driven by MixinLocalPlayer's aiStep constant patch. */
object ModuleNoSlow : Module("NoSlow", Category.MOVEMENT)

/** NoWeb — ignores cobweb slowdown. Driven by MixinEntity.makeStuckInBlock. */
object ModuleNoWeb : Module("NoWeb", Category.MOVEMENT)

/** Step — step up full blocks. Driven by MixinEntity.maxUpStep. */
object ModuleStep : Module("Step", Category.MOVEMENT) {
    private val height by float("Height", 1.0f, 0.5f..2.5f)

    /** Read by MixinEntity. */
    fun stepHeight(): Float = height
}

/** NoClip — passes through blocks. TODO: MixinEntity collision. */
object ModuleNoClip : Module("NoClip", Category.MOVEMENT)

/** NoJumpDelay — removes the vanilla jump cooldown. TODO: MixinLivingEntity noJumpDelay. */
object ModuleNoJumpDelay : Module("NoJumpDelay", Category.MOVEMENT)

/** NoPush — ignores entity/block push. TODO: MixinEntity push. */
object ModuleNoPush : Module("NoPush", Category.MOVEMENT)

/** LiquidWalk — walk on liquids. TODO: MixinEntity fluid collision. */
object ModuleLiquidWalk : Module("LiquidWalk", Category.MOVEMENT)

/** BlockWalk — walk on top of specific blocks. TODO: MixinEntity collision. */
object ModuleBlockWalk : Module("BlockWalk", Category.MOVEMENT)

/** BlockBounce — bounces off blocks. TODO: MixinPlayerMove. */
object ModuleBlockBounce : Module("BlockBounce", Category.MOVEMENT)

/** ReverseStep — steps down instead of up. TODO: MixinEntity step. */
object ModuleReverseStep : Module("ReverseStep", Category.MOVEMENT)

/** AutoDodge — dodges incoming projectiles. TODO: projectile prediction util. */
object ModuleAutoDodge : Module("AutoDodge", Category.MOVEMENT)

/** AvoidHazards — steers away from harmful blocks. TODO: block-scan util. */
object ModuleAvoidHazards : Module("AvoidHazards", Category.MOVEMENT)

/** PerfectHorseJump — perfect horse jump charge. TODO: horse jump power hook. */
object ModulePerfectHorseJump : Module("PerfectHorseJump", Category.MOVEMENT)

/** TerrainSpeed — speed based on terrain. TODO: block friction hook. */
object ModuleTerrainSpeed : Module("TerrainSpeed", Category.MOVEMENT)
