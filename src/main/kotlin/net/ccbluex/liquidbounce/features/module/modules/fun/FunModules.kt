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
 *
 * Fun modules (scaffold: register/toggle; visual behavior pending the noted render mixin).
 */
package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/** DankBobbing — exaggerated view/hand bobbing. TODO: MixinGameRenderer/HeldItemRenderer. */
object ModuleDankBobbing : Module("DankBobbing", Category.FUN)

/** Derp — randomizes the player's server-side head/body rotation. TODO: rotation packet spoof. */
object ModuleDerp : Module("Derp", Category.FUN)

/** HandDerp — derps the held-item rendering. TODO: MixinHeldItemRenderer. */
object ModuleHandDerp : Module("HandDerp", Category.FUN)

/** SkinDerp — randomizes visible skin layers. TODO: MixinPlayerModel/skin layers. */
object ModuleSkinDerp : Module("SkinDerp", Category.FUN)
