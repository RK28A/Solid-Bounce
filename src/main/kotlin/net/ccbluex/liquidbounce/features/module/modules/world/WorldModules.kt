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
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.utils.interaction.InteractionUtil
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtil
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

/** Nuker — breaks every breakable block within range, one per tick. */
object ModuleNuker : Module("Nuker", Category.WORLD) {
    private val range by float("Range", 4.5f, 1.0f..6.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val radius = range.toInt() + 1
        val center = player.blockPosition()
        val eye = player.eyePosition

        for (pos in BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        )) {
            val state = world.getBlockState(pos)
            if (state.isAir) continue
            if (state.getDestroySpeed(world, pos) < 0f) continue
            if (eye.distanceTo(Vec3.atCenterOf(pos)) > range) continue

            InteractionUtil.breakBlock(pos.immutable(), Direction.UP)
            return@handler
        }
    }
}

/** Fucker — keeps destroying the block the crosshair is on. */
object ModuleFucker : Module("Fucker", Category.WORLD) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val hit = mc.hitResult
        if (hit == null || hit.type != HitResult.Type.BLOCK) return@handler
        val blockHit = hit as BlockHitResult
        InteractionUtil.breakBlock(blockHit.blockPos, blockHit.direction)
    }
}

/** FastBreak — pushes extra break progress each tick on the targeted block. */
object ModuleFastBreak : Module("FastBreak", Category.WORLD) {
    private val repeats by int("Repeats", 4, 1..10)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (!mc.options.keyAttack.isDown) return@handler
        val hit = mc.hitResult
        if (hit == null || hit.type != HitResult.Type.BLOCK) return@handler
        val blockHit = hit as BlockHitResult

        repeat(repeats) {
            InteractionUtil.continueBreaking(blockHit.blockPos, blockHit.direction)
        }
    }
}

/** Scaffold — places a block under the player while walking. */
object ModuleScaffold : Module("Scaffold", Category.WORLD) {
    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val below = player.blockPosition().below()
        if (!world.getBlockState(below).isAir) return@handler

        val slot = InventoryUtil.findHotbarSlot { !it.isEmpty && it.item is BlockItem } ?: return@handler
        InventoryUtil.select(slot)

        // Find a neighbouring solid block we can place against.
        for (direction in Direction.values()) {
            val neighbor = below.relative(direction)
            if (world.getBlockState(neighbor).isAir) continue

            val hit = BlockHitResult(Vec3.atCenterOf(neighbor), direction.opposite, neighbor, false)
            InteractionUtil.useOn(hit)
            return@handler
        }
    }
}

/** AutoFarm — harvests fully grown crops around the player. */
object ModuleAutoFarm : Module("AutoFarm", Category.WORLD) {
    private val range by float("Range", 4.5f, 1.0f..6.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val radius = range.toInt() + 1
        val center = player.blockPosition()
        val eye = player.eyePosition

        for (pos in BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        )) {
            val state = world.getBlockState(pos)
            val block = state.block
            if (block !is CropBlock || !block.isMaxAge(state)) continue
            if (eye.distanceTo(Vec3.atCenterOf(pos)) > range) continue

            InteractionUtil.breakBlock(pos.immutable(), Direction.UP)
            return@handler
        }
    }
}

/** ChestAura — opens containers around the player. */
object ModuleChestAura : Module("ChestAura", Category.WORLD) {
    private val range by float("Range", 4.5f, 1.0f..6.0f)
    private val delay by int("Delay", 20, 1..100)

    private val containers: Set<Block> = setOf(
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.ENDER_CHEST, Blocks.SHULKER_BOX
    )

    private var cooldown = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (cooldown > 0) {
            cooldown--
            return@handler
        }
        if (mc.screen != null) return@handler

        val radius = range.toInt() + 1
        val center = player.blockPosition()
        val eye = player.eyePosition

        for (pos in BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        )) {
            if (world.getBlockState(pos).block !in containers) continue
            if (eye.distanceTo(Vec3.atCenterOf(pos)) > range) continue

            val hit = BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos.immutable(), false)
            InteractionUtil.useOn(hit)
            cooldown = delay
            return@handler
        }
    }

    override fun disable() {
        cooldown = 0
    }
}

/** CrystalAura — breaks end crystals in range. */
object ModuleCrystalAura : Module("CrystalAura", Category.WORLD) {
    private val range by float("Range", 4.5f, 1.0f..6.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val crystal = world.entitiesForRendering()
            .firstOrNull { it is EndCrystal && player.distanceTo(it) <= range } ?: return@handler

        mc.gameMode?.attack(player, crystal)
        player.swing(InteractionHand.MAIN_HAND)
    }
}

/** ProjectilePuncher — swats incoming projectiles out of the air. */
object ModuleProjectilePuncher : Module("ProjectilePuncher", Category.WORLD) {
    private val range by float("Range", 4.0f, 1.0f..6.0f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val projectile = world.entitiesForRendering()
            .firstOrNull { it is Projectile && player.distanceTo(it) <= range } ?: return@handler

        mc.gameMode?.attack(player, projectile)
        player.swing(InteractionHand.MAIN_HAND)
    }
}

/** Ignite — lights blocks with flint and steel. */
object ModuleIgnite : Module("Ignite", Category.WORLD) {
    private val delay by int("Delay", 10, 1..60)
    private var cooldown = 0

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (cooldown > 0) {
            cooldown--
            return@handler
        }
        val slot = InventoryUtil.findHotbarSlot { it.`is`(Items.FLINT_AND_STEEL) } ?: return@handler

        val hit = mc.hitResult
        if (hit == null || hit.type != HitResult.Type.BLOCK) return@handler

        InventoryUtil.select(slot)
        InteractionUtil.useOn(hit as BlockHitResult)
        cooldown = delay
    }

    override fun disable() {
        cooldown = 0
    }
}

/** Timer — changes the game tick speed. Read by MixinTimer. */
object ModuleTimer : Module("Timer", Category.WORLD) {
    private val speed by float("Speed", 1.5f, 0.1f..10.0f)

    /** Read by MixinTimer. */
    fun timerSpeed(): Float = speed
}

/** FastPlace — removes the block-place cooldown. Driven by MixinMinecraft's rightClickDelay reset. */
object ModuleFastPlace : Module("FastPlace", Category.WORLD)

/** NoSlowBreak — ignores the underwater/airborne mining penalty. Driven by MixinPlayer. */
object ModuleNoSlowBreak : Module("NoSlowBreak", Category.WORLD)

/** AutoDisable — turns every other module off when you die or leave the world. */
object ModuleAutoDisable : Module("AutoDisable", Category.WORLD) {
    private val onDeath by boolean("OnDeath", true)
    private val onWorldLeave by boolean("OnWorldLeave", true)

    // Not named onWorldChange: Module already declares a handler under that name, and a property
    // of the same name in a subclass hides it, which Kotlin rejects.
    @Suppress("unused")
    val worldLeaveHandler = handler<WorldChangeEvent>(ignoreCondition = true) { event ->
        if (enabled && onWorldLeave && event.world == null) {
            disableEverythingElse()
        }
    }

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        if (onDeath && player.health <= 0f) {
            disableEverythingElse()
        }
    }

    private fun disableEverythingElse() {
        ModuleManager.forEach { module ->
            if (module !== ModuleAutoDisable && module.enabled) {
                module.enabled = false
            }
        }
    }
}
