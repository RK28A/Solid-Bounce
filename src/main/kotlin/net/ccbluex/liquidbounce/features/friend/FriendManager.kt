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
package net.ccbluex.liquidbounce.features.friend

import net.ccbluex.liquidbounce.utils.client.logger
import net.ccbluex.liquidbounce.utils.client.mc
import java.io.File

/** Persistent friend list, stored next to the module config. */
object FriendManager {

    private val friends = linkedSetOf<String>()
    private var loaded = false

    private val file: File
        get() = File(File(mc.gameDirectory, "solidbounce").also { it.mkdirs() }, "friends.txt")

    private fun ensureLoaded() {
        if (loaded) return
        loaded = true
        runCatching {
            if (file.exists()) {
                friends += file.readLines().map { it.trim() }.filter { it.isNotEmpty() }
            }
        }.onFailure { logger.error("Could not read friends.txt", it) }
    }

    private fun save() {
        runCatching { file.writeText(friends.joinToString("\n")) }
            .onFailure { logger.error("Could not write friends.txt", it) }
    }

    fun isFriend(name: String): Boolean {
        ensureLoaded()
        return friends.any { it.equals(name, ignoreCase = true) }
    }

    /** Adds the name if absent, removes it otherwise. Returns true when it is now a friend. */
    fun toggle(name: String): Boolean {
        ensureLoaded()
        val existing = friends.firstOrNull { it.equals(name, ignoreCase = true) }
        val added = if (existing != null) {
            friends.remove(existing)
            false
        } else {
            friends.add(name)
            true
        }
        save()
        return added
    }

    fun all(): List<String> {
        ensureLoaded()
        return friends.toList()
    }
}
