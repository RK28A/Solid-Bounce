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

import com.mojang.blaze3d.vertex.PoseStack;
import net.ccbluex.liquidbounce.features.module.modules.player.ModuleZoot;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleNoBob;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleNoHurtCam;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Drives NoBob, NoHurtCam and Zoot. */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$bobView(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (ModuleNoBob.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$bobHurt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (ModuleNoHurtCam.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void solidbounce$getFov(Camera camera, float partialTick, boolean useFovSetting,
                                    CallbackInfoReturnable<Double> cir) {
        if (ModuleZoot.INSTANCE.getEnabled()) {
            cir.setReturnValue(cir.getReturnValue() / ModuleZoot.INSTANCE.zoomFactor());
        }
    }
}
