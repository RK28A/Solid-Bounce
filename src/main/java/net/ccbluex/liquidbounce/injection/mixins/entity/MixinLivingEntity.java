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
package net.ccbluex.liquidbounce.injection.mixins.entity;

import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleNoJumpDelay;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleNoSwing;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Drives NoJumpDelay and NoSwing. */
@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    // public, not private: Mixin rejects a @Shadow whose visibility is narrower than the
    // target field, and that rejection is fatal regardless of require = 0.
    @Shadow
    public int noJumpDelay;

    @Inject(method = "aiStep", at = @At("HEAD"), require = 0)
    private void solidbounce$clearJumpDelay(CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player && ModuleNoJumpDelay.INSTANCE.getEnabled()) {
            this.noJumpDelay = 0;
        }
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$noSwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player && ModuleNoSwing.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }
}
