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

import net.ccbluex.liquidbounce.features.module.modules.player.ModuleFastUse;
import net.ccbluex.liquidbounce.features.module.modules.world.ModuleFastPlace;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Drives FastPlace / FastUse by clearing the vanilla right-click cooldown every tick. */
@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void solidbounce$tick(CallbackInfo ci) {
        if (ModuleFastPlace.INSTANCE.getEnabled() || ModuleFastUse.INSTANCE.getEnabled()) {
            this.rightClickDelay = 0;
        }
    }
}
