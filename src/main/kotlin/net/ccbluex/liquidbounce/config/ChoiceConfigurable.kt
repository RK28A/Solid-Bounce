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
package net.ccbluex.liquidbounce.config

import net.ccbluex.liquidbounce.event.Listenable

/**
 * A single selectable mode of a [ChoiceConfigurable].
 */
abstract class Choice(name: String) : Configurable(name), Listenable {

    abstract val parent: ChoiceConfigurable

    open val isActive: Boolean
        get() = parent.activeChoice === this

    override fun parent(): Listenable = parent

    override fun handleEvents(): Boolean = isActive && parent.handleEvents()

    open fun enable() {}
    open fun disable() {}
}

/**
 * A configurable that lets the user pick one [Choice] out of several (module "modes").
 */
class ChoiceConfigurable(
    private val listenable: Listenable,
    name: String,
    activeInit: (ChoiceConfigurable) -> Choice,
    choicesInit: (ChoiceConfigurable) -> Array<Choice>
) : Configurable(name), Listenable {

    val choices: Array<Choice> = choicesInit(this)

    var activeChoice: Choice = activeInit(this)
        set(newChoice) {
            if (field === newChoice) return
            if (parentHandlesEvents()) field.disable()
            field = newChoice
            if (parentHandlesEvents()) newChoice.enable()
        }

    init {
        choices.forEach { innerValues += it }
    }

    private fun parentHandlesEvents() = listenable.handleEvents()

    override fun parent(): Listenable = listenable

    override fun handleEvents(): Boolean = listenable.handleEvents()

    override fun children(): List<Listenable> = choices.toList()

    /** Called by a module when it toggles, to (de)activate the current choice. */
    fun newState(state: Boolean) {
        if (state) activeChoice.enable() else activeChoice.disable()
    }

    fun setByName(name: String): Boolean {
        val choice = choices.firstOrNull { it.configurableName.equals(name, ignoreCase = true) } ?: return false
        activeChoice = choice
        return true
    }
}
