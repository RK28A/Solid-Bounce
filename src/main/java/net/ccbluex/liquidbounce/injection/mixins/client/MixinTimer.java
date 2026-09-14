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

import net.ccbluex.liquidbounce.features.module.modules.combat.ModuleTimerRange;
import net.ccbluex.liquidbounce.features.module.modules.world.ModuleTimer;
import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Drives the Timer module by scaling how many ticks the game is told to run.
 * Scaling the returned tick count avoids having to write to Timer's internal fields.
 */
@Mixin(Timer.class)
public class MixinTimer {

    @Inject(method = "advanceTime", at = @At("RETURN"), cancellable = true, require = 0)
    private void solidbounce$advanceTime(long gameTime, CallbackInfoReturnable<Integer> cir) {
        float speed = 1.0F;
        if (ModuleTimer.INSTANCE.getEnabled()) {
            speed = ModuleTimer.INSTANCE.timerSpeed();
        } else if (ModuleTimerRange.INSTANCE.getEnabled()) {
            speed = ModuleTimerRange.INSTANCE.activeSpeed();
        }

        if (speed != 1.0F) {
            cir.setReturnValue(Math.round(cir.getReturnValue() * speed));
        }
    }
}
