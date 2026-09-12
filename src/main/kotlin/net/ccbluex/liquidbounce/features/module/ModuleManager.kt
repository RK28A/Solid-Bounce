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

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.events.KeyEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.logger
import net.ccbluex.liquidbounce.utils.client.mc
import org.lwjgl.glfw.GLFW

object ModuleManager : Listenable, Iterable<Module> {

    private val modules = mutableListOf<Module>()

    override fun iterator(): Iterator<Module> = modules.iterator()

    val count: Int get() = modules.size

    fun addModule(module: Module) {
        modules += module
        runCatching { module.init() }.onFailure {
            logger.error("Failed to init module '${module.configurableName}'.", it)
        }
    }

    fun addModules(vararg modulesToAdd: Module) = modulesToAdd.forEach(::addModule)

    fun getModule(name: String): Module? =
        modules.firstOrNull { it.configurableName.equals(name, ignoreCase = true) }

    fun inCategory(category: Category): List<Module> = modules.filter { it.category == category }

    /** Modules currently enabled and not hidden — used by the ArrayList HUD. */
    fun activeVisible(): List<Module> = modules.filter { it.enabled && !it.hidden }

    // Toggle modules on their configured key bind (only when no screen is open).
    @Suppress("unused")
    val onKey = handler<KeyEvent>(ignoreCondition = true) { event ->
        if (event.action != GLFW.GLFW_PRESS) return@handler
        if (mc.screen != null) return@handler
        if (event.key == GLFW.GLFW_KEY_UNKNOWN) return@handler

        modules.filter { it.bind == event.key }.forEach { it.enabled = !it.enabled }
    }
}
