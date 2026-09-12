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
package net.ccbluex.liquidbounce.features.module.modules

import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.`fun`.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.world.*

/**
 * Registers the full LiquidBounce v0.1.0 module set (156 modules) plus Solid-Bounce extras
 * (currently EnchantCracker) with the [ModuleManager].
 *
 * Behavioral depth varies: many combat/movement/render/packet modules are fully wired, while
 * others are registered scaffolds whose deep logic is being filled in incrementally (each such
 * module documents the mixin/util it still needs). The full set is present, toggleable and
 * listed so the client is feature-complete in structure.
 */
object ModuleRegistry {

    fun init() {
        ModuleManager.addModules(
            // ---------------- Combat (20) ----------------
            ModuleAutoClicker, ModuleAutoWeapon, ModuleSuperKnockback, ModuleKillAura,
            ModuleCriticals, ModuleVelocity, ModuleAimbot, ModuleAutoArmor, ModuleAutoBalls,
            ModuleAutoBow, ModuleAutoGapple, ModuleAutoHead, ModuleAutoLeave, ModuleAutoPot,
            ModuleAutoSoup, ModuleBacktrack, ModuleFakeLag, ModuleHitbox, ModuleSwordBlock,
            ModuleTimerRange,

            // ---------------- Movement (30) ----------------
            ModuleSprint, ModuleSneak, ModuleSpeed, ModuleFly, ModuleAirJump, ModuleHighJump,
            ModuleAntiLevitation, ModuleAutoDodge, ModuleAvoidHazards, ModuleBlockBounce,
            ModuleBlockWalk, ModuleBugUp, ModuleElytraFly, ModuleFreeze, ModuleInventoryMove,
            ModuleLiquidWalk, ModuleLongJump, ModuleNoClip, ModuleNoJumpDelay, ModuleNoPush,
            ModuleNoSlow, ModuleNoWeb, ModuleParkour, ModulePerfectHorseJump, ModuleReverseStep,
            ModuleSafeWalk, ModuleStep, ModuleStrafe, ModuleTerrainSpeed, ModuleVehicleFly,

            // ---------------- Player (18) ----------------
            ModuleAutoRespawn, ModuleNoFall, ModuleAntiAFK, ModuleAutoWalk, ModuleBlink,
            ModuleAntiExploit, ModuleAutoBreak, ModuleAutoFish, ModuleAutoPlay, ModuleAutoTotem,
            ModuleChestStealer, ModuleEagle, ModuleFastUse, ModuleInventoryCleaner,
            ModuleNoRotateSet, ModuleReach, ModuleRegen, ModuleZoot,

            // ---------------- Render (35) ----------------
            ModuleFullBright, ModuleESP, ModuleItemESP, ModuleAnimation, ModuleAntiBlind,
            ModuleAttackEffects, ModuleBlockESP, ModuleBreadcrumbs, ModuleCameraClip,
            ModuleClickGui, ModuleCombineMobs, ModuleDebug, ModuleFreeCam, ModuleHoleESP,
            ModuleHud, ModuleJumpEffect, ModuleMinimap, ModuleMobOwners, ModuleMurderMystery,
            ModuleNametags, ModuleNoBob, ModuleNoFov, ModuleNoHurtCam, ModuleNoSignRender,
            ModuleNoSwing, ModuleOverrideTime, ModuleOverrideWeather, ModuleQuickPerspectiveSwap,
            ModuleRotations, ModuleScoreboard, ModuleStorageESP, ModuleTracers, ModuleTrajectories,
            ModuleTrueSight, ModuleXRay,

            // ---------------- World (14) ----------------
            ModuleAutoTool, ModuleAutoDisable, ModuleAutoFarm, ModuleChestAura, ModuleCrystalAura,
            ModuleFastBreak, ModuleFastPlace, ModuleFucker, ModuleIgnite, ModuleNoSlowBreak,
            ModuleNuker, ModuleProjectilePuncher, ModuleScaffold, ModuleTimer,

            // ---------------- Exploit (20) ----------------
            ModuleAbortBreaking, ModuleAntiReducedDebugInfo, ModuleAntiVanish, ModuleClip,
            ModuleDamage, ModuleDisabler, ModuleForceUnicodeChat, ModuleGhostHand, ModuleKick,
            ModuleMoreCarry, ModuleNameCollector, ModuleNoPitchLimit, ModulePingSpoof,
            ModulePlugins, ModulePortalMenu, ModuleResourceSpoof, ModuleServerCrasher,
            ModuleSleepWalker, ModuleSpoofer, ModuleVehicleOneHit,

            // ---------------- Misc (15) ----------------
            ModuleSpammer, ModuleAntiBot, ModuleAutoAccount, ModuleAutoChatGame, ModuleAutoConfig,
            ModuleCapeTransfer, ModuleClickRecorder, ModuleDebugRecorder, ModuleFocus,
            ModuleFriendClicker, ModuleHideClient, ModuleKeepChatAfterDeath, ModuleNameProtect,
            ModuleNotifier, ModuleTeams,
            // Extra (not in upstream v0.1.0)
            ModuleEnchantCracker,

            // ---------------- Fun (4) ----------------
            ModuleDankBobbing, ModuleDerp, ModuleHandDerp, ModuleSkinDerp,
        )
    }
}
