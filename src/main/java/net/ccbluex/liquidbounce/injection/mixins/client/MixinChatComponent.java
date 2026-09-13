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

import net.ccbluex.liquidbounce.features.module.modules.misc.ModuleKeepChatAfterDeath;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Drives KeepChatAfterDeath by refusing to wipe the chat history. */
@Mixin(ChatComponent.class)
public class MixinChatComponent {

    @Inject(method = "clearMessages", at = @At("HEAD"), cancellable = true, require = 0)
    private void solidbounce$keepChat(boolean clearSentMsgHistory, CallbackInfo ci) {
        if (ModuleKeepChatAfterDeath.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }
}
