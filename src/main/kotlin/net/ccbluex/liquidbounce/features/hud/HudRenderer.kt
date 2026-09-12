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
package net.ccbluex.liquidbounce.features.hud

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.events.OverlayRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.utils.client.SolidBounceInfo
import net.ccbluex.liquidbounce.utils.client.mc

/**
 * Simple HUD: client watermark + top-right module ArrayList.
 *
 * The full themed/custom HUD system is a later port; this is a functional placeholder.
 */
object HudRenderer : Listenable {

    private const val COLOR_ACCENT = 0x55FFFF
    private const val COLOR_WHITE = 0xFFFFFF

    @Suppress("unused")
    val onRender = handler<OverlayRenderEvent>(ignoreCondition = true) { event ->
        if (mc.options.hideGui || mc.player == null) {
            return@handler
        }

        val gg = event.guiGraphics
        val font = mc.font
        val screenWidth = mc.window.guiScaledWidth

        // Watermark
        gg.drawString(font, "${SolidBounceInfo.CLIENT_NAME} ${SolidBounceInfo.CLIENT_VERSION}", 4, 4, COLOR_ACCENT, true)

        // ArrayList (top-right, longest first)
        val modules = ModuleManager.activeVisible().sortedByDescending { font.width(it.configurableName) }
        var y = 2
        for (module in modules) {
            val name = module.tag?.let { "${module.configurableName} §7$it" } ?: module.configurableName
            val x = screenWidth - font.width(name) - 2
            gg.drawString(font, name, x, y, COLOR_WHITE, true)
            y += font.lineHeight + 1
        }
    }
}
