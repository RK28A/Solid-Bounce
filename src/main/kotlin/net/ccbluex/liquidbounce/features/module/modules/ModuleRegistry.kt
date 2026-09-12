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
package net.ccbluex.liquidbounce.features.module.modules

import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.combat.ModuleAutoClicker
import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleSprint
import net.ccbluex.liquidbounce.features.module.modules.player.ModuleAutoRespawn
import net.ccbluex.liquidbounce.features.module.modules.player.ModuleNoFall
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleESP
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleFullBright

/**
 * Registers every built module with the [ModuleManager].
 *
 * This is the file that grows as the port progresses toward the full LiquidBounce module set.
 */
object ModuleRegistry {

    fun init() {
        ModuleManager.addModules(
            // Combat
            ModuleAutoClicker,
            // Movement
            ModuleSprint,
            // Render
            ModuleFullBright,
            ModuleESP,
            // Player
            ModuleAutoRespawn,
            ModuleNoFall,
        )
    }
}
