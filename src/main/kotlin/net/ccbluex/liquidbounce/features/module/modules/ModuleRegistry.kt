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

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.`fun`.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.world.*
import net.ccbluex.liquidbounce.utils.client.logger

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
        group("Combat") {
            arrayOf(
                ModuleAutoClicker, ModuleAutoWeapon, ModuleSuperKnockback, ModuleKillAura,
                ModuleCriticals, ModuleVelocity, ModuleAimbot, ModuleAutoArmor, ModuleAutoBalls,
                ModuleAutoBow, ModuleAutoGapple, ModuleAutoHead, ModuleAutoLeave, ModuleAutoPot,
                ModuleAutoSoup, ModuleBacktrack, ModuleFakeLag, ModuleHitbox, ModuleSwordBlock,
                ModuleTimerRange,
            )
        }

        group("Movement") {
            arrayOf(
                ModuleSprint, ModuleSneak, ModuleSpeed, ModuleFly, ModuleAirJump, ModuleHighJump,
                ModuleAntiLevitation, ModuleAutoDodge, ModuleAvoidHazards, ModuleBlockBounce,
                ModuleBlockWalk, ModuleBugUp, ModuleElytraFly, ModuleFreeze, ModuleInventoryMove,
                ModuleLiquidWalk, ModuleLongJump, ModuleNoClip, ModuleNoJumpDelay, ModuleNoPush,
                ModuleNoSlow, ModuleNoWeb, ModuleParkour, ModulePerfectHorseJump, ModuleReverseStep,
                ModuleSafeWalk, ModuleStep, ModuleStrafe, ModuleTerrainSpeed, ModuleVehicleFly,
            )
        }

        group("Player") {
            arrayOf(
                ModuleAutoRespawn, ModuleNoFall, ModuleAntiAFK, ModuleAutoWalk, ModuleBlink,
                ModuleAntiExploit, ModuleAutoBreak, ModuleAutoFish, ModuleAutoPlay, ModuleAutoTotem,
                ModuleChestStealer, ModuleEagle, ModuleFastUse, ModuleInventoryCleaner,
                ModuleNoRotateSet, ModuleReach, ModuleRegen, ModuleZoot,
            )
        }

        group("Render") {
            arrayOf(
                ModuleFullBright, ModuleESP, ModuleItemESP, ModuleAnimation, ModuleAntiBlind,
                ModuleAttackEffects, ModuleBlockESP, ModuleBreadcrumbs, ModuleCameraClip,
                ModuleClickGui, ModuleCombineMobs, ModuleDebug, ModuleFreeCam, ModuleHoleESP,
                ModuleHud, ModuleJumpEffect, ModuleMinimap, ModuleMobOwners, ModuleMurderMystery,
                ModuleNametags, ModuleNoBob, ModuleNoFov, ModuleNoHurtCam, ModuleNoSignRender,
                ModuleNoSwing, ModuleOverrideTime, ModuleOverrideWeather, ModuleQuickPerspectiveSwap,
                ModuleRotations, ModuleScoreboard, ModuleStorageESP, ModuleTracers, ModuleTrajectories,
                ModuleTrueSight, ModuleXRay,
            )
        }

        group("World") {
            arrayOf(
                ModuleAutoTool, ModuleAutoDisable, ModuleAutoFarm, ModuleChestAura, ModuleCrystalAura,
                ModuleFastBreak, ModuleFastPlace, ModuleFucker, ModuleIgnite, ModuleNoSlowBreak,
                ModuleNuker, ModuleProjectilePuncher, ModuleScaffold, ModuleTimer,
            )
        }

        group("Exploit") {
            arrayOf(
                ModuleAbortBreaking, ModuleAntiReducedDebugInfo, ModuleAntiVanish, ModuleClip,
                ModuleDamage, ModuleDisabler, ModuleForceUnicodeChat, ModuleGhostHand, ModuleKick,
                ModuleMoreCarry, ModuleNameCollector, ModuleNoPitchLimit, ModulePingSpoof,
                ModulePlugins, ModulePortalMenu, ModuleResourceSpoof, ModuleServerCrasher,
                ModuleSleepWalker, ModuleSpoofer, ModuleVehicleOneHit,
            )
        }

        group("Misc") {
            arrayOf(
                ModuleSpammer, ModuleAntiBot, ModuleAutoAccount, ModuleAutoChatGame, ModuleAutoConfig,
                ModuleCapeTransfer, ModuleClickRecorder, ModuleDebugRecorder, ModuleFocus,
                ModuleFriendClicker, ModuleHideClient, ModuleKeepChatAfterDeath, ModuleNameProtect,
                ModuleNotifier, ModuleTeams,
            )
        }

        // Extra (not in upstream v0.1.0). Kept on its own so a failure here cannot take Misc down.
        group("EnchantCracker") { arrayOf(ModuleEnchantCracker) }

        group("Fun") {
            arrayOf(ModuleDankBobbing, ModuleDerp, ModuleHandDerp, ModuleSkinDerp)
        }
    }

    /**
     * Registers one category. Constructing the module objects runs their initialisers, so a single
     * bad module would otherwise abort the whole registration (and, before this, the client boot).
     * Isolating each category keeps the client usable and names the culprit in the log.
     */
    private inline fun group(name: String, supply: () -> Array<Module>) {
        runCatching { ModuleManager.addModules(*supply()) }
            .onFailure { logger.error("Solid-Bounce: module category '$name' failed to register.", it) }
    }
}
