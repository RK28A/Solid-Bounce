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
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.event.events.AttackEvent
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.WorldRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.ui.clickgui.ClickGuiScreen
import net.minecraft.client.CameraType
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.SwordItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.lwjgl.glfw.GLFW

/**
 * Small helper shared by the block-scanning ESP modules: walks a cube around the player and
 * draws an outline on every position the predicate accepts.
 */
private inline fun renderMatchingBlocks(
    event: WorldRenderEvent,
    radius: Int,
    red: Float,
    green: Float,
    blue: Float,
    predicate: (BlockPos) -> Boolean
) {
    val mc = net.ccbluex.liquidbounce.utils.client.mc
    val player = mc.player ?: return

    val poseStack = event.matrixStack
    val cam = event.camera.position
    val buffers = mc.renderBuffers().bufferSource()
    val consumer = buffers.getBuffer(RenderType.lines())

    poseStack.pushPose()
    poseStack.translate(-cam.x, -cam.y, -cam.z)

    val center = player.blockPosition()
    for (pos in BlockPos.betweenClosed(
        center.offset(-radius, -radius, -radius),
        center.offset(radius, radius, radius)
    )) {
        if (!predicate(pos)) continue
        LevelRenderer.renderLineBox(poseStack, consumer, AABB(pos), red, green, blue, 1.0f)
    }

    poseStack.popPose()
    buffers.endBatch(RenderType.lines())
}

/** ClickGui — opens the click-based settings GUI (default bind: Right Shift). */
object ModuleClickGui : Module(
    "ClickGui",
    Category.RENDER,
    bind = GLFW.GLFW_KEY_RIGHT_SHIFT,
    disableActivation = true
) {
    override fun enable() {
        // Never pop the GUI open while a config is being applied at startup.
        if (ConfigSystem.loading) return
        mc.setScreen(ClickGuiScreen())
    }
}

/** BlockESP — outlines every instance of a chosen block around the player. */
object ModuleBlockESP : Module("BlockESP", Category.RENDER) {
    private val block by list("Block", "diamond_ore") {
        BuiltInRegistries.BLOCK.keySet().map { it.path }.sorted()
    }
    private val radius by int("Radius", 16, 4..48)

    private fun target(): Block? =
        BuiltInRegistries.BLOCK.keySet()
            .firstOrNull { it.path.equals(block, ignoreCase = true) }
            ?.let { BuiltInRegistries.BLOCK.get(it) }

    @Suppress("unused")
    val renderHandler = handler<WorldRenderEvent> { event ->
        val wanted = target() ?: return@handler
        renderMatchingBlocks(event, radius, 0.2f, 1.0f, 0.4f) { pos ->
            world.getBlockState(pos).block === wanted
        }
    }
}

/** StorageESP — outlines chests, barrels, shulkers and other containers. */
object ModuleStorageESP : Module("StorageESP", Category.RENDER) {
    private val radius by int("Radius", 24, 4..48)

    private val containers: Set<Block> = setOf(
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.BARREL,
        Blocks.SHULKER_BOX, Blocks.HOPPER, Blocks.DISPENSER, Blocks.DROPPER,
        Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER
    )

    @Suppress("unused")
    val renderHandler = handler<WorldRenderEvent> { event ->
        renderMatchingBlocks(event, radius, 1.0f, 0.7f, 0.1f) { pos ->
            world.getBlockState(pos).block in containers
        }
    }
}

/** HoleESP — outlines 1x1 holes that are safe to stand in. */
object ModuleHoleESP : Module("HoleESP", Category.RENDER) {
    private val radius by int("Radius", 8, 2..24)

    @Suppress("unused")
    val renderHandler = handler<WorldRenderEvent> { event ->
        renderMatchingBlocks(event, radius, 0.2f, 0.6f, 1.0f) { pos ->
            if (!world.getBlockState(pos).isAir) return@renderMatchingBlocks false
            if (!world.getBlockState(pos.above()).isAir) return@renderMatchingBlocks false
            if (world.getBlockState(pos.below()).isAir) return@renderMatchingBlocks false

            // All four sides must be walled in for the hole to be safe.
            !world.getBlockState(pos.north()).isAir &&
                !world.getBlockState(pos.south()).isAir &&
                !world.getBlockState(pos.east()).isAir &&
                !world.getBlockState(pos.west()).isAir
        }
    }
}

/** Breadcrumbs — leaves an outlined trail behind the player. */
object ModuleBreadcrumbs : Module("Breadcrumbs", Category.RENDER) {
    private val length by int("Length", 120, 10..600)

    private val trail = ArrayDeque<Vec3>()

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        trail.addLast(player.position())
        while (trail.size > length) {
            trail.removeFirst()
        }
    }

    @Suppress("unused")
    val renderHandler = handler<WorldRenderEvent> { event ->
        if (trail.isEmpty()) return@handler

        val poseStack = event.matrixStack
        val cam = event.camera.position
        val buffers = mc.renderBuffers().bufferSource()
        val consumer = buffers.getBuffer(RenderType.lines())

        poseStack.pushPose()
        poseStack.translate(-cam.x, -cam.y, -cam.z)
        for (point in trail) {
            val box = AABB(
                point.x - 0.05, point.y, point.z - 0.05,
                point.x + 0.05, point.y + 0.1, point.z + 0.05
            )
            LevelRenderer.renderLineBox(poseStack, consumer, box, 0.4f, 0.8f, 1.0f, 1.0f)
        }
        poseStack.popPose()
        buffers.endBatch(RenderType.lines())
    }

    override fun disable() {
        trail.clear()
    }
}

/** MurderMystery — colours players by what they are holding (sword = red, bow = blue). */
object ModuleMurderMystery : Module("MurderMystery", Category.RENDER) {
    @Suppress("unused")
    val renderHandler = handler<WorldRenderEvent> { event ->
        val poseStack = event.matrixStack
        val cam = event.camera.position
        val buffers = mc.renderBuffers().bufferSource()
        val consumer = buffers.getBuffer(RenderType.lines())

        poseStack.pushPose()
        poseStack.translate(-cam.x, -cam.y, -cam.z)

        for (entity in world.entitiesForRendering()) {
            if (entity !is Player || entity === player) continue
            val item = entity.mainHandItem.item

            val (r, g, b) = when {
                item is SwordItem -> Triple(1.0f, 0.2f, 0.2f)
                item is BowItem -> Triple(0.3f, 0.5f, 1.0f)
                else -> continue
            }
            LevelRenderer.renderLineBox(poseStack, consumer, entity.boundingBox, r, g, b, 1.0f)
        }

        poseStack.popPose()
        buffers.endBatch(RenderType.lines())
    }
}

/** JumpEffect — puffs particles under the player on every jump. */
object ModuleJumpEffect : Module("JumpEffect", Category.RENDER) {
    private var wasOnGround = true

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val onGround = player.onGround()
        if (wasOnGround && !onGround) {
            repeat(12) {
                world.addParticle(
                    ParticleTypes.CLOUD,
                    player.x + (Math.random() - 0.5) * 0.6,
                    player.y + 0.05,
                    player.z + (Math.random() - 0.5) * 0.6,
                    0.0, 0.02, 0.0
                )
            }
        }
        wasOnGround = onGround
    }
}

/** AttackEffects — spawns extra particles on the target when you hit it. */
object ModuleAttackEffects : Module("AttackEffects", Category.RENDER) {
    @Suppress("unused")
    val onAttack = handler<AttackEvent> { event ->
        val target = event.target
        repeat(16) {
            world.addParticle(
                ParticleTypes.CRIT,
                target.x + (Math.random() - 0.5) * target.bbWidth,
                target.y + Math.random() * target.bbHeight,
                target.z + (Math.random() - 0.5) * target.bbWidth,
                0.0, 0.0, 0.0
            )
        }
    }
}

/** AntiBlind — strips blindness and darkness client-side. */
object ModuleAntiBlind : Module("AntiBlind", Category.RENDER) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (player.hasEffect(MobEffects.BLINDNESS)) {
            player.removeEffect(MobEffects.BLINDNESS)
        }
        if (player.hasEffect(MobEffects.DARKNESS)) {
            player.removeEffect(MobEffects.DARKNESS)
        }
    }
}

/** OverrideTime — pins the client-side world time. */
object ModuleOverrideTime : Module("OverrideTime", Category.RENDER) {
    private val time by int("Time", 1000, 0..24000)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        world.dayTime = time.toLong()
    }
}

/** OverrideWeather — clears rain and thunder client-side. */
object ModuleOverrideWeather : Module("OverrideWeather", Category.RENDER) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        world.setRainLevel(0.0f)
        world.setThunderLevel(0.0f)
    }
}

/** QuickPerspectiveSwap — cycles the camera perspective each time the bind is pressed. */
object ModuleQuickPerspectiveSwap : Module(
    "QuickPerspectiveSwap",
    Category.RENDER,
    bind = GLFW.GLFW_KEY_V,
    disableActivation = true
) {
    override fun enable() {
        if (ConfigSystem.loading) return
        val options = mc.options
        options.setCameraType(
            when (options.cameraType) {
                CameraType.FIRST_PERSON -> CameraType.THIRD_PERSON_BACK
                CameraType.THIRD_PERSON_BACK -> CameraType.THIRD_PERSON_FRONT
                else -> CameraType.FIRST_PERSON
            }
        )
    }
}

// --------------------------------------------------------------------------------------------
// Mixin-driven render modules.
// --------------------------------------------------------------------------------------------

/** NoBob — removes view bobbing. Driven by MixinGameRenderer.bobView. */
object ModuleNoBob : Module("NoBob", Category.RENDER)

/** NoHurtCam — removes the hurt camera tilt. Driven by MixinGameRenderer.bobHurt. */
object ModuleNoHurtCam : Module("NoHurtCam", Category.RENDER)

/** NoFov — keeps the FOV fixed. Driven by MixinAbstractClientPlayer. */
object ModuleNoFov : Module("NoFov", Category.RENDER)

/** NoSwing — hides the hand swing. Driven by MixinLivingEntity.swing. */
object ModuleNoSwing : Module("NoSwing", Category.RENDER)

/** NoSignRender — skips the sign edit screen. Driven by MixinClientPacketListener. */
object ModuleNoSignRender : Module("NoSignRender", Category.RENDER)

/** CameraClip — third-person camera passes through blocks. Driven by MixinCamera. */
object ModuleCameraClip : Module("CameraClip", Category.RENDER)

/** Scoreboard — shows/hides the vanilla sidebar. Driven by MixinGui. */
object ModuleScoreboard : Module("Scoreboard", Category.RENDER, state = true)

/** TrueSight — reveals invisible entities. Driven by MixinEntity.isInvisible. */
object ModuleTrueSight : Module("TrueSight", Category.RENDER)

// --------------------------------------------------------------------------------------------
// Still pending a dedicated render hook.
// --------------------------------------------------------------------------------------------

/** Animation — custom item/hand animations. TODO: MixinItemInHandRenderer. */
object ModuleAnimation : Module("Animation", Category.RENDER)

/** CombineMobs — stacks identical mobs visually. TODO: MixinEntityRenderDispatcher. */
object ModuleCombineMobs : Module("CombineMobs", Category.RENDER)

/** Debug — developer debug overlay. TODO: overlay renderer. */
object ModuleDebug : Module("Debug", Category.RENDER)

/** FreeCam — detaches the camera from the player. TODO: MixinCamera + input rerouting. */
object ModuleFreeCam : Module("FreeCam", Category.RENDER)

/** Hud — toggles the client HUD. TODO: wire to the HUD renderer. */
object ModuleHud : Module("Hud", Category.RENDER, state = true)

/** Minimap — renders a minimap. TODO: overlay renderer + chunk sampling. */
object ModuleMinimap : Module("Minimap", Category.RENDER)

/** MobOwners — shows tamed mob owners. TODO: world-space text renderer. */
object ModuleMobOwners : Module("MobOwners", Category.RENDER)

/** Nametags — enhanced entity nametags. TODO: world-space text renderer. */
object ModuleNametags : Module("Nametags", Category.RENDER)

/** Rotations — visualises client rotations. TODO: renderer. */
object ModuleRotations : Module("Rotations", Category.RENDER)

/** Tracers — lines from the camera to entities. TODO: line renderer with per-vertex normals. */
object ModuleTracers : Module("Tracers", Category.RENDER)

/** Trajectories — predicts projectile arcs. TODO: physics sim + line renderer. */
object ModuleTrajectories : Module("Trajectories", Category.RENDER)
