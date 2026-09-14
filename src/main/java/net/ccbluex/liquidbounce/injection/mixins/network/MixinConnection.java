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
package net.ccbluex.liquidbounce.injection.mixins.network;

import io.netty.channel.ChannelHandlerContext;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.PacketEvent;
import net.ccbluex.liquidbounce.event.events.TransferOrigin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts inbound and outbound packets and fires a {@link PacketEvent}, so modules
 * (Velocity, Blink, packet-based NoFall, ...) can observe or cancel them.
 *
 * Mojang-mappings equivalent of upstream LiquidBounce's MixinClientConnection.
 */
@Mixin(Connection.class)
public class MixinConnection {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true,
        require = 0)
    private void solidbounce$onSend(Packet<?> packet, CallbackInfo ci) {
        PacketEvent event = new PacketEvent(packet, TransferOrigin.SEND);
        EventManager.INSTANCE.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void solidbounce$onReceive(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        // Only the client's own connection is of interest. Checking it against Minecraft's
        // listener uses public API, so no @Shadow of Connection.receiving is needed — an
        // unresolvable shadow would be a fatal InvalidMixinException rather than a skipped hook.
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener != null && listener.getConnection() == (Object) this) {
            PacketEvent event = new PacketEvent(packet, TransferOrigin.RECEIVE);
            EventManager.INSTANCE.callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
