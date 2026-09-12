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
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

/**
 * FullBright — lights up the world using a client-side night vision effect.
 *
 * (Gamma-override FullBright will be added once the OptionInstance access mixin lands;
 * night vision is a mixin-free approach that works out of the box on Forge.)
 */
object ModuleFullBright : Module("FullBright", Category.RENDER) {

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        player.addEffect(
            MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false)
        )
    }

    override fun disable() {
        mc.player?.removeEffect(MobEffects.NIGHT_VISION)
    }
}
