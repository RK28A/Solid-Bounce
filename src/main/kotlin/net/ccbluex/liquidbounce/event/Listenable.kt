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

import net.ccbluex.liquidbounce.event.events.GameTickEvent

typealias Handler<T> = (T) -> Unit

class EventHook<T : Event>(
    val handlerClass: Listenable,
    val handler: Handler<T>,
    val ignoresCondition: Boolean,
    val priority: Int = 0
)

interface Listenable {

    /**
     * Allows disabling event handling when the condition is false.
     */
    fun handleEvents(): Boolean = parent()?.handleEvents() ?: true

    fun parent(): Listenable? = null

    fun children(): List<Listenable> = emptyList()

    fun unregister() {
        EventManager.unregisterEventHandler(this)
        for (child in children()) {
            child.unregister()
        }
    }
}

inline fun <reified T : Event> Listenable.handler(
    ignoreCondition: Boolean = false,
    priority: Int = 0,
    noinline handler: Handler<T>
) {
    EventManager.registerEventHook(T::class.java, EventHook(this, handler, ignoreCondition, priority))
}

/**
 * Registers an event hook for events of type [T] and launches a coroutine-like sequence.
 */
inline fun <reified T : Event> Listenable.sequenceHandler(
    ignoreCondition: Boolean = false,
    priority: Int = 0,
    noinline eventHandler: SuspendableHandler<T>
) {
    handler<T>(ignoreCondition, priority) { event -> Sequence(this, eventHandler, event) }
}

/**
 * Registers a repeatable sequence which repeats the execution of code while events are handled.
 */
fun Listenable.repeatable(eventHandler: SuspendableHandler<DummyEvent>) {
    var sequence: RepeatingSequence? = null

    handler<GameTickEvent>(ignoreCondition = true) {
        if (this.handleEvents()) {
            if (sequence == null) {
                sequence = RepeatingSequence(this, eventHandler)
            }
        } else if (sequence != null) {
            sequence?.cancel()
            sequence = null
        }
    }
}
