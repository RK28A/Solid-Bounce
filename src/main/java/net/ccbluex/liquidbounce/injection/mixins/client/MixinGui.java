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

import net.ccbluex.liquidbounce.features.module.modules.render.ModuleScoreboard;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Drives the Scoreboard module: hides the vanilla sidebar when it is turned off. */
@Mixin(Gui.class)
public class MixinGui {

    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$hideScoreboard(net.minecraft.client.gui.GuiGraphics guiGraphics,
                                            net.minecraft.world.scores.Objective objective,
                                            CallbackInfo ci) {
        if (!ModuleScoreboard.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }
}
