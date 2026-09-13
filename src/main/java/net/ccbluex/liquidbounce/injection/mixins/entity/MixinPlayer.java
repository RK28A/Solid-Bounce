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

import net.ccbluex.liquidbounce.features.module.modules.world.ModuleNoSlowBreak;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Drives NoSlowBreak: cancels the underwater / airborne mining penalties. */
@Mixin(Player.class)
public class MixinPlayer {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true, require = 0)
    private void solidbounce$noSlowBreak(BlockState state, CallbackInfoReturnable<Float> cir) {
        if ((Object) this != Minecraft.getInstance().player || !ModuleNoSlowBreak.INSTANCE.getEnabled()) {
            return;
        }
        float speed = cir.getReturnValue();
        // Vanilla divides by 5 when not on the ground and by another 5 when in water.
        if (!Minecraft.getInstance().player.onGround()) {
            speed *= 5.0F;
        }
        if (Minecraft.getInstance().player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) {
            speed *= 5.0F;
        }
        cir.setReturnValue(speed);
    }
}
