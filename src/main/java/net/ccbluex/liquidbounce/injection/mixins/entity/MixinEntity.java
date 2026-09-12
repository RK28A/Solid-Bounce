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

import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleNoWeb;
import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleStep;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleTrueSight;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Drives Step, NoWeb and TrueSight. */
@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$maxUpStep(CallbackInfoReturnable<Float> cir) {
        if ((Object) this == Minecraft.getInstance().player && ModuleStep.INSTANCE.getEnabled()) {
            cir.setReturnValue(ModuleStep.INSTANCE.stepHeight());
        }
    }

    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$makeStuckInBlock(BlockState state, Vec3 motionMultiplier, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player && ModuleNoWeb.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$isInvisible(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleTrueSight.INSTANCE.getEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
