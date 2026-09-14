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
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.events.ToggleModuleEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.inGame
import net.ccbluex.liquidbounce.utils.client.logger
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.client.notification
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.client.player.LocalPlayer
import org.lwjgl.glfw.GLFW

/**
 * A module (also called "hack") can be enabled and handles events while enabled.
 */
@Suppress("LongParameterList")
open class Module(
    name: String,
    val category: Category,
    bind: Int = GLFW.GLFW_KEY_UNKNOWN,
    state: Boolean = false,
    val disableActivation: Boolean = false,
    hide: Boolean = false,
    val disableOnQuit: Boolean = false
) : Listenable, Configurable(name) {

    val valueEnabled = boolean("Enabled", state).notAnOption()

    private var calledSinceStartup = false

    var enabled: Boolean by valueEnabled.listen { new ->
        runCatching {
            if (!inGame) {
                return@runCatching
            }
            calledSinceStartup = true
            if (new) enable() else disable()
        }.onSuccess {
            if (!disableActivation) {
                if (!ConfigSystem.loading) {
                    notification(configurableName, if (new) "enabled" else "disabled")
                }
                EventManager.callEvent(ToggleModuleEvent(configurableName, new))
                innerValues.filterIsInstance<ChoiceConfigurable>().forEach { it.newState(new) }
            }
        }.onFailure {
            logger.error("Module '$configurableName' failed to ${if (new) "enable" else "disable"}.", it)
        }
        new
    }

    var bind: Int by key("Bind", bind).doNotInclude()

    var hidden: Boolean by boolean("Hidden", hide).doNotInclude()

    /** Shown by the ClickGUI when the module row is held down. */
    open val description: String
        get() = ModuleDescriptions.of(configurableName)

    /** Optional tag displayed next to the module name in the HUD. */
    open val tag: String?
        get() = null

    // Quick access, matching upstream naming (Yarn -> Mojang mappings).
    protected val mc: Minecraft
        inline get() = net.ccbluex.liquidbounce.utils.client.mc
    protected val player: LocalPlayer
        inline get() = mc.player!!
    protected val world: ClientLevel
        inline get() = mc.level!!
    protected val connection: ClientPacketListener
        inline get() = mc.connection!!
    protected val interaction: MultiPlayerGameMode
        inline get() = mc.gameMode!!

    open fun enable() {}
    open fun disable() {}
    open fun init() {}

    override fun handleEvents(): Boolean = enabled && inGame

    val onWorldChange = handler<WorldChangeEvent>(ignoreCondition = true) {
        if (it.world == null) {
            if (disableOnQuit) {
                enabled = false
            }
            calledSinceStartup = false
        } else if (enabled && !calledSinceStartup) {
            calledSinceStartup = true
            runCatching { enable() }.onFailure { e -> logger.error("Late enable of '$configurableName' failed.", e) }
        }
    }

    protected fun choices(name: String, active: Choice, choices: Array<Choice>) =
        choices(this, name, active, choices)

    protected fun choices(
        name: String,
        activeCallback: (ChoiceConfigurable) -> Choice,
        choicesCallback: (ChoiceConfigurable) -> Array<Choice>
    ) = choices(this, name, activeCallback, choicesCallback)
}
