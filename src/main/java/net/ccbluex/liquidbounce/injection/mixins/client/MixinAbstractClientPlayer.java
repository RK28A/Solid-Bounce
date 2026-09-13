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

import net.ccbluex.liquidbounce.features.module.modules.render.ModuleNoFov;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Drives NoFov: keeps the field of view fixed while sprinting or under Speed. */
@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$noFov(CallbackInfoReturnable<Float> cir) {
        if (ModuleNoFov.INSTANCE.getEnabled()) {
            cir.setReturnValue(1.0F);
        }
    }
}
