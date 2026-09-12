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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.utils.client.logger
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias SuspendableHandler<T> = suspend Sequence<T>.(T) -> Unit

object SequenceManager : Listenable {

    internal val sequences = CopyOnWriteArrayList<Sequence<*>>()

    // Run before everything else so existing sequences tick before newly-added ones.
    val tickSequences = handler<GameTickEvent>(priority = 1000) {
        for (sequence in sequences) {
            if (!sequence.owner.handleEvents()) {
                sequence.cancel()
                continue
            }
            sequence.tick()
        }
    }
}

@OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
open class Sequence<T : Event>(val owner: Listenable, val handler: SuspendableHandler<T>, protected val event: T) {

    private var coroutine = GlobalScope.launch(Dispatchers.Unconfined) {
        SequenceManager.sequences += this@Sequence
        coroutineRun()
        SequenceManager.sequences -= this@Sequence
    }

    open fun cancel() {
        coroutine.cancel()
        SequenceManager.sequences -= this@Sequence
    }

    private var continuation: Continuation<Unit>? = null
    private var elapsedTicks = 0
    private var totalTicks: () -> Int = { 0 }

    internal open suspend fun coroutineRun() {
        if (owner.handleEvents()) {
            runCatching {
                handler(event)
            }.onFailure {
                logger.error("Exception occurred during subroutine", it)
            }
        }
    }

    internal fun tick() {
        if (++this.elapsedTicks >= this.totalTicks()) {
            this.continuation?.resume(Unit)
        }
    }

    suspend fun waitUntil(case: () -> Boolean) {
        while (!case()) {
            sync()
        }
    }

    suspend fun waitConditional(ticks: Int, breakLoop: () -> Boolean = { false }): Boolean {
        if (ticks == 0) {
            return true
        }
        wait { if (breakLoop()) 0 else ticks }
        return elapsedTicks >= ticks
    }

    suspend fun waitTicks(ticks: Int) {
        if (ticks == 0) {
            return
        }
        this.wait { ticks }
    }

    suspend fun waitSeconds(seconds: Int) {
        this.wait { seconds * 20 }
    }

    private suspend fun wait(ticksToWait: () -> Int) {
        elapsedTicks = 0
        totalTicks = ticksToWait
        suspendCoroutine { continuation = it }
    }

    internal suspend fun sync() = wait { 0 }
}

class DummyEvent : Event()

class RepeatingSequence(owner: Listenable, handler: SuspendableHandler<DummyEvent>) :
    Sequence<DummyEvent>(owner, handler, DummyEvent()) {

    private var continueLoop = true

    override suspend fun coroutineRun() {
        sync()
        while (continueLoop && owner.handleEvents()) {
            super.coroutineRun()
            sync()
        }
    }

    override fun cancel() {
        continueLoop = false
    }
}
