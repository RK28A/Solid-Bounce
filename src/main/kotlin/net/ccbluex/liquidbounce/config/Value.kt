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
 * Note: this is a compact, API-compatible reimplementation of LiquidBounce's value system.
 * It keeps the module-facing declaration style (boolean(...), int(...), .listen { }, `by`)
 * so ported modules need minimal changes. JSON persistence is added in a later step.
 */
package net.ccbluex.liquidbounce.config

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

enum class ValueType {
    BOOLEAN, INT, FLOAT, INT_RANGE, FLOAT_RANGE, TEXT, KEY, ENUM, CHOICE, CONFIGURABLE
}

/**
 * A named, typed, listenable configuration value that also acts as a Kotlin property delegate.
 */
open class Value<T>(
    val name: String,
    value: T,
    val type: ValueType
) : ReadWriteProperty<Any?, T> {

    open var value: T = value
        set(newValue) {
            val processed = changeListener?.invoke(newValue) ?: newValue
            field = processed
        }

    private var changeListener: ((T) -> T)? = null

    /** Whether this value is written to the config file. */
    var included: Boolean = true
        private set

    /** Whether this value is shown as an option in the GUI. */
    var isOption: Boolean = true
        private set

    fun listen(listener: (T) -> T): Value<T> {
        this.changeListener = listener
        return this
    }

    fun onChange(listener: (T) -> Unit): Value<T> {
        this.changeListener = { it.also(listener) }
        return this
    }

    fun doNotInclude(): Value<T> {
        included = false
        return this
    }

    fun notAnOption(): Value<T> {
        isOption = false
        return this
    }

    fun set(newValue: T) {
        this.value = newValue
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        set(value)
    }
}

/**
 * A numeric value clamped to [range].
 *
 * Clamping is installed as a change-listener in [init] (after fields are set) instead of by
 * overriding the value setter, to avoid the "leaking `this` during construction" pitfall where
 * an overridden open setter would run before subclass fields are initialized.
 */
class RangedValue<T>(
    name: String,
    value: T,
    val range: ClosedRange<Double>,
    type: ValueType
) : Value<T>(name, value, type) where T : Number, T : Comparable<T> {

    init {
        listen { clamp(it) }
        // Clamp the initial value now that the listener is installed.
        set(value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun clamp(newValue: T): T {
        val clamped = newValue.toDouble().coerceIn(range.start, range.endInclusive)
        return when (value) {
            is Int -> clamped.toInt() as T
            is Long -> clamped.toLong() as T
            is Float -> clamped.toFloat() as T
            is Double -> clamped as T
            else -> newValue
        }
    }
}
