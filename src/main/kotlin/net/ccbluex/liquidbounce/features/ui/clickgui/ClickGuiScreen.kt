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
import net.ccbluex.liquidbounce.config.ListValue
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
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private const val PANEL_WIDTH = 118
private const val HEADER_HEIGHT = 14
private const val ROW_HEIGHT = 12
private const val COLUMNS = 5
private const val DROPDOWN_ROWS = 10
private const val MARGIN = 6

/** A panel never grows past this; past it the body scrolls. */
private const val MAX_BODY_HEIGHT = 216

/** Right-hand strip of a module row that starts a key binding instead of toggling. */
private const val BIND_ZONE_WIDTH = 36

private const val SEARCH_WIDTH = 200
private const val SEARCH_HEIGHT = 14
private const val SEARCH_RESULTS = 8

/** How long the left button must be held on a module before its description appears. */
private const val HOLD_MILLIS = 350L

private const val COLOR_PANEL_BG = 0xC0101014.toInt()
private const val COLOR_HEADER = 0xFF15151A.toInt()
private const val COLOR_ROW_HOVER = 0x30FFFFFF
private const val COLOR_ACCENT = 0xFF55FFFF.toInt()
private const val COLOR_TEXT = 0xFFCCCCCC.toInt()
private const val COLOR_TEXT_ON = 0xFF55FFFF.toInt()
private const val COLOR_TEXT_DIM = 0xFF808080.toInt()
private const val COLOR_SLIDER_FILL = 0x6055FFFF
private const val COLOR_TOOLTIP_BG = 0xF00C0C10.toInt()
private const val COLOR_SCROLLBAR = 0x5055FFFF

/** Panel position/expansion/scroll survive GUI re-opens. */
class ClickGuiPanel(val category: Category, var x: Int, var y: Int) {
    var open: Boolean = true
    var dragging: Boolean = false
    var dragOffsetX: Int = 0
    var dragOffsetY: Int = 0
    var scroll: Int = 0
    val expanded: MutableSet<String> = mutableSetOf()
}

object ClickGuiState {
    val panels: MutableList<ClickGuiPanel> by lazy {
        Category.entries.mapIndexed { index, category ->
            ClickGuiPanel(
                category,
                MARGIN + (index % COLUMNS) * (PANEL_WIDTH + MARGIN),
                MARGIN + SEARCH_HEIGHT + MARGIN + (index / COLUMNS) * 150
            )
        }.toMutableList()
    }
}

/** One laid-out row: either a module header row (value == null) or one of its options. */
private class GuiRow(val x: Int, val y: Int, val module: Module, val value: Value<*>?) {
    fun contains(mouseX: Double, mouseY: Double): Boolean =
        mouseX >= x && mouseX < x + PANEL_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT
}

class ClickGuiScreen : Screen(Component.literal("ClickGUI")) {

    /** Module currently waiting for a key to be bound. */
    private var bindingModule: Module? = null

    /** KEY-typed option currently waiting for a key. */
    private var bindingValue: Value<Int>? = null

    /** Currently open dropdown (ListValue), if any. */
    private var dropdown: ListValue? = null
    private var dropdownX = 0
    private var dropdownY = 0
    private var dropdownScroll = 0

    /** Numeric option being dragged as a slider. */
    private var sliderValue: Value<*>? = null
    private var sliderX = 0

    /** Text option being edited. */
    private var editingText: Value<String>? = null

    /** Left button held on a module row — after HOLD_MILLIS this shows the description. */
    private var pressedModule: Module? = null
    private var pressedAt = 0L

    private var searchQuery = ""
    private var searchFocused = false

    /** Module a search result pointed at; blinks until the mouse finds it. */
    private var blinkModule: Module? = null

    override fun isPauseScreen(): Boolean = false

    override fun shouldCloseOnEsc(): Boolean =
        bindingModule == null && bindingValue == null && editingText == null

    // ------------------------------------------------------------------ layout

    /** Module rows plus, for expanded modules, their option rows — in display order. */
    private fun rowsOf(panel: ClickGuiPanel): List<Pair<Module, Value<*>?>> {
        val rows = ArrayList<Pair<Module, Value<*>?>>()
        for (module in ModuleManager.inCategory(panel.category)) {
            rows += module to null
            if (panel.expanded.contains(module.configurableName)) {
                optionRows(module).forEach { rows += module to it }
            }
        }
        return rows
    }

    private fun contentHeight(panel: ClickGuiPanel) = rowsOf(panel).size * ROW_HEIGHT

    private fun bodyHeight(panel: ClickGuiPanel) = contentHeight(panel).coerceAtMost(MAX_BODY_HEIGHT)

    private fun maxScroll(panel: ClickGuiPanel) =
        (contentHeight(panel) - MAX_BODY_HEIGHT).coerceAtLeast(0)

    /**
     * Rows with their on-screen position, scroll applied, and anything outside the visible band
     * dropped — so rendering and hit-testing can never disagree about where a row is.
     */
    private fun laidOut(panel: ClickGuiPanel): List<GuiRow> {
        if (!panel.open) return emptyList()
        val top = panel.y + HEADER_HEIGHT
        val bottom = top + bodyHeight(panel)
        return rowsOf(panel).mapIndexedNotNull { index, (module, value) ->
            val y = top + index * ROW_HEIGHT - panel.scroll
            if (y + ROW_HEIGHT <= top || y >= bottom) null else GuiRow(panel.x, y, module, value)
        }
    }

    // ------------------------------------------------------------------ render

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)

        for (panel in ClickGuiState.panels) {
            panel.scroll = panel.scroll.coerceIn(0, maxScroll(panel))
            renderPanel(guiGraphics, panel, mouseX, mouseY)
        }

        renderSearch(guiGraphics, mouseX, mouseY)
        renderDropdown(guiGraphics, mouseX, mouseY)
        renderHeldDescription(guiGraphics, mouseX, mouseY)

        val binding = bindingModule?.configurableName ?: bindingValue?.name
        if (binding != null) {
            guiGraphics.drawString(
                font, "Press a key for '$binding' (ESC to clear)",
                MARGIN, height - 12, COLOR_ACCENT, true
            )
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    private fun renderPanel(guiGraphics: GuiGraphics, panel: ClickGuiPanel, mouseX: Int, mouseY: Int) {
        val x = panel.x
        val top = panel.y

        guiGraphics.fill(x, top, x + PANEL_WIDTH, top + HEADER_HEIGHT, COLOR_HEADER)
        guiGraphics.drawString(font, panel.category.readableName, x + 4, top + 3, COLOR_ACCENT, true)
        guiGraphics.drawString(
            font, if (panel.open) "-" else "+", x + PANEL_WIDTH - 10, top + 3, COLOR_TEXT, true
        )
        if (!panel.open) return

        val bodyTop = top + HEADER_HEIGHT
        val body = bodyHeight(panel)
        guiGraphics.fill(x, bodyTop, x + PANEL_WIDTH, bodyTop + body, COLOR_PANEL_BG)

        // Clip so a scrolled row cannot bleed past the panel.
        guiGraphics.enableScissor(x, bodyTop, x + PANEL_WIDTH, bodyTop + body)
        for (row in laidOut(panel)) {
            if (row.value == null) {
                renderModuleRow(guiGraphics, row, mouseX, mouseY)
            } else {
                renderValueRow(guiGraphics, row, row.value, mouseX, mouseY)
            }
        }
        guiGraphics.disableScissor()

        if (maxScroll(panel) > 0) {
            val trackHeight = body
            val thumbHeight = (trackHeight * body / contentHeight(panel)).coerceAtLeast(8)
            val thumbY = bodyTop + (trackHeight - thumbHeight) * panel.scroll / maxScroll(panel)
            guiGraphics.fill(
                x + PANEL_WIDTH - 2, thumbY, x + PANEL_WIDTH, thumbY + thumbHeight, COLOR_SCROLLBAR
            )
        }
    }

    private fun renderModuleRow(guiGraphics: GuiGraphics, row: GuiRow, mouseX: Int, mouseY: Int) {
        val module = row.module
        val hovered = row.contains(mouseX.toDouble(), mouseY.toDouble())
        if (hovered) {
            guiGraphics.fill(row.x, row.y, row.x + PANEL_WIDTH, row.y + ROW_HEIGHT, COLOR_ROW_HOVER)
            // Finding it with the mouse is what ends the search blink.
            if (blinkModule === module) blinkModule = null
        }
        if (blinkModule === module) {
            val phase = (System.currentTimeMillis() % 800L) / 800.0 * 2.0 * PI
            val alpha = ((sin(phase) * 0.5 + 0.5) * 150).toInt().coerceIn(0, 255)
            guiGraphics.fill(
                row.x, row.y, row.x + PANEL_WIDTH, row.y + ROW_HEIGHT, (alpha shl 24) or 0x55FFFF
            )
        }

        guiGraphics.drawString(
            font, module.configurableName, row.x + 4, row.y + 2,
            if (module.enabled) COLOR_TEXT_ON else COLOR_TEXT, true
        )

        val bindText = when {
            bindingModule === module -> "..."
            module.bind != GLFW.GLFW_KEY_UNKNOWN -> keyName(module.bind)
            hovered -> "bind"
            else -> null
        }
        if (bindText != null) {
            guiGraphics.drawString(
                font, bindText, row.x + PANEL_WIDTH - font.width(bindText) - 4, row.y + 2,
                if (bindingModule === module) COLOR_ACCENT else COLOR_TEXT_DIM, true
            )
        }
    }

    private fun renderValueRow(
        guiGraphics: GuiGraphics, row: GuiRow, value: Value<*>, mouseX: Int, mouseY: Int
    ) {
        if (row.contains(mouseX.toDouble(), mouseY.toDouble())) {
            guiGraphics.fill(row.x, row.y, row.x + PANEL_WIDTH, row.y + ROW_HEIGHT, COLOR_ROW_HOVER)
        }

        // Numeric options draw as a filled bar so the drag target is obvious.
        val fraction = sliderFraction(value)
        if (fraction != null) {
            val fillWidth = ((PANEL_WIDTH - 8) * fraction).roundToInt().coerceIn(0, PANEL_WIDTH - 8)
            guiGraphics.fill(
                row.x + 4, row.y + ROW_HEIGHT - 3, row.x + 4 + fillWidth, row.y + ROW_HEIGHT - 1,
                COLOR_SLIDER_FILL
            )
        }

        val label = "  ${value.name}"
        val shown = displayValue(value) + if (editingText === value) "_" else ""
        guiGraphics.drawString(font, label, row.x + 4, row.y + 2, COLOR_TEXT_DIM, true)
        guiGraphics.drawString(
            font, shown, row.x + PANEL_WIDTH - font.width(shown) - 4, row.y + 2,
            if (editingText === value || bindingValue === value) COLOR_ACCENT else COLOR_TEXT, true
        )
    }

    private fun renderSearch(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val x = width / 2 - SEARCH_WIDTH / 2
        val y = MARGIN

        guiGraphics.fill(x, y, x + SEARCH_WIDTH, y + SEARCH_HEIGHT, COLOR_HEADER)
        val text = when {
            searchQuery.isNotEmpty() -> searchQuery + if (searchFocused) "_" else ""
            searchFocused -> "_"
            else -> "Search..."
        }
        guiGraphics.drawString(
            font, text, x + 4, y + 3,
            if (searchQuery.isEmpty() && !searchFocused) COLOR_TEXT_DIM else COLOR_TEXT, true
        )

        val results = searchResults()
        if (results.isEmpty()) return

        var rowY = y + SEARCH_HEIGHT
        guiGraphics.fill(x, rowY, x + SEARCH_WIDTH, rowY + results.size * ROW_HEIGHT, COLOR_TOOLTIP_BG)
        for (module in results) {
            if (mouseX >= x && mouseX < x + SEARCH_WIDTH && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                guiGraphics.fill(x, rowY, x + SEARCH_WIDTH, rowY + ROW_HEIGHT, COLOR_ROW_HOVER)
            }
            guiGraphics.drawString(font, module.configurableName, x + 4, rowY + 2, COLOR_TEXT, true)
            val category = module.category.readableName
            guiGraphics.drawString(
                font, category, x + SEARCH_WIDTH - font.width(category) - 4, rowY + 2, COLOR_TEXT_DIM, true
            )
            rowY += ROW_HEIGHT
        }
    }

    private fun searchResults(): List<Module> {
        if (searchQuery.isBlank()) return emptyList()
        return ModuleManager
            .filter { it.configurableName.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.configurableName.lowercase().indexOf(searchQuery.lowercase()) }
            .take(SEARCH_RESULTS)
    }

    private fun renderHeldDescription(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val module = pressedModule ?: return
        if (System.currentTimeMillis() - pressedAt < HOLD_MILLIS) return

        val lines = font.split(Component.literal(module.description), 170)
        if (lines.isEmpty()) return

        val boxWidth = (lines.maxOf { font.width(it) } + 8).coerceAtMost(186)
        val boxHeight = lines.size * 10 + 6
        val boxX = (mouseX + 8).coerceAtMost(width - boxWidth - 2)
        val boxY = (mouseY + 8).coerceAtMost(height - boxHeight - 2)

        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, COLOR_TOOLTIP_BG)
        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, COLOR_ACCENT)
        lines.forEachIndexed { index, line ->
            guiGraphics.drawString(font, line, boxX + 4, boxY + 4 + index * 10, COLOR_TEXT, true)
        }
    }

    private fun dropdownRows(value: ListValue): List<String> =
        value.choices.drop(dropdownScroll).take(DROPDOWN_ROWS)

    private fun renderDropdown(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val value = dropdown ?: return
        val rows = dropdownRows(value)
        val boxHeight = rows.size * ROW_HEIGHT + 2

        guiGraphics.fill(dropdownX, dropdownY, dropdownX + PANEL_WIDTH, dropdownY + boxHeight, COLOR_TOOLTIP_BG)
        var y = dropdownY + 1
        for (choice in rows) {
            if (inside(mouseX.toDouble(), mouseY.toDouble(), dropdownX, y, PANEL_WIDTH, ROW_HEIGHT)) {
                guiGraphics.fill(dropdownX, y, dropdownX + PANEL_WIDTH, y + ROW_HEIGHT, COLOR_ROW_HOVER)
            }
            guiGraphics.drawString(
                font, choice, dropdownX + 3, y + 2,
                if (choice == value.value) COLOR_ACCENT else COLOR_TEXT, true
            )
            y += ROW_HEIGHT
        }

        if (value.choices.size > DROPDOWN_ROWS) {
            guiGraphics.drawString(
                font, "scroll (${dropdownScroll + 1}/${value.choices.size})",
                dropdownX + 3, dropdownY + boxHeight + 1, COLOR_TEXT_DIM, true
            )
        }
    }

    // ------------------------------------------------------------------ input

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (dropdown != null) {
            handleDropdownClick(mouseX, mouseY)
            return true
        }
        if (handleSearchClick(mouseX, mouseY)) {
            return true
        }

        for (panel in ClickGuiState.panels) {
            if (inside(mouseX, mouseY, panel.x, panel.y, PANEL_WIDTH, HEADER_HEIGHT)) {
                if (button == 0) {
                    panel.dragging = true
                    panel.dragOffsetX = mouseX.toInt() - panel.x
                    panel.dragOffsetY = mouseY.toInt() - panel.y
                } else if (button == 1) {
                    panel.open = !panel.open
                }
                return true
            }

            for (row in laidOut(panel)) {
                if (!row.contains(mouseX, mouseY)) continue
                if (row.value == null) {
                    onModulePressed(row, mouseX, button)
                } else {
                    onValuePressed(row, row.value, mouseX, button)
                }
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun onModulePressed(row: GuiRow, mouseX: Double, button: Int) {
        val module = row.module
        when (button) {
            0 -> {
                // The right-hand strip starts a key binding; the rest toggles — but only on
                // release, so that holding the row can show the description instead.
                if (mouseX >= row.x + PANEL_WIDTH - BIND_ZONE_WIDTH) {
                    bindingModule = module
                } else {
                    pressedModule = module
                    pressedAt = System.currentTimeMillis()
                }
            }
            1 -> toggleExpanded(module)
            2 -> bindingModule = module
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onValuePressed(row: GuiRow, value: Value<*>, mouseX: Double, button: Int) {
        when (value.type) {
            ValueType.BOOLEAN -> (value as Value<Boolean>).set(!value.value)
            ValueType.INT, ValueType.FLOAT -> {
                sliderValue = value
                sliderX = row.x
                applySlider(value, mouseX)
            }
            ValueType.ENUM -> {
                val enumValue = value as EnumValue<*>
                val choices: Array<out Enum<*>> = enumValue.choices
                if (choices.isNotEmpty()) {
                    val current = choices.indexOfFirst { it == enumValue.value }
                    val step = if (button == 1) -1 else 1
                    (value as Value<Any>).set(choices[((current + step) + choices.size) % choices.size])
                }
            }
            ValueType.LIST -> {
                val list = value as ListValue
                dropdown = list
                dropdownX = row.x
                dropdownY = row.y + ROW_HEIGHT
                dropdownScroll = list.choices.indexOf(list.value)
                    .coerceAtLeast(0)
                    .coerceAtMost((list.choices.size - DROPDOWN_ROWS).coerceAtLeast(0))
            }
            ValueType.KEY -> {
                if (button == 1) (value as Value<Int>).set(GLFW.GLFW_KEY_UNKNOWN)
                else bindingValue = value as Value<Int>
            }
            ValueType.TEXT -> editingText = value as Value<String>
            else -> {}
        }
    }

    /** Returns true when the click landed on the search bar or one of its results. */
    private fun handleSearchClick(mouseX: Double, mouseY: Double): Boolean {
        val x = width / 2 - SEARCH_WIDTH / 2
        val y = MARGIN

        if (inside(mouseX, mouseY, x, y, SEARCH_WIDTH, SEARCH_HEIGHT)) {
            searchFocused = true
            return true
        }

        var rowY = y + SEARCH_HEIGHT
        for (module in searchResults()) {
            if (inside(mouseX, mouseY, x, rowY, SEARCH_WIDTH, ROW_HEIGHT)) {
                revealModule(module)
                return true
            }
            rowY += ROW_HEIGHT
        }

        searchFocused = false
        return false
    }

    /** Opens the module's category, scrolls it into view and blinks it until the mouse arrives. */
    private fun revealModule(module: Module) {
        val panel = ClickGuiState.panels.firstOrNull { it.category == module.category } ?: return
        panel.open = true
        val index = rowsOf(panel).indexOfFirst { it.first === module && it.second == null }
        if (index >= 0) {
            panel.scroll = (index * ROW_HEIGHT - MAX_BODY_HEIGHT / 2 + ROW_HEIGHT)
                .coerceIn(0, maxScroll(panel))
        }
        blinkModule = module
        searchQuery = ""
        searchFocused = false
    }

    private fun handleDropdownClick(mouseX: Double, mouseY: Double) {
        val value = dropdown ?: return
        var y = dropdownY + 1
        for (choice in dropdownRows(value)) {
            if (inside(mouseX, mouseY, dropdownX, y, PANEL_WIDTH, ROW_HEIGHT)) {
                value.set(choice)
                dropdown = null
                return
            }
            y += ROW_HEIGHT
        }
        dropdown = null
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        ClickGuiState.panels.forEach { it.dragging = false }
        sliderValue = null

        val module = pressedModule
        if (module != null) {
            // A short press toggles; a long one was a request for the description. Releasing
            // away from the row cancels, the way a button does.
            val stillOnRow = ClickGuiState.panels.any { panel ->
                laidOut(panel).any { it.value == null && it.module === module && it.contains(mouseX, mouseY) }
            }
            if (stillOnRow && System.currentTimeMillis() - pressedAt < HOLD_MILLIS) {
                module.enabled = !module.enabled
            }
            pressedModule = null
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(
        mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double
    ): Boolean {
        sliderValue?.let {
            applySlider(it, mouseX)
            return true
        }
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
        dropdown?.let { value ->
            val maxDropdownScroll = (value.choices.size - DROPDOWN_ROWS).coerceAtLeast(0)
            dropdownScroll = (dropdownScroll - delta.toInt()).coerceIn(0, maxDropdownScroll)
            return true
        }

        for (panel in ClickGuiState.panels) {
            if (!panel.open) continue
            val bodyTop = panel.y + HEADER_HEIGHT
            if (!inside(mouseX, mouseY, panel.x, bodyTop, PANEL_WIDTH, bodyHeight(panel))) continue
            panel.scroll = (panel.scroll - delta.toInt() * ROW_HEIGHT).coerceIn(0, maxScroll(panel))
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, delta)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        bindingModule?.let { module ->
            module.bind = if (keyCode == GLFW.GLFW_KEY_ESCAPE) GLFW.GLFW_KEY_UNKNOWN else keyCode
            bindingModule = null
            return true
        }
        bindingValue?.let { value ->
            value.set(if (keyCode == GLFW.GLFW_KEY_ESCAPE) GLFW.GLFW_KEY_UNKNOWN else keyCode)
            bindingValue = null
            return true
        }
        editingText?.let { value ->
            when (keyCode) {
                GLFW.GLFW_KEY_BACKSPACE -> value.set(value.value.dropLast(1))
                GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_ESCAPE -> editingText = null
            }
            return true
        }
        if (searchFocused) {
            when (keyCode) {
                GLFW.GLFW_KEY_BACKSPACE -> searchQuery = searchQuery.dropLast(1)
                GLFW.GLFW_KEY_ENTER -> searchResults().firstOrNull()?.let(::revealModule)
                GLFW.GLFW_KEY_ESCAPE -> {
                    searchQuery = ""
                    searchFocused = false
                }
            }
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        editingText?.let {
            it.set(it.value + codePoint)
            return true
        }
        if (searchFocused) {
            searchQuery += codePoint
            return true
        }
        return super.charTyped(codePoint, modifiers)
    }

    override fun onClose() {
        // Reset the momentary ClickGui module so the next Right Shift re-opens the GUI.
        ModuleClickGui.enabled = false
        ConfigSystem.save()
        super.onClose()
    }

    // ---------------------------------------------------------------- helpers

    private fun toggleExpanded(module: Module) {
        val panel = ClickGuiState.panels.firstOrNull { it.category == module.category } ?: return
        val name = module.configurableName
        if (!panel.expanded.remove(name)) {
            panel.expanded.add(name)
        }
    }

    /** Visible option rows of a module (skips the internal "Enabled" value). */
    private fun optionRows(module: Module): List<Value<*>> =
        module.innerValues.filter { it.isOption && it !is Configurable && it !is ChoiceConfigurable }

    private fun inside(mouseX: Double, mouseY: Double, x: Int, y: Int, w: Int, h: Int): Boolean =
        mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h

    /** 0..1 position of a numeric value inside its range, or null if it is not numeric. */
    private fun sliderFraction(value: Value<*>): Double? {
        val ranged = value as? RangedValue<*> ?: return null
        val span = ranged.range.endInclusive - ranged.range.start
        if (span <= 0.0) return null
        return (((value.value as Number).toDouble() - ranged.range.start) / span).coerceIn(0.0, 1.0)
    }

    @Suppress("UNCHECKED_CAST")
    private fun applySlider(value: Value<*>, mouseX: Double) {
        val ranged = value as? RangedValue<*> ?: return
        val fraction = ((mouseX - (sliderX + 4)) / (PANEL_WIDTH - 8)).coerceIn(0.0, 1.0)
        val raw = ranged.range.start + fraction * (ranged.range.endInclusive - ranged.range.start)
        when (value.type) {
            ValueType.INT -> (value as Value<Int>).set(raw.roundToInt())
            // Two decimals: fine enough to tune, coarse enough to hit a round number.
            ValueType.FLOAT -> (value as Value<Float>).set((raw * 100.0).roundToInt() / 100.0f)
            else -> {}
        }
    }

    private fun displayValue(value: Value<*>): String = when (value.type) {
        ValueType.BOOLEAN -> if (value.value as Boolean) "ON" else "OFF"
        ValueType.FLOAT -> String.format("%.2f", value.value as Float)
        ValueType.ENUM -> (value.value as Enum<*>).name
        ValueType.KEY -> if (bindingValue === value) "..." else keyName(value.value as Int)
        ValueType.LIST -> value.value as String
        ValueType.INT_RANGE -> (value.value as IntRange).let { "${it.first}-${it.last}" }
        else -> value.value.toString()
    }
}

/** Small, dependency-free key name formatter. */
@Suppress("CyclomaticComplexMethod")
fun keyName(code: Int): String = when {
    code == GLFW.GLFW_KEY_UNKNOWN -> "NONE"
    code >= GLFW.GLFW_KEY_A && code <= GLFW.GLFW_KEY_Z -> ('A' + (code - GLFW.GLFW_KEY_A)).toString()
    code >= GLFW.GLFW_KEY_0 && code <= GLFW.GLFW_KEY_9 -> ('0' + (code - GLFW.GLFW_KEY_0)).toString()
    code >= GLFW.GLFW_KEY_F1 && code <= GLFW.GLFW_KEY_F12 -> "F${code - GLFW.GLFW_KEY_F1 + 1}"
    code >= GLFW.GLFW_KEY_KP_0 && code <= GLFW.GLFW_KEY_KP_9 -> "NUM${code - GLFW.GLFW_KEY_KP_0}"
    code == GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT"
    code == GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT"
    code == GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL"
    code == GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL"
    code == GLFW.GLFW_KEY_LEFT_ALT -> "LALT"
    code == GLFW.GLFW_KEY_RIGHT_ALT -> "RALT"
    code == GLFW.GLFW_KEY_TAB -> "TAB"
    code == GLFW.GLFW_KEY_SPACE -> "SPACE"
    code == GLFW.GLFW_KEY_ENTER -> "ENTER"
    code == GLFW.GLFW_KEY_INSERT -> "INSERT"
    code == GLFW.GLFW_KEY_DELETE -> "DELETE"
    code == GLFW.GLFW_KEY_HOME -> "HOME"
    code == GLFW.GLFW_KEY_END -> "END"
    code == GLFW.GLFW_KEY_PAGE_UP -> "PGUP"
    code == GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN"
    code == GLFW.GLFW_KEY_UP -> "UP"
    code == GLFW.GLFW_KEY_DOWN -> "DOWN"
    code == GLFW.GLFW_KEY_LEFT -> "LEFT"
    code == GLFW.GLFW_KEY_RIGHT -> "RIGHT"
    else -> "#$code"
}
