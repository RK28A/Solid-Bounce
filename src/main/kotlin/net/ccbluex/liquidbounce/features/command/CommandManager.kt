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
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.event.events.ChatSendEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.misc.ModuleEnchantCracker
import net.ccbluex.liquidbounce.features.ui.clickgui.ClickGuiScreen
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.mc
import org.lwjgl.glfw.GLFW

/**
 * Minimal chat-prefix command handler (a full command tree is ported later).
 *
 * Prefix: "." — e.g. ".t sprint", ".bind killaura R", ".list movement", ".panic".
 */
object CommandManager : Listenable {

    const val PREFIX = "."

    @Suppress("unused")
    val onChat = handler<ChatSendEvent>(ignoreCondition = true) { event ->
        val message = event.message
        if (!message.startsWith(PREFIX)) {
            return@handler
        }

        event.cancelEvent()
        val args = message.removePrefix(PREFIX).trim().split(Regex("\\s+"))
        execute(args)
    }

    private fun execute(args: List<String>) {
        when (args.getOrNull(0)?.lowercase()) {
            "", null, "help" -> {
                chat("Commands: t/toggle <module>, bind <module> <key>, list [category], modules, panic, gui, save, load, enchant")
            }

            "gui", "clickgui" -> mc.setScreen(ClickGuiScreen())

            "enchant" -> {
                if (ModuleEnchantCracker.enabled) {
                    ModuleEnchantCracker.report()
                } else {
                    chat("Enable the EnchantCracker module first.")
                }
            }

            "save" -> {
                ConfigSystem.save()
                chat("Config saved.")
            }

            "load" -> {
                ConfigSystem.load()
                chat("Config loaded.")
            }

            "t", "toggle" -> {
                val module = args.getOrNull(1)?.let { ModuleManager.getModule(it) }
                if (module == null) {
                    chat("Unknown module.")
                } else {
                    module.enabled = !module.enabled
                    chat("${module.configurableName} -> ${if (module.enabled) "ON" else "OFF"}")
                }
            }

            "bind" -> {
                val module = args.getOrNull(1)?.let { ModuleManager.getModule(it) }
                val keyName = args.getOrNull(2)
                if (module == null || keyName == null) {
                    chat("Usage: bind <module> <key|none>")
                    return
                }
                module.bind = parseKey(keyName)
                chat("${module.configurableName} bound to ${keyName.uppercase()}")
            }

            "list", "modules" -> {
                val category = args.getOrNull(1)?.let { Category.fromReadableName(it) }
                val modules = if (category != null) ModuleManager.inCategory(category) else ModuleManager.toList()
                chat(modules.joinToString(", ") {
                    (if (it.enabled) "§a" else "§7") + it.configurableName
                })
            }

            "panic" -> {
                ModuleManager.forEach { if (it.enabled) it.enabled = false }
                chat("All modules disabled.")
            }

            else -> chat("Unknown command. Type '.help'.")
        }
    }

    private fun parseKey(name: String): Int {
        val upper = name.uppercase()
        return when {
            upper == "NONE" || upper == "UNBIND" -> GLFW.GLFW_KEY_UNKNOWN
            upper.length == 1 && upper[0] in 'A'..'Z' -> GLFW.GLFW_KEY_A + (upper[0] - 'A')
            upper.length == 1 && upper[0] in '0'..'9' -> GLFW.GLFW_KEY_0 + (upper[0] - '0')
            else -> GLFW.GLFW_KEY_UNKNOWN
        }
    }
}
