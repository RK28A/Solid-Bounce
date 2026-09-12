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

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.events.ToggleModuleEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.utils.client.logger
import net.ccbluex.liquidbounce.utils.client.mc
import java.io.File

/**
 * Persists module state (enabled, bind, hidden) and all option values to JSON, and restores
 * them on startup — the same responsibility as upstream LiquidBounce's ConfigSystem, using a
 * compact per-module JSON layout.
 */
object ConfigSystem : Listenable {

    private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    private val directory: File by lazy { File(mc.gameDirectory, "solidbounce").also { it.mkdirs() } }
    private val modulesFile: File get() = File(directory, "modules.json")

    /** True while a config is being applied (suppresses toggle notifications/side effects). */
    @Volatile
    var loading: Boolean = false
        private set

    @Suppress("unused")
    val autoSave = handler<ToggleModuleEvent>(ignoreCondition = true) {
        if (!loading) {
            save()
        }
    }

    fun load() {
        val file = modulesFile
        if (!file.exists()) {
            return
        }
        loading = true
        try {
            val root = gson.fromJson(file.readText(), JsonObject::class.java) ?: return
            for (module in ModuleManager) {
                val obj = root.getAsJsonObject(module.configurableName) ?: continue
                applyConfigurable(module, obj)
            }
            logger.info("Loaded config for ${ModuleManager.count} modules.")
        } catch (e: Exception) {
            logger.error("Failed to load config.", e)
        } finally {
            loading = false
        }
    }

    fun save() {
        try {
            val root = JsonObject()
            for (module in ModuleManager) {
                root.add(module.configurableName, serializeConfigurable(module))
            }
            modulesFile.writeText(gson.toJson(root))
        } catch (e: Exception) {
            logger.error("Failed to save config.", e)
        }
    }

    private fun serializeConfigurable(configurable: Configurable): JsonObject {
        val obj = JsonObject()
        for (value in configurable.innerValues) {
            when (value) {
                is ChoiceConfigurable -> obj.addProperty(value.configurableName, value.activeChoice.configurableName)
                is Configurable -> obj.add(value.configurableName, serializeConfigurable(value))
                else -> serializeValue(obj, value)
            }
        }
        return obj
    }

    private fun serializeValue(obj: JsonObject, value: Value<*>) {
        val name = value.name
        when (value.type) {
            ValueType.BOOLEAN -> obj.addProperty(name, value.value as Boolean)
            ValueType.INT, ValueType.KEY -> obj.addProperty(name, value.value as Int)
            ValueType.FLOAT -> obj.addProperty(name, value.value as Float)
            ValueType.TEXT, ValueType.LIST -> obj.addProperty(name, value.value as String)
            ValueType.ENUM -> obj.addProperty(name, (value.value as Enum<*>).name)
            ValueType.INT_RANGE -> {
                val range = value.value as IntRange
                val o = JsonObject()
                o.addProperty("min", range.first)
                o.addProperty("max", range.last)
                obj.add(name, o)
            }
            ValueType.FLOAT_RANGE -> {
                @Suppress("UNCHECKED_CAST")
                val range = value.value as ClosedFloatingPointRange<Float>
                val o = JsonObject()
                o.addProperty("min", range.start)
                o.addProperty("max", range.endInclusive)
                obj.add(name, o)
            }
            ValueType.CONFIGURABLE, ValueType.CHOICE -> {} // handled by serializeConfigurable
        }
    }

    private fun applyConfigurable(configurable: Configurable, obj: JsonObject) {
        for (value in configurable.innerValues) {
            val element = obj.get(value.name) ?: continue
            when (value) {
                is ChoiceConfigurable -> if (element.isJsonPrimitive) value.setByName(element.asString)
                is Configurable -> if (element.isJsonObject) applyConfigurable(value, element.asJsonObject)
                else -> applyValue(value, element)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun applyValue(value: Value<*>, element: JsonElement) {
        try {
            when (value.type) {
                ValueType.BOOLEAN -> (value as Value<Boolean>).set(element.asBoolean)
                ValueType.INT, ValueType.KEY -> (value as Value<Int>).set(element.asInt)
                ValueType.FLOAT -> (value as Value<Float>).set(element.asFloat)
                ValueType.TEXT, ValueType.LIST -> (value as Value<String>).set(element.asString)
                ValueType.ENUM -> {
                    val enumValue = value as EnumValue<*>
                    val name = element.asString
                    enumValue.choices.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let {
                        (value as Value<Any>).set(it)
                    }
                }
                ValueType.INT_RANGE -> {
                    val o = element.asJsonObject
                    (value as Value<IntRange>).set(o.get("min").asInt..o.get("max").asInt)
                }
                ValueType.FLOAT_RANGE -> {
                    val o = element.asJsonObject
                    (value as Value<ClosedFloatingPointRange<Float>>).set(o.get("min").asFloat..o.get("max").asFloat)
                }
                else -> {}
            }
        } catch (e: Exception) {
            logger.warn("Could not apply value '${value.name}': ${e.message}")
        }
    }
}
