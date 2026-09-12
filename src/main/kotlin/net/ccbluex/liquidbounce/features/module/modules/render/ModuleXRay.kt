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

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

/**
 * XRay — hides every block except the configured ones (ores, containers, ...).
 *
 * The actual face culling is done in
 * [net.ccbluex.liquidbounce.injection.mixins.world.MixinBlock], which redirects
 * `Block.shouldRenderFace` to [shouldShow] while this module is enabled.
 * Toggling the module forces a full chunk re-render so the change is applied immediately.
 */
object ModuleXRay : Module("XRay", Category.RENDER) {

    private val showFluids by boolean("ShowFluids", false)
    private val showContainers by boolean("ShowContainers", true)

    /** Ores and other valuables that stay visible. */
    private val ores: Set<Block> = setOf(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
        Blocks.ANCIENT_DEBRIS,
        Blocks.RAW_IRON_BLOCK, Blocks.RAW_COPPER_BLOCK, Blocks.RAW_GOLD_BLOCK,
        Blocks.BUDDING_AMETHYST,
        Blocks.SPAWNER,
        Blocks.TNT,
        Blocks.BEACON,
        Blocks.END_PORTAL_FRAME,
        Blocks.NETHER_PORTAL,
        Blocks.LODESTONE
    )

    /** Containers, shown when [showContainers] is on. */
    private val containers: Set<Block> = setOf(
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
        Blocks.BARREL, Blocks.SHULKER_BOX, Blocks.HOPPER,
        Blocks.DISPENSER, Blocks.DROPPER, Blocks.FURNACE,
        Blocks.BLAST_FURNACE, Blocks.SMOKER, Blocks.BREWING_STAND
    )

    /**
     * Called from the mixin for every block face: true keeps the face, false hides the block.
     */
    fun shouldShow(state: BlockState): Boolean {
        val block = state.block

        if (block in ores) {
            return true
        }
        if (showContainers && block in containers) {
            return true
        }
        if (showFluids && !state.fluidState.isEmpty) {
            return true
        }
        return false
    }

    override fun enable() {
        reloadChunks()
    }

    override fun disable() {
        reloadChunks()
    }

    private fun reloadChunks() {
        mc.levelRenderer.allChanged()
    }
}
