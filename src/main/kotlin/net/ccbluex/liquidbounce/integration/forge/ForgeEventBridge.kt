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
package net.ccbluex.liquidbounce.integration.forge

import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.events.AttackEvent
import net.ccbluex.liquidbounce.event.events.ChatReceiveEvent
import net.ccbluex.liquidbounce.event.events.ChatSendEvent
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.KeyEvent
import net.ccbluex.liquidbounce.event.events.MouseButtonEvent
import net.ccbluex.liquidbounce.event.events.OverlayRenderEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.events.WorldRenderEvent
import net.ccbluex.liquidbounce.features.module.modules.player.ModuleNoFall
import net.ccbluex.liquidbounce.utils.client.ClientTicks
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.client.event.RenderGuiEvent
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.living.LivingFallEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

/**
 * Bridges Forge's event bus to Solid-Bounce's internal [EventManager].
 *
 * This is the core Fabric -> Forge glue: every place upstream LiquidBounce fired an event
 * from a Fabric hook / mixin, we translate the equivalent Forge event here. Events that Forge
 * does not expose natively (packets, movement internals, ...) are added later through mixins.
 */
object ForgeEventBridge {

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        ClientTicks.ticks++
        EventManager.callEvent(GameTickEvent())
    }

    @SubscribeEvent
    fun onRenderLevel(event: RenderLevelStageEvent) {
        if (event.stage != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return
        EventManager.callEvent(
            WorldRenderEvent(event.poseStack, mc.gameRenderer.mainCamera, event.partialTick)
        )
    }

    @SubscribeEvent
    fun onRenderGui(event: RenderGuiEvent.Post) {
        EventManager.callEvent(OverlayRenderEvent(event.guiGraphics, event.partialTick))
    }

    @SubscribeEvent
    fun onKey(event: InputEvent.Key) {
        EventManager.callEvent(KeyEvent(event.key, event.scanCode, event.action, event.modifiers))
    }

    @SubscribeEvent
    fun onMouseButton(event: InputEvent.MouseButton.Pre) {
        val result = EventManager.callEvent(MouseButtonEvent(event.button, event.action, event.modifiers))
        if (result.isCancelled) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onAttack(event: AttackEntityEvent) {
        if (event.entity !== mc.player) return
        val result = EventManager.callEvent(AttackEvent(event.target))
        if (result.isCancelled) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onChatSend(event: ClientChatEvent) {
        val result = EventManager.callEvent(ChatSendEvent(event.message))
        if (result.isCancelled) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onChatReceived(event: ClientChatReceivedEvent) {
        val component = event.message
        val result = EventManager.callEvent(ChatReceiveEvent(component.string, component))
        if (result.isCancelled) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onLoggingIn(event: ClientPlayerNetworkEvent.LoggingIn) {
        EventManager.callEvent(WorldChangeEvent(mc.level))
    }

    @SubscribeEvent
    fun onLoggingOut(event: ClientPlayerNetworkEvent.LoggingOut) {
        EventManager.callEvent(WorldChangeEvent(null))
    }

    // NoFall — mixin-free implementation using Forge's fall event.
    @SubscribeEvent
    fun onLivingFall(event: LivingFallEvent) {
        if (event.entity === mc.player && ModuleNoFall.enabled) {
            event.isCanceled = true
        }
    }
}
