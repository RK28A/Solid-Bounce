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
package net.ccbluex.liquidbounce.injection.mixins.client;

import net.ccbluex.liquidbounce.features.module.modules.player.ModuleNoRotateSet;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleNoSignRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Drives NoRotateSet and NoSignRender. */
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Unique
    private float solidbounce$yaw;
    @Unique
    private float solidbounce$pitch;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), require = 0)
    private void solidbounce$captureRotation(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            this.solidbounce$yaw = player.getYRot();
            this.solidbounce$pitch = player.getXRot();
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"), require = 0)
    private void solidbounce$restoreRotation(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && ModuleNoRotateSet.INSTANCE.getEnabled()) {
            player.setYRot(this.solidbounce$yaw);
            player.setXRot(this.solidbounce$pitch);
        }
    }

    @Inject(method = "handleOpenSignEditor", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$noSignRender(ClientboundOpenSignEditorPacket packet, CallbackInfo ci) {
        if (ModuleNoSignRender.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }
}
