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
 * Native ClickGUI. Upstream LiquidBounce nextgen renders its UI through an embedded browser
 * (JCEF + the src-theme Svelte frontend); that subsystem is a separate port. This screen keeps
 * the same model: the same modules, categories, option names, binds and config storage.
 */
package net.ccbluex.liquidbounce.features.ui.clickgui

import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.config.EnumValue
import net.ccbluex.liquidbounce.config.RangedValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.config.ValueType
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleClickGui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

private const val PANEL_WIDTH = 112
private const val HEADER_HEIGHT = 14
private const val ROW_HEIGHT = 12
private const val COLUMNS = 5
private const val MARGIN = 6

private const val COLOR_PANEL_BG = 0xC0101014.toInt()
private const val COLOR_HEADER = 0xFF15151A.toInt()
private const val COLOR_ROW_HOVER = 0x30FFFFFF
private const val COLOR_ACCENT = 0xFF55FFFF.toInt()
private const val COLOR_TEXT = 0xFFCCCCCC.toInt()
private const val COLOR_TEXT_ON = 0xFF55FFFF.toInt()
private const val COLOR_TEXT_DIM = 0xFF808080.toInt()

/** Panel position/expansion survives GUI re-opens. */
class ClickGuiPanel(val category: Category, var x: Int, var y: Int) {
    var open: Boolean = true
    var dragging: Boolean = false
    var dragOffsetX: Int = 0
    var dragOffsetY: Int = 0
    val expanded: MutableSet<String> = mutableSetOf()
}

object ClickGuiState {
    val panels: MutableList<ClickGuiPanel> by lazy {
        Category.entries.mapIndexed { index, category ->
            ClickGuiPanel(
                category,
                MARGIN + (index % COLUMNS) * (PANEL_WIDTH + MARGIN),
                MARGIN + (index / COLUMNS) * 150
            )
        }.toMutableList()
    }
}

class ClickGuiScreen : Screen(Component.literal("ClickGUI")) {

    /** Module currently waiting for a key to be bound. */
    private var bindingModule: Module? = null

    override fun isPauseScreen(): Boolean = false

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)

        for (panel in ClickGuiState.panels) {
            renderPanel(guiGraphics, panel, mouseX, mouseY)
        }

        bindingModule?.let {
            guiGraphics.drawString(
                font, "Press a key to bind '${it.configurableName}' (ESC to unbind)",
                MARGIN, height - 12, COLOR_ACCENT, true
            )
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    private fun renderPanel(guiGraphics: GuiGraphics, panel: ClickGuiPanel, mouseX: Int, mouseY: Int) {
        val x = panel.x
        var y = panel.y

        // Header
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + HEADER_HEIGHT, COLOR_HEADER)
        guiGraphics.drawString(font, panel.category.readableName, x + 4, y + 3, COLOR_ACCENT, true)
        guiGraphics.drawString(font, if (panel.open) "-" else "+", x + PANEL_WIDTH - 10, y + 3, COLOR_TEXT, true)
        y += HEADER_HEIGHT

        if (!panel.open) return

        val modules = ModuleManager.inCategory(panel.category)
        val bodyHeight = modules.sumOf { module ->
            ROW_HEIGHT + if (panel.expanded.contains(module.configurableName)) {
                optionRows(module).size * ROW_HEIGHT
            } else 0
        }
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + bodyHeight, COLOR_PANEL_BG)

        for (module in modules) {
            val hovered = mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + ROW_HEIGHT
            if (hovered) {
                guiGraphics.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT, COLOR_ROW_HOVER)
            }

            guiGraphics.drawString(
                font, module.configurableName, x + 4, y + 2,
                if (module.enabled) COLOR_TEXT_ON else COLOR_TEXT, true
            )
            if (module.bind != GLFW.GLFW_KEY_UNKNOWN) {
                val keyText = keyName(module.bind)
                guiGraphics.drawString(
                    font, keyText, x + PANEL_WIDTH - font.width(keyText) - 4, y + 2, COLOR_TEXT_DIM, true
                )
            }
            y += ROW_HEIGHT

            if (panel.expanded.contains(module.configurableName)) {
                for (value in optionRows(module)) {
                    guiGraphics.drawString(
                        font, "  ${value.name}: ${displayValue(value)}", x + 4, y + 2, COLOR_TEXT_DIM, true
                    )
                    y += ROW_HEIGHT
                }
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (panel in ClickGuiState.panels) {
            val x = panel.x
            var y = panel.y

            // Header: left-drag to move, right-click to collapse
            if (inside(mouseX, mouseY, x, y, PANEL_WIDTH, HEADER_HEIGHT)) {
                if (button == 0) {
                    panel.dragging = true
                    panel.dragOffsetX = mouseX.toInt() - panel.x
                    panel.dragOffsetY = mouseY.toInt() - panel.y
                } else if (button == 1) {
                    panel.open = !panel.open
                }
                return true
            }
            y += HEADER_HEIGHT
            if (!panel.open) continue

            for (module in ModuleManager.inCategory(panel.category)) {
                if (inside(mouseX, mouseY, x, y, PANEL_WIDTH, ROW_HEIGHT)) {
                    when (button) {
                        0 -> module.enabled = !module.enabled
                        1 -> toggleExpanded(panel, module)
                        2 -> bindingModule = module
                    }
                    return true
                }
                y += ROW_HEIGHT

                if (panel.expanded.contains(module.configurableName)) {
                    for (value in optionRows(module)) {
                        if (inside(mouseX, mouseY, x, y, PANEL_WIDTH, ROW_HEIGHT)) {
                            onValueClicked(value, button)
                            return true
                        }
                        y += ROW_HEIGHT
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        ClickGuiState.panels.forEach { it.dragging = false }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(
        mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double
    ): Boolean {
        for (panel in ClickGuiState.panels) {
            if (panel.dragging) {
                panel.x = mouseX.toInt() - panel.dragOffsetX
                panel.y = mouseY.toInt() - panel.dragOffsetY
                return true
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
        for (panel in ClickGuiState.panels) {
            var y = panel.y + HEADER_HEIGHT
            if (!panel.open) continue

            for (module in ModuleManager.inCategory(panel.category)) {
                y += ROW_HEIGHT
                if (!panel.expanded.contains(module.configurableName)) continue

                for (value in optionRows(module)) {
                    if (inside(mouseX, mouseY, panel.x, y, PANEL_WIDTH, ROW_HEIGHT)) {
                        adjustValue(value, delta)
                        return true
                    }
                    y += ROW_HEIGHT
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val module = bindingModule
        if (module != null) {
            module.bind = if (keyCode == GLFW.GLFW_KEY_ESCAPE) GLFW.GLFW_KEY_UNKNOWN else keyCode
            bindingModule = null
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun onClose() {
        // Reset the momentary ClickGui module so the next Right Shift re-opens the GUI.
        ModuleClickGui.enabled = false
        ConfigSystem.save()
        super.onClose()
    }

    // ---------------------------------------------------------------- helpers

    private fun toggleExpanded(panel: ClickGuiPanel, module: Module) {
        val name = module.configurableName
        if (!panel.expanded.remove(name)) {
            panel.expanded.add(name)
        }
    }

    /** Visible option rows of a module (skips the internal "Enabled" value). */
    private fun optionRows(module: Module): List<Value<*>> =
        module.innerValues.filter { it.isOption && it !is Configurable && it !is ChoiceConfigurable }

    private fun inside(mouseX: Double, mouseY: Double, x: Int, y: Int, w: Int, h: Int): Boolean =
        mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h

    @Suppress("UNCHECKED_CAST")
    private fun onValueClicked(value: Value<*>, button: Int) {
        when (value.type) {
            ValueType.BOOLEAN -> (value as Value<Boolean>).set(!value.value)
            ValueType.ENUM -> {
                val enumValue = value as EnumValue<*>
                val choices: Array<out Enum<*>> = enumValue.choices
                if (choices.isNotEmpty()) {
                    val currentIndex = choices.indexOfFirst { it == enumValue.value }
                    val step = if (button == 1) -1 else 1
                    val next = choices[((currentIndex + step) + choices.size) % choices.size]
                    (value as Value<Any>).set(next)
                }
            }
            ValueType.KEY -> (value as Value<Int>).set(GLFW.GLFW_KEY_UNKNOWN)
            else -> {}
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun adjustValue(value: Value<*>, delta: Double) {
        val direction = if (delta > 0) 1 else -1
        when (value.type) {
            ValueType.INT -> (value as Value<Int>).set(value.value + direction)
            ValueType.FLOAT -> {
                val step = if (value is RangedValue<*>) 0.05f else 0.1f
                (value as Value<Float>).set(value.value + direction * step)
            }
            else -> {}
        }
    }

    private fun displayValue(value: Value<*>): String = when (value.type) {
        ValueType.BOOLEAN -> if (value.value as Boolean) "ON" else "OFF"
        ValueType.FLOAT -> String.format("%.2f", value.value as Float)
        ValueType.ENUM -> (value.value as Enum<*>).name
        ValueType.KEY -> keyName(value.value as Int)
        ValueType.INT_RANGE -> (value.value as IntRange).let { "${it.first}-${it.last}" }
        else -> value.value.toString()
    }
}

/** Small, dependency-free key name formatter. */
fun keyName(code: Int): String = when {
    code == GLFW.GLFW_KEY_UNKNOWN -> "NONE"
    code >= GLFW.GLFW_KEY_A && code <= GLFW.GLFW_KEY_Z -> ('A' + (code - GLFW.GLFW_KEY_A)).toString()
    code >= GLFW.GLFW_KEY_0 && code <= GLFW.GLFW_KEY_9 -> ('0' + (code - GLFW.GLFW_KEY_0)).toString()
    code == GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT"
    code == GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT"
    code == GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL"
    code == GLFW.GLFW_KEY_TAB -> "TAB"
    else -> "#$code"
}
