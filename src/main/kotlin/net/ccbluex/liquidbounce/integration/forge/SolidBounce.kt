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
package net.ccbluex.liquidbounce.integration.forge

import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.hud.HudRenderer
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.ModuleRegistry
import net.ccbluex.liquidbounce.utils.client.SolidBounceInfo
import net.ccbluex.liquidbounce.utils.client.logger
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Solid-Bounce entry point (Forge @Mod, loaded via the Kotlin For Forge language adapter).
 */
@Mod("solidbounce")
object SolidBounce {

    const val MOD_ID = "solidbounce"

    init {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MOD_BUS.addListener(::onClientSetup)
            // Registering the bridge must never be able to abort mod construction: a listener Forge
            // rejects would otherwise show up as "Failed to create mod instance" and kill the game.
            runCatching { MinecraftForge.EVENT_BUS.register(ForgeEventBridge) }
                .onFailure { logger.error("Solid-Bounce: could not register the Forge event bridge.", it) }
            logger.info("${SolidBounceInfo.CLIENT_NAME} v${SolidBounceInfo.CLIENT_VERSION} constructing (${SolidBounceInfo.BASED_ON}, MC ${SolidBounceInfo.MC_VERSION}).")
        } else {
            logger.warn("Solid-Bounce is a client mod; skipping init on the dedicated server.")
        }
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            // Nothing in here is worth crashing Minecraft over: a client mod that fails to set
            // itself up should log why and let the game start, not take the whole launch down.
            runCatching {
                // Touch the manager/feature objects so their hooks register with the EventManager.
                ModuleManager
                CommandManager
                HudRenderer
            }.onFailure { logger.error("Solid-Bounce: failed to bring up the core managers.", it) }

            // Registers all modules; each category is isolated internally.
            runCatching { ModuleRegistry.init() }
                .onFailure { logger.error("Solid-Bounce: module registration failed.", it) }

            // Restore persisted module state / binds / option values. A corrupt modules.json must
            // not be fatal either — worst case the client starts with defaults.
            runCatching {
                ConfigSystem
                ConfigSystem.load()
            }.onFailure { logger.error("Solid-Bounce: failed to load the saved configuration.", it) }

            logger.info("Solid-Bounce ready — ${ModuleManager.count} modules loaded.")
        }
    }
}
