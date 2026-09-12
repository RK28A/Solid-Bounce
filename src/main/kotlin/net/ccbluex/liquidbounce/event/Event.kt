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
package net.ccbluex.liquidbounce.event

/**
 * A callable event.
 */
open class Event

/**
 * A cancellable event.
 */
open class CancellableEvent : Event() {

    var isCancelled: Boolean = false
        private set

    fun cancelEvent() {
        isCancelled = true
    }
}

/**
 * State of an event. Might be PRE or POST.
 */
enum class EventState(val stateName: String) {
    PRE("PRE"), POST("POST")
}
