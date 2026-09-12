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

import net.ccbluex.liquidbounce.utils.client.logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A modern and fast event handler using lambda handlers.
 *
 * Unlike upstream LiquidBounce (which pre-registers every event class in a fixed array),
 * this port lazily creates the handler list per event type on first use. Functionally
 * identical for handlers, and avoids having to maintain a giant lookup table while porting.
 */
object EventManager {

    private val registry: MutableMap<Class<out Event>, CopyOnWriteArrayList<EventHook<in Event>>> =
        ConcurrentHashMap()

    init {
        // Touch SequenceManager so the sequence tick handler is registered.
        SequenceManager
    }

    private fun listFor(eventClass: Class<out Event>): CopyOnWriteArrayList<EventHook<in Event>> =
        registry.getOrPut(eventClass) { CopyOnWriteArrayList() }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> registerEventHook(eventClass: Class<out Event>, eventHook: EventHook<T>) {
        val handlers = listFor(eventClass)
        val hook = eventHook as EventHook<in Event>
        if (!handlers.contains(hook)) {
            handlers.add(hook)
            handlers.sortByDescending { it.priority }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> unregisterEventHook(eventClass: Class<out Event>, eventHook: EventHook<T>) {
        registry[eventClass]?.remove(eventHook as EventHook<in Event>)
    }

    fun unregisterEventHandler(eventHandler: Listenable) {
        registry.values.forEach { list ->
            list.removeIf { it.handlerClass == eventHandler }
        }
    }

    /**
     * Call an event to its listeners.
     */
    fun <T : Event> callEvent(event: T): T {
        val target = registry[event.javaClass] ?: return event

        for (eventHook in target) {
            if (!eventHook.ignoresCondition && !eventHook.handlerClass.handleEvents()) {
                continue
            }

            runCatching {
                eventHook.handler(event)
            }.onFailure {
                logger.error("Exception while executing handler for ${event.javaClass.simpleName}.", it)
            }
        }

        return event
    }
}
