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
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * NoFall — prevents fall damage.
 *
 * The actual cancellation is performed in [net.ccbluex.liquidbounce.integration.forge.ForgeEventBridge]
 * via Forge's LivingFallEvent (a mixin-free approach on Forge). A packet-based mode
 * (spoofing onGround) will be added once the network mixin is activated.
 */
object ModuleNoFall : Module("NoFall", Category.PLAYER)
