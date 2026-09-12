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

/**
 * An enum-backed value that also remembers the available constants (for the GUI).
 */
class EnumValue<E : Enum<E>>(
    name: String,
    value: E,
    val choices: Array<E>
) : Value<E>(name, value, ValueType.ENUM)

/**
 * A string value picked from a dynamic set of options — rendered as a dropdown in the ClickGUI.
 * The options are supplied lazily because most of them come from game registries.
 */
class ListValue(
    name: String,
    value: String,
    private val choicesProvider: () -> List<String>
) : Value<String>(name, value, ValueType.LIST) {
    val choices: List<String> get() = choicesProvider()
}

/**
 * A container of [Value]s. Modules, choices and nested groups are all Configurables.
 */
open class Configurable(name: String) : Value<Configurable?>(name, null, ValueType.CONFIGURABLE) {

    val configurableName: String get() = name

    /** Child values (and nested configurables) declared on this container. */
    val innerValues = mutableListOf<Value<*>>()

    private fun <T : Value<*>> register(value: T): T {
        innerValues += value
        return value
    }

    fun boolean(name: String, default: Boolean): Value<Boolean> =
        register(Value(name, default, ValueType.BOOLEAN))

    fun int(name: String, default: Int, range: IntRange): RangedValue<Int> =
        register(RangedValue(name, default, range.first.toDouble()..range.last.toDouble(), ValueType.INT))

    fun float(name: String, default: Float, range: ClosedFloatingPointRange<Float>): RangedValue<Float> =
        register(RangedValue(name, default, range.start.toDouble()..range.endInclusive.toDouble(), ValueType.FLOAT))

    fun intRange(name: String, default: IntRange, @Suppress("UNUSED_PARAMETER") range: IntRange): Value<IntRange> =
        register(Value(name, default, ValueType.INT_RANGE))

    fun floatRange(
        name: String,
        default: ClosedFloatingPointRange<Float>,
        @Suppress("UNUSED_PARAMETER") range: ClosedFloatingPointRange<Float>
    ): Value<ClosedFloatingPointRange<Float>> =
        register(Value(name, default, ValueType.FLOAT_RANGE))

    fun text(name: String, default: String): Value<String> =
        register(Value(name, default, ValueType.TEXT))

    fun key(name: String, default: Int): Value<Int> =
        register(Value(name, default, ValueType.KEY))

    /** A dropdown of strings, options resolved lazily (e.g. from a registry). */
    fun list(name: String, default: String, choices: () -> List<String>): ListValue =
        register(ListValue(name, default, choices))

    inline fun <reified E : Enum<E>> enumChoice(name: String, default: E): EnumValue<E> =
        registerEnum(EnumValue(name, default, enumValues<E>()))

    // Exposed for the inline enumChoice above.
    fun <E : Enum<E>> registerEnum(value: EnumValue<E>): EnumValue<E> {
        innerValues += value
        return value
    }

    /** Adds a nested configurable group. */
    fun <T : Configurable> tree(configurable: T): T {
        innerValues += configurable
        return configurable
    }

    fun choices(
        listenable: net.ccbluex.liquidbounce.event.Listenable,
        name: String,
        active: Choice,
        choices: Array<Choice>
    ): ChoiceConfigurable {
        val cc = ChoiceConfigurable(listenable, name, { active }, { choices })
        innerValues += cc
        return cc
    }

    fun choices(
        listenable: net.ccbluex.liquidbounce.event.Listenable,
        name: String,
        activeCallback: (ChoiceConfigurable) -> Choice,
        choicesCallback: (ChoiceConfigurable) -> Array<Choice>
    ): ChoiceConfigurable {
        val cc = ChoiceConfigurable(listenable, name, activeCallback, choicesCallback)
        innerValues += cc
        return cc
    }
}
