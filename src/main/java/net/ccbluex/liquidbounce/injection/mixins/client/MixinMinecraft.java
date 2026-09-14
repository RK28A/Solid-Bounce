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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Drives FastPlace / FastUse.
 *
 * Vanilla ends {@code startUseItem} with {@code this.rightClickDelay = 4}, so rewriting that
 * constant removes the right-click cooldown. This deliberately avoids a {@code @Shadow} of
 * {@code rightClickDelay}: a shadow that cannot be resolved at runtime throws
 * InvalidMixinException and kills the game, while {@code require = 0} makes a missed
 * constant merely turn the feature off.
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {

    @ModifyConstant(method = "startUseItem", constant = @Constant(intValue = 4), require = 0)
    private int solidbounce$rightClickDelay(int original) {
        if (ModuleFastPlace.INSTANCE.getEnabled() || ModuleFastUse.INSTANCE.getEnabled()) {
            return 0;
        }
        return original;
    }
}
