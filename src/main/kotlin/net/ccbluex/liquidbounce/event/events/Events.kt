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
package net.ccbluex.liquidbounce.event.events

import com.mojang.blaze3d.vertex.PoseStack
import net.ccbluex.liquidbounce.event.CancellableEvent
import net.ccbluex.liquidbounce.event.Event
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.utils.client.Nameable
import net.minecraft.client.Camera
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.Entity

/**
 * Fired once per client tick (end phase). This is the primary timing event and the driver
 * for the [net.ccbluex.liquidbounce.event.Sequence] system.
 */
@Nameable("GameTick")
class GameTickEvent : Event()

/**
 * Fired every player tick.
 */
@Nameable("PlayerTick")
class PlayerTickEvent : Event()

/**
 * Fired while the world is being rendered. Use [matrixStack] for 3D overlays (ESP, tracers, ...).
 */
@Nameable("WorldRender")
class WorldRenderEvent(val matrixStack: PoseStack, val camera: Camera, val partialTicks: Float) : Event()

/**
 * Fired after the vanilla HUD is drawn. Use for the ArrayList / HUD rendering.
 */
@Nameable("OverlayRender")
class OverlayRenderEvent(val guiGraphics: GuiGraphics, val partialTicks: Float) : Event()

/**
 * Keyboard key event (press/release). [action] follows GLFW (0 = release, 1 = press, 2 = repeat).
 */
@Nameable("Key")
class KeyEvent(val key: Int, val scancode: Int, val action: Int, val modifiers: Int) : Event()

/**
 * Mouse button event (pre-vanilla-handling), cancellable.
 */
@Nameable("MouseButton")
class MouseButtonEvent(val button: Int, val action: Int, val modifiers: Int) : CancellableEvent()

/**
 * Fired when the local player attacks an entity. Cancellable.
 */
@Nameable("Attack")
class AttackEvent(val target: Entity) : CancellableEvent()

/**
 * Fired before a chat message is sent by the local player. Cancellable so command handlers
 * can intercept client commands.
 */
@Nameable("ChatSend")
class ChatSendEvent(val message: String) : CancellableEvent()

/**
 * Fired when the player joins / changes world (or disconnects, [world] == null).
 */
@Nameable("WorldChange")
class WorldChangeEvent(val world: net.minecraft.client.multiplayer.ClientLevel?) : Event()

/**
 * Fired once when the client finished starting.
 */
@Nameable("ClientStart")
class ClientStartEvent : Event()

/**
 * Fired when a module is toggled.
 */
@Nameable("ToggleModule")
class ToggleModuleEvent(val moduleName: String, val enabled: Boolean) : Event()

/**
 * Direction of a packet relative to the client.
 */
enum class TransferOrigin { SEND, RECEIVE }

/**
 * Fired for every packet passing through the network connection (see MixinConnection).
 * Cancellable — cancelling drops the packet.
 */
@Nameable("Packet")
class PacketEvent(
    val packet: net.minecraft.network.protocol.Packet<*>,
    val origin: TransferOrigin
) : CancellableEvent()
