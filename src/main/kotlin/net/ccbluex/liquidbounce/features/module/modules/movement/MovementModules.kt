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
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.level.block.Blocks
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
// Mixin-driven modules (behaviour lives in the injection package).
// --------------------------------------------------------------------------------------------

/** NoSlow — removes item-use slowdown. Driven by MixinLocalPlayer's aiStep constant patch. */
object ModuleNoSlow : Module("NoSlow", Category.MOVEMENT)

/** NoWeb — ignores cobweb slowdown. Driven by MixinEntity.makeStuckInBlock. */
object ModuleNoWeb : Module("NoWeb", Category.MOVEMENT)

/** NoPush — ignores entity push. Driven by MixinEntity.push. */
object ModuleNoPush : Module("NoPush", Category.MOVEMENT)

/** NoJumpDelay — removes the vanilla jump cooldown. Driven by MixinLivingEntity.aiStep. */
object ModuleNoJumpDelay : Module("NoJumpDelay", Category.MOVEMENT)

/** Step — step up full blocks. Driven by MixinEntity.maxUpStep. */
object ModuleStep : Module("Step", Category.MOVEMENT) {
    private val height by float("Height", 1.0f, 0.5f..2.5f)

    /** Read by MixinEntity. */
    fun stepHeight(): Float = height
}

// --------------------------------------------------------------------------------------------

/** NoClip — walks through blocks by disabling the player's physics. */
object ModuleNoClip : Module("NoClip", Category.MOVEMENT) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        player.noPhysics = true
    }

    override fun disable() {
        mc.player?.noPhysics = false
    }
}

/** BlockBounce — bounces back up every time the player lands. */
object ModuleBlockBounce : Module("BlockBounce", Category.MOVEMENT) {
    private val power by float("Power", 0.42f, 0.1f..2.0f)
    private var wasAirborne = false

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val onGround = player.onGround()
        if (onGround && wasAirborne) {
            val m = player.deltaMovement
            player.setDeltaMovement(m.x, power.toDouble(), m.z)
        }
        wasAirborne = !onGround
    }
}

/** ReverseStep — drops off ledges instantly instead of easing down. */
object ModuleReverseStep : Module("ReverseStep", Category.MOVEMENT) {
    private val speed by float("Speed", 0.5f, 0.1f..3.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.onGround()) return@handler
        val m = player.deltaMovement
        if (m.y < 0.0) {
            player.setDeltaMovement(m.x, -speed.toDouble(), m.z)
        }
    }
}

/** LiquidWalk — keeps the player on the surface of liquids. */
object ModuleLiquidWalk : Module("LiquidWalk", Category.MOVEMENT) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (!player.isInWater || player.isShiftKeyDown) return@handler
        // Only hold the player up at the surface, otherwise let them sink normally.
        if (world.getBlockState(player.blockPosition().above()).isAir) {
            val m = player.deltaMovement
            player.setDeltaMovement(m.x, 0.0, m.z)
        }
    }
}

/** AvoidHazards — cuts momentum before walking into something that hurts. */
object ModuleAvoidHazards : Module("AvoidHazards", Category.MOVEMENT) {
    private val hazards = setOf(
        Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.MAGMA_BLOCK,
        Blocks.CACTUS, Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE,
        Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.POWDER_SNOW
    )

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val motion = player.deltaMovement
        val ahead = player.blockPosition().offset(
            Math.signum(motion.x).toInt(), 0, Math.signum(motion.z).toInt()
        )

        if (world.getBlockState(ahead).block in hazards ||
            world.getBlockState(ahead.below()).block in hazards
        ) {
            player.setDeltaMovement(0.0, motion.y, 0.0)
        }
    }
}

/** AutoDodge — strafes away from arrows that are flying at the player. */
object ModuleAutoDodge : Module("AutoDodge", Category.MOVEMENT) {
    private val range by float("Range", 8.0f, 1.0f..30.0f)
    private val strength by float("Strength", 0.35f, 0.1f..1.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val threat = world.entitiesForRendering()
            .filterIsInstance<AbstractArrow>()
            .firstOrNull { arrow ->
                if (player.distanceTo(arrow) > range) {
                    false
                } else {
                    val motion = arrow.deltaMovement
                    if (motion.lengthSqr() < 0.01) {
                        false
                    } else {
                        // Is it actually heading towards us?
                        val toPlayer = player.position().subtract(arrow.position()).normalize()
                        motion.normalize().dot(toPlayer) > 0.9
                    }
                }
            } ?: return@handler

        // Sidestep perpendicular to the arrow's flight path.
        val motion = threat.deltaMovement.normalize()
        val m = player.deltaMovement
        player.setDeltaMovement(-motion.z * strength, m.y, motion.x * strength)
    }
}

/** PerfectHorseJump — always charges a mounted jump to full power. */
object ModulePerfectHorseJump : Module("PerfectHorseJump", Category.MOVEMENT) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.vehicle == null) return@handler
        if (mc.options.keyJump.isDown) {
            player.jumpRidingScale = 1.0f
        }
    }
}

/** TerrainSpeed — scales movement with the friction of the ground being walked on. */
object ModuleTerrainSpeed : Module("TerrainSpeed", Category.MOVEMENT) {
    private val factor by float("Factor", 1.5f, 1.0f..3.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (!player.onGround()) return@handler
        val friction = world.getBlockState(player.blockPosition().below()).block.friction
        // Only boost on slippery ground (ice and friends).
        if (friction <= 0.6f) return@handler

        val m = player.deltaMovement
        player.setDeltaMovement(m.x * factor, m.y, m.z * factor)
    }
}

/** BlockWalk — walk on top of specific blocks. TODO: MixinEntity collision shape. */
object ModuleBlockWalk : Module("BlockWalk", Category.MOVEMENT)
