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

import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleNoSlow;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Drives NoSlow: vanilla multiplies the movement input by 0.2 while an item is in use.
 */
@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {

    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F), require = 0)
    private float solidbounce$noSlow(float original) {
        return ModuleNoSlow.INSTANCE.getEnabled() ? 1.0F : original;
    }
}
