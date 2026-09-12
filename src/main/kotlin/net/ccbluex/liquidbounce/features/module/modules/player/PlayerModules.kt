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
 * Remaining Player modules (scaffold: register/toggle/options; deep behavior pending the
 * noted mixin/util).
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/** AntiExploit — blocks known crash/exploit packets. TODO: MixinConnection filters. */
object ModuleAntiExploit : Module("AntiExploit", Category.PLAYER)

/** AutoBreak — auto-mines the block being looked at. TODO: interaction util. */
object ModuleAutoBreak : Module("AutoBreak", Category.PLAYER)

/** AutoFish — auto-reels when the bobber bites. TODO: packet hook (fishing sound/hook). */
object ModuleAutoFish : Module("AutoFish", Category.PLAYER)

/** AutoPlay — plays mini-games automatically. TODO: game-specific logic. */
object ModuleAutoPlay : Module("AutoPlay", Category.PLAYER)

/** AutoTotem — keeps a totem in the offhand. TODO: container-click util. */
object ModuleAutoTotem : Module("AutoTotem", Category.PLAYER)

/** ChestStealer — auto-loots containers. TODO: container-click util. */
object ModuleChestStealer : Module("ChestStealer", Category.PLAYER)

/** Eagle — auto-sneaks at block edges while bridging. TODO: MixinPlayerMove + block-scan. */
object ModuleEagle : Module("Eagle", Category.PLAYER)

/** FastUse — removes item-use cooldown. TODO: MixinMinecraft rightClickDelay. */
object ModuleFastUse : Module("FastUse", Category.PLAYER)

/** InventoryCleaner — drops junk items. TODO: container-click util. */
object ModuleInventoryCleaner : Module("InventoryCleaner", Category.PLAYER)

/** NoRotateSet — ignores server-forced rotations. TODO: MixinClientPlayNetworkHandler. */
object ModuleNoRotateSet : Module("NoRotateSet", Category.PLAYER)

/** Reach — extends attack reach. TODO: MixinMultiPlayerGameMode / AT on reach distance. */
object ModuleReach : Module("Reach", Category.PLAYER) {
    private val reach by float("Reach", 3.5f, 3.0f..6.0f)
}

/** Regen — faster health regeneration tricks. TODO: packet/inventory logic. */
object ModuleRegen : Module("Regen", Category.PLAYER)

/** Zoot — zoom (adjustable FOV) while a key is held. TODO: MixinGameRenderer FOV. */
object ModuleZoot : Module("Zoot", Category.PLAYER)
