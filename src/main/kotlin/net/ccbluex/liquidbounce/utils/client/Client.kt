/*
 * This file is part of Solid-Bounce, a Forge 1.20.1 port of LiquidBounce.
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Original work Copyright (c) 2015 - 2024 CCBlueX (https://github.com/CCBlueX/LiquidBounce)
 * Forge port modifications Copyright (c) 2025 Solid-Bounce contributors.
 */
package net.ccbluex.liquidbounce.utils.client

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import org.slf4j.LoggerFactory

/**
 * Global Minecraft client instance. Kept as a top-level `val`-style accessor so ported
 * modules can reference [mc] exactly like they do in the upstream LiquidBounce sources.
 */
val mc: Minecraft
    get() = Minecraft.getInstance()

/**
 * Shared logger for the client.
 */
val logger: org.slf4j.Logger = LoggerFactory.getLogger("Solid-Bounce")

/**
 * True when the player is fully in a world (player and level both present).
 */
val inGame: Boolean
    get() = mc.player != null && mc.level != null

/**
 * Marks an [net.ccbluex.liquidbounce.event.Event] (or other object) with a stable name.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Nameable(val name: String)

/**
 * Client branding.
 */
object SolidBounceInfo {
    const val CLIENT_NAME = "Solid-Bounce"
    const val CLIENT_VERSION = "0.1.0"
    const val BASED_ON = "LiquidBounce nextgen v0.1.0"
    const val MC_VERSION = "1.20.1"
}

private const val PREFIX_BRACKET_COLOR = ChatFormatting.GRAY
private val clientPrefix: MutableComponent =
    Component.literal("[").withStyle(ChatFormatting.GRAY)
        .append(Component.literal(SolidBounceInfo.CLIENT_NAME).withStyle(ChatFormatting.AQUA))
        .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))

/**
 * Prints a prefixed client message to the in-game chat (client-side only).
 */
fun chat(message: Component) {
    val player = mc.player ?: return
    player.displayClientMessage(clientPrefix.copy().append(message), false)
}

fun chat(text: String) = chat(Component.literal(text))

fun notification(title: String, message: String) {
    chat(
        Component.literal(title).withStyle(ChatFormatting.AQUA)
            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(message).withStyle(ChatFormatting.WHITE))
    )
}

/**
 * Regular tick counter (in-game ticks) that ported timing logic can rely on.
 */
object ClientTicks {
    @Volatile
    var ticks: Long = 0L
        internal set
}
