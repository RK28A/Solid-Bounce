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
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.events.ChatReceiveEvent
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.MouseButtonEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.friend.FriendManager
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket
import net.minecraft.world.entity.player.Player
import org.lwjgl.glfw.GLFW

/** AntiBot — decides whether an entity is a fake player, so targeting can skip it. */
object ModuleAntiBot : Module("AntiBot", Category.MISC) {
    private val requireTabEntry by boolean("RequireTabEntry", true)

    /** Shared by the combat modules; always false while the module is off. */
    fun isBot(target: Player): Boolean {
        if (!enabled) return false
        val connection = mc.connection ?: return false

        if (requireTabEntry && connection.getPlayerInfo(target.uuid) == null) {
            return true
        }
        return false
    }
}

/** Teams — recognises teammates so they can be left alone. */
object ModuleTeams : Module("Teams", Category.MISC) {
    /** Shared by the combat modules; always false while the module is off. */
    fun isTeammate(target: Player): Boolean {
        if (!enabled) return false
        val self = mc.player ?: return false
        val ownTeam = self.team ?: return false
        return ownTeam.isAlliedTo(target.team)
    }
}

/** AutoChatGame — answers simple chat mini-games automatically. */
object ModuleAutoChatGame : Module("AutoChatGame", Category.MISC) {
    private val delayTicks by int("Delay", 10, 0..100)

    private val maths = Regex("""(\d+)\s*([+\-*x/])\s*(\d+)""")
    private val typeThis = Regex("""type\s+["“']?([A-Za-z0-9]{3,24})["”']?""", RegexOption.IGNORE_CASE)

    private var pending: String? = null
    private var countdown = 0

    @Suppress("unused")
    val onChat = handler<ChatReceiveEvent> { event ->
        if (pending != null) return@handler
        val message = event.message

        maths.find(message)?.let { match ->
            val a = match.groupValues[1].toLongOrNull() ?: return@let
            val b = match.groupValues[3].toLongOrNull() ?: return@let
            val answer = when (match.groupValues[2]) {
                "+" -> a + b
                "-" -> a - b
                "*", "x" -> a * b
                "/" -> if (b == 0L) return@let else a / b
                else -> return@let
            }
            pending = answer.toString()
            countdown = delayTicks
            return@handler
        }

        typeThis.find(message)?.let { match ->
            pending = match.groupValues[1]
            countdown = delayTicks
        }
    }

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val answer = pending ?: return@handler
        if (countdown-- > 0) return@handler

        pending = null
        mc.connection?.sendChat(answer)
    }

    override fun disable() {
        pending = null
        countdown = 0
    }
}

/** Focus — throttles the frame rate while the window is in the background. */
object ModuleFocus : Module("Focus", Category.MISC) {
    private val backgroundFps by int("BackgroundFps", 10, 1..60)

    private var savedLimit: Int? = null

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val options = mc.options
        if (!mc.isWindowActive) {
            if (savedLimit == null) {
                savedLimit = options.framerateLimit().get()
            }
            options.framerateLimit().set(backgroundFps)
        } else {
            savedLimit?.let {
                options.framerateLimit().set(it)
                savedLimit = null
            }
        }
    }

    override fun disable() {
        savedLimit?.let {
            mc.options.framerateLimit().set(it)
            savedLimit = null
        }
    }
}

/** FriendClicker — middle-click a player to add or remove them from the friend list. */
object ModuleFriendClicker : Module("FriendClicker", Category.MISC) {
    @Suppress("unused")
    val onMouse = handler<MouseButtonEvent> { event ->
        if (event.button != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.action != GLFW.GLFW_PRESS) {
            return@handler
        }
        val target = mc.crosshairPickEntity as? Player ?: return@handler
        val name = target.gameProfile.name ?: return@handler

        event.cancelEvent()
        val added = FriendManager.toggle(name)
        chat(if (added) "§aAdded §f$name §ato friends." else "§cRemoved §f$name §cfrom friends.")
    }
}

/** HideClient — stops the client from announcing its brand to the server. */
object ModuleHideClient : Module("HideClient", Category.MISC) {
    @Suppress("unused")
    val onPacket = handler<PacketEvent> { event ->
        if (event.origin != TransferOrigin.SEND) return@handler
        if (event.packet is ServerboundCustomPayloadPacket) {
            event.cancelEvent()
        }
    }
}

/** Notifier — warns about things worth reacting to (currently: low health). */
object ModuleNotifier : Module("Notifier", Category.MISC) {
    private val lowHealth by int("LowHealth", 6, 1..20)

    private var warned = false

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        val health = player.health
        if (health <= lowHealth && !warned) {
            warned = true
            chat("§cNotifier§7: low health (§f${health.toInt()}§7).")
        } else if (health > lowHealth) {
            warned = false
        }
    }

    override fun disable() {
        warned = false
    }
}

/** KeepChatAfterDeath — keeps the chat history on respawn. Driven by MixinChatComponent. */
object ModuleKeepChatAfterDeath : Module("KeepChatAfterDeath", Category.MISC)

// --------------------------------------------------------------------------------------------
// Still pending its dedicated hook.
// --------------------------------------------------------------------------------------------

/** NameProtect — censors your name in chat/HUD. TODO: chat component text transform. */
object ModuleNameProtect : Module("NameProtect", Category.MISC)

// --------------------------------------------------------------------------------------------
// These rely on CCBlueX's own backend (capes, accounts, config CDN, telemetry). Without that
// infrastructure there is nothing to port, so they stay registered but inert.
// --------------------------------------------------------------------------------------------

/** AutoAccount — needs the upstream account manager. */
object ModuleAutoAccount : Module("AutoAccount", Category.MISC)

/** AutoConfig — needs the upstream config CDN. */
object ModuleAutoConfig : Module("AutoConfig", Category.MISC)

/** CapeTransfer — needs the upstream cape service. */
object ModuleCapeTransfer : Module("CapeTransfer", Category.MISC)

/** ClickRecorder — needs the upstream recording backend. */
object ModuleClickRecorder : Module("ClickRecorder", Category.MISC)

/** DebugRecorder — needs the upstream recording backend. */
object ModuleDebugRecorder : Module("DebugRecorder", Category.MISC)
