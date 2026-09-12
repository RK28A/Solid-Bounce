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
 * Remaining World modules (scaffold: register/toggle/options; deep behavior pending interaction
 * util / mixin noted per module).
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/** AutoDisable — disables other modules on configured conditions. TODO: condition system. */
object ModuleAutoDisable : Module("AutoDisable", Category.WORLD)

/** AutoFarm — auto-harvests and replants crops. TODO: interaction util + block-scan. */
object ModuleAutoFarm : Module("AutoFarm", Category.WORLD)

/** ChestAura — auto-opens nearby containers. TODO: interaction util. */
object ModuleChestAura : Module("ChestAura", Category.WORLD)

/** CrystalAura — auto-places and breaks end crystals. TODO: interaction util + rotation. */
object ModuleCrystalAura : Module("CrystalAura", Category.WORLD) {
    private val range by float("Range", 4.0f, 1.0f..6.0f)
}

/** FastBreak — speeds up block breaking. TODO: MixinMultiPlayerGameMode destroyProgress. */
object ModuleFastBreak : Module("FastBreak", Category.WORLD) {
    private val speed by float("Speed", 0.5f, 0.0f..1.0f)
}

/** FastPlace — removes the block-place cooldown. TODO: MixinMinecraft rightClickDelay. */
object ModuleFastPlace : Module("FastPlace", Category.WORLD)

/** Fucker — rapidly breaks a targeted block/area. TODO: interaction util. */
object ModuleFucker : Module("Fucker", Category.WORLD)

/** Ignite — auto-uses flint & steel on entities/blocks. TODO: interaction util. */
object ModuleIgnite : Module("Ignite", Category.WORLD)

/** NoSlowBreak — ignores mining slowdown (e.g., in water/air). TODO: MixinPlayer breakSpeed. */
object ModuleNoSlowBreak : Module("NoSlowBreak", Category.WORLD)

/** Nuker — breaks all breakable blocks around the player. TODO: interaction util + block-scan. */
object ModuleNuker : Module("Nuker", Category.WORLD) {
    private val range by float("Range", 4.5f, 1.0f..6.0f)
}

/** ProjectilePuncher — hits nearby projectiles back. TODO: rotation + interaction util. */
object ModuleProjectilePuncher : Module("ProjectilePuncher", Category.WORLD)

/** Scaffold — auto-bridges beneath the player. TODO: interaction util + rotation + block-scan. */
object ModuleScaffold : Module("Scaffold", Category.WORLD)

/** Timer — changes the game tick speed. TODO: MixinTimer advanceTime. */
object ModuleTimer : Module("Timer", Category.WORLD) {
    private val speed by float("Speed", 1.0f, 0.1f..10.0f)
}
