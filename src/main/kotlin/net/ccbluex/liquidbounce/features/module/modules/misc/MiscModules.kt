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
 * Remaining Misc modules (scaffold: register/toggle/options; deep behavior pending noted util/mixin).
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/** AntiBot — filters out likely bot players from targeting. TODO: heuristics + tab-list analysis. */
object ModuleAntiBot : Module("AntiBot", Category.MISC)

/** AutoAccount — account automation helper. TODO: account manager integration. */
object ModuleAutoAccount : Module("AutoAccount", Category.MISC)

/** AutoChatGame — answers chat mini-games automatically. TODO: chat parsing. */
object ModuleAutoChatGame : Module("AutoChatGame", Category.MISC)

/** AutoConfig — auto-applies a config based on the server. TODO: config system + server detect. */
object ModuleAutoConfig : Module("AutoConfig", Category.MISC)

/** CapeTransfer — cosmetic cape handling. TODO: cape provider integration. */
object ModuleCapeTransfer : Module("CapeTransfer", Category.MISC)

/** ClickRecorder — records click patterns for analysis. TODO: input recorder. */
object ModuleClickRecorder : Module("ClickRecorder", Category.MISC)

/** DebugRecorder — records debug data. TODO: recorder + export. */
object ModuleDebugRecorder : Module("DebugRecorder", Category.MISC)

/** Focus — reduces effects/animations while unfocused. TODO: window focus hook. */
object ModuleFocus : Module("Focus", Category.MISC)

/** FriendClicker — helper for adding friends by clicking. TODO: friend manager + screen hook. */
object ModuleFriendClicker : Module("FriendClicker", Category.MISC)

/** HideClient — hides client-identifying signals. TODO: packet/brand spoof. */
object ModuleHideClient : Module("HideClient", Category.MISC)

/** KeepChatAfterDeath — keeps chat history after respawning. TODO: MixinChatHud. */
object ModuleKeepChatAfterDeath : Module("KeepChatAfterDeath", Category.MISC)

/** NameProtect — censors your name in chat/HUD. TODO: MixinChatHud/text transform. */
object ModuleNameProtect : Module("NameProtect", Category.MISC)

/** Notifier — shows notifications for game events. TODO: event → notification bridge. */
object ModuleNotifier : Module("Notifier", Category.MISC)

/** Teams — reads/uses team info. TODO: scoreboard/team analysis. */
object ModuleTeams : Module("Teams", Category.MISC)
