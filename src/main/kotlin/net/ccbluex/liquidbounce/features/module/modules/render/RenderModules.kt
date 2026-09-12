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
 * Remaining Render modules (scaffold: register/toggle/options; deep behavior pending the noted
 * render mixin — most visuals need a MixinGameRenderer/LevelRenderer/entity-render hook).
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.ui.clickgui.ClickGuiScreen
import org.lwjgl.glfw.GLFW

/** Animation — custom item/hand animations. TODO: MixinHeldItemRenderer. */
object ModuleAnimation : Module("Animation", Category.RENDER)

/** AntiBlind — removes blindness/darkness/fog overlays. TODO: MixinInGameHud/fog. */
object ModuleAntiBlind : Module("AntiBlind", Category.RENDER)

/** AttackEffects — custom hit particles/effects. TODO: MixinParticleManager. */
object ModuleAttackEffects : Module("AttackEffects", Category.RENDER)

/** BlockESP — highlights configured blocks. TODO: block-scan + line renderer. */
object ModuleBlockESP : Module("BlockESP", Category.RENDER)

/** Breadcrumbs — draws a trail of the player's path. TODO: WorldRender line trail. */
object ModuleBreadcrumbs : Module("Breadcrumbs", Category.RENDER)

/** CameraClip — lets the third-person camera clip through blocks. TODO: MixinCamera. */
object ModuleCameraClip : Module("CameraClip", Category.RENDER)

/** ClickGui — opens the click-based settings GUI (default bind: Right Shift). */
object ModuleClickGui : Module(
    "ClickGui",
    Category.RENDER,
    bind = GLFW.GLFW_KEY_RIGHT_SHIFT,
    disableActivation = true
) {
    override fun enable() {
        // Never pop the GUI open while a config is being applied at startup.
        if (ConfigSystem.loading) return
        mc.setScreen(ClickGuiScreen())
    }
}

/** CombineMobs — stacks identical mobs visually. TODO: MixinEntityRenderDispatcher. */
object ModuleCombineMobs : Module("CombineMobs", Category.RENDER)

/** Debug — developer debug overlay. TODO: overlay renderer. */
object ModuleDebug : Module("Debug", Category.RENDER)

/** FreeCam — detaches the camera from the player. TODO: MixinCamera + input. */
object ModuleFreeCam : Module("FreeCam", Category.RENDER)

/** HoleESP — highlights safe holes (obsidian/bedrock). TODO: block-scan + line renderer. */
object ModuleHoleESP : Module("HoleESP", Category.RENDER)

/** Hud — toggles the client HUD. TODO: bind to HudRenderer visibility. */
object ModuleHud : Module("Hud", Category.RENDER, state = true)

/** JumpEffect — particle effect on jump. TODO: particle spawn. */
object ModuleJumpEffect : Module("JumpEffect", Category.RENDER)

/** Minimap — renders a minimap. TODO: overlay renderer + chunk sampling. */
object ModuleMinimap : Module("Minimap", Category.RENDER)

/** MobOwners — shows tamed mob owners. TODO: nametag renderer. */
object ModuleMobOwners : Module("MobOwners", Category.RENDER)

/** MurderMystery — highlights roles in Murder Mystery. TODO: game detection + ESP. */
object ModuleMurderMystery : Module("MurderMystery", Category.RENDER)

/** Nametags — enhanced entity nametags. TODO: world-space text renderer. */
object ModuleNametags : Module("Nametags", Category.RENDER)

/** NoBob — removes view bobbing. TODO: MixinGameRenderer bobView. */
object ModuleNoBob : Module("NoBob", Category.RENDER)

/** NoFov — removes FOV changes (sprint/speed). TODO: MixinAbstractClientPlayer fovModifier. */
object ModuleNoFov : Module("NoFov", Category.RENDER)

/** NoHurtCam — removes the hurt camera tilt. TODO: MixinGameRenderer bobHurt. */
object ModuleNoHurtCam : Module("NoHurtCam", Category.RENDER)

/** NoSignRender — skips the sign edit screen. TODO: MixinClientPlayNetworkHandler openScreen. */
object ModuleNoSignRender : Module("NoSignRender", Category.RENDER)

/** NoSwing — hides the hand swing animation. TODO: MixinLivingEntity swing. */
object ModuleNoSwing : Module("NoSwing", Category.RENDER)

/** OverrideTime — forces a client-side world time. TODO: MixinClientLevel time. */
object ModuleOverrideTime : Module("OverrideTime", Category.RENDER) {
    private val time by int("Time", 1000, 0..24000)
}

/** OverrideWeather — forces client-side weather. TODO: MixinClientLevel rain. */
object ModuleOverrideWeather : Module("OverrideWeather", Category.RENDER)

/** QuickPerspectiveSwap — quick key to swap camera perspective. TODO: keybind + option persist. */
object ModuleQuickPerspectiveSwap : Module("QuickPerspectiveSwap", Category.RENDER)

/** Rotations — visualizes/forces client rotations. TODO: rotation system + renderer. */
object ModuleRotations : Module("Rotations", Category.RENDER)

/** Scoreboard — toggles/customizes the scoreboard. TODO: MixinInGameHud scoreboard. */
object ModuleScoreboard : Module("Scoreboard", Category.RENDER, state = true)

/** StorageESP — highlights chests/shulkers/etc. TODO: block-entity enumeration + line renderer. */
object ModuleStorageESP : Module("StorageESP", Category.RENDER)

/** Tracers — lines from the camera to entities. TODO: line renderer with normals. */
object ModuleTracers : Module("Tracers", Category.RENDER)

/** Trajectories — predicts projectile arcs. TODO: physics sim + line renderer. */
object ModuleTrajectories : Module("Trajectories", Category.RENDER)

/** TrueSight — reveals invisible entities/renders. TODO: MixinLivingEntityRenderer. */
object ModuleTrueSight : Module("TrueSight", Category.RENDER)

