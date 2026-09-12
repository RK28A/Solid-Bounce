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
package net.ccbluex.liquidbounce.injection.mixins.world;

import net.ccbluex.liquidbounce.features.module.modules.render.ModuleXRay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Drives the XRay module: while it is enabled, every block face is culled unless the block is
 * one XRay wants to show.
 *
 * require = 0 on purpose — if the vanilla/Forge descriptor of shouldRenderFace ever differs,
 * the injection is skipped (XRay simply does nothing) instead of crashing the client.
 */
@Mixin(Block.class)
public class MixinBlock {

    @Inject(
        method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;" +
                 "Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;" +
                 "Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void solidbounce$onShouldRenderFace(BlockState state, BlockGetter level, BlockPos pos,
                                                       Direction face, BlockPos neighborPos,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (ModuleXRay.INSTANCE.getEnabled()) {
            cir.setReturnValue(ModuleXRay.INSTANCE.shouldShow(state));
        }
    }
}
