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
package net.ccbluex.liquidbounce.features.module

/**
 * One-line descriptions, shown by the ClickGUI when a module row is held down.
 *
 * Kept in a table rather than on each module so that adding a description never means touching
 * 157 module declarations. A module may still override [Module.description] for something more
 * specific; this is the fallback.
 */
object ModuleDescriptions {

    fun of(name: String): String = table[name] ?: "No description yet."

    private val table: Map<String, String> = mapOf(
        // ---------------- Combat ----------------
        "Aimbot" to "Aims at the nearest entity without attacking it.",
        "AutoArmor" to "Equips the best armour found in your inventory.",
        "AutoBalls" to "Throws snowballs and eggs at the nearest player.",
        "AutoBow" to "Charges and releases a bow at the nearest target.",
        "AutoClicker" to "Clicks for you at a configurable, randomised rate.",
        "AutoGapple" to "Eats a golden apple when your health drops.",
        "AutoHead" to "Aims at the target's head instead of its body.",
        "AutoLeave" to "Disconnects when your health drops below the threshold.",
        "AutoPot" to "Throws a splash healing potion at your feet when hurt.",
        "AutoSoup" to "Drinks soup when your health drops.",
        "AutoWeapon" to "Switches to the strongest weapon before attacking.",
        "Backtrack" to "Holds incoming entity positions back so targets lag behind.",
        "Criticals" to "Makes every hit a critical hit.",
        "FakeLag" to "Holds outgoing packets and releases them in bursts.",
        "Hitbox" to "Expands entity hitboxes so hits land more easily.",
        "KillAura" to "Automatically attacks entities around you.",
        "SuperKnockback" to "Adds sprint knockback to every hit.",
        "SwordBlock" to "Shows the 1.8 sword blocking animation.",
        "TimerRange" to "Speeds the game timer up only while an enemy is near.",
        "Velocity" to "Reduces or cancels the knockback you take.",

        // ---------------- Movement ----------------
        "AirJump" to "Lets you jump again while in mid-air.",
        "AntiLevitation" to "Cancels the levitation effect.",
        "AutoDodge" to "Steps aside when an arrow is heading for you.",
        "AvoidHazards" to "Refuses to walk into fire, lava and other hazards.",
        "BlockBounce" to "Bounces you off the block you land on.",
        "BlockWalk" to "Lets you walk on top of blocks you would normally fall past.",
        "BugUp" to "Pushes you up through the block above you.",
        "ElytraFly" to "Flies with an elytra without using firework rockets.",
        "Fly" to "Lets you fly.",
        "Freeze" to "Freezes you in place while the server keeps running.",
        "HighJump" to "Jumps higher than vanilla allows.",
        "InventoryMove" to "Lets you keep walking while an inventory screen is open.",
        "LiquidWalk" to "Lets you walk on the surface of water and lava.",
        "LongJump" to "Jumps much further than vanilla allows.",
        "NoClip" to "Lets you pass through blocks.",
        "NoJumpDelay" to "Removes the cooldown between jumps.",
        "NoPush" to "Stops other entities and flowing water from pushing you.",
        "NoSlow" to "Removes the slowdown from eating, blocking, soul sand and webs.",
        "NoWeb" to "Ignores cobwebs.",
        "Parkour" to "Jumps automatically at the edge of a block.",
        "PerfectHorseJump" to "Always charges a mounted jump to full power.",
        "ReverseStep" to "Steps down as fast as you step up.",
        "SafeWalk" to "Stops you walking off an edge, as if sneaking.",
        "Sneak" to "Keeps you sneaking without holding the key.",
        "Speed" to "Moves you faster than vanilla allows.",
        "Sprint" to "Keeps you sprinting without holding the key.",
        "Step" to "Steps up blocks taller than vanilla allows.",
        "Strafe" to "Gives you full air control.",
        "TerrainSpeed" to "Scales your speed with the friction of the ground.",
        "VehicleFly" to "Flies the boat, horse or minecart you are riding.",

        // ---------------- Player ----------------
        "AntiAFK" to "Keeps you active so AFK checks do not kick you.",
        "AntiExploit" to "Ignores packets a server could use to crash or desync your client.",
        "AutoBreak" to "Breaks the block you are looking at.",
        "AutoFish" to "Reels the rod in and recasts it when a fish bites.",
        "AutoPlay" to "Plays simple server mini-games for you.",
        "AutoRespawn" to "Respawns the moment you die.",
        "AutoTotem" to "Keeps a totem of undying in your off hand.",
        "AutoWalk" to "Keeps walking forward without holding the key.",
        "Blink" to "Holds your movement packets back, then releases them at once.",
        "ChestStealer" to "Empties a chest as soon as you open it.",
        "Eagle" to "Sneaks at block edges while bridging.",
        "FastUse" to "Removes the cooldown between right-clicks.",
        "InventoryCleaner" to "Throws away items you do not want to carry.",
        "NoFall" to "Stops you taking fall damage.",
        "NoRotateSet" to "Ignores the server's attempts to turn your camera.",
        "Reach" to "Extends how far you can reach.",
        "Regen" to "Regenerates health faster than vanilla.",
        "Zoot" to "Zooms the camera in.",

        // ---------------- Render ----------------
        "Animation" to "Customises the held-item and swing animation.",
        "AntiBlind" to "Removes blindness, darkness and the pumpkin overlay.",
        "AttackEffects" to "Draws an effect where your hits land.",
        "BlockESP" to "Highlights chosen blocks through walls.",
        "Breadcrumbs" to "Draws a trail behind every player.",
        "CameraClip" to "Stops the third-person camera clipping into blocks.",
        "ClickGui" to "Opens this window.",
        "CombineMobs" to "Merges nearby mobs of the same kind into one marker.",
        "Debug" to "Draws internal client state for debugging.",
        "ESP" to "Outlines entities through walls.",
        "FreeCam" to "Detaches the camera from your body.",
        "FullBright" to "Lights the world up as if it were day.",
        "HoleESP" to "Highlights one-block-wide holes you can stand in.",
        "Hud" to "Draws the watermark, module list and other overlays.",
        "ItemESP" to "Outlines dropped items through walls.",
        "JumpEffect" to "Draws an effect under you when you jump.",
        "Minimap" to "Draws a small map of the terrain around you.",
        "MobOwners" to "Shows who tamed a pet.",
        "MurderMystery" to "Marks the murderer and the detective.",
        "Nametags" to "Draws a readable name above every player, through walls.",
        "NoBob" to "Removes the walking view bob.",
        "NoFov" to "Locks the field of view so speed effects do not change it.",
        "NoHurtCam" to "Removes the camera shake when you take damage.",
        "NoSignRender" to "Hides sign text.",
        "NoSwing" to "Hides your own arm swing.",
        "OverrideTime" to "Forces a time of day.",
        "OverrideWeather" to "Forces the weather.",
        "QuickPerspectiveSwap" to "Switches perspective with one key.",
        "Rotations" to "Shows the rotation the server sees, next to your own.",
        "Scoreboard" to "Hides or restyles the sidebar scoreboard.",
        "StorageESP" to "Outlines chests, barrels and shulkers through walls.",
        "Tracers" to "Draws a line from you to every entity.",
        "Trajectories" to "Draws where your projectile will land.",
        "TrueSight" to "Reveals invisible entities.",
        "XRay" to "Hides every block except the ones you choose.",

        // ---------------- World ----------------
        "AutoDisable" to "Turns every other module off when you die or leave the world.",
        "AutoFarm" to "Harvests and replants crops around you.",
        "AutoTool" to "Switches to the best tool before breaking a block.",
        "ChestAura" to "Opens containers around you automatically.",
        "CrystalAura" to "Places and detonates end crystals.",
        "FastBreak" to "Breaks blocks faster.",
        "FastPlace" to "Removes the cooldown between block placements.",
        "Fucker" to "Breaks the chosen block type around you.",
        "Ignite" to "Sets nearby players on fire.",
        "NoSlowBreak" to "Ignores the underwater and airborne mining penalty.",
        "Nuker" to "Breaks every block within reach.",
        "ProjectilePuncher" to "Hits incoming projectiles away.",
        "Scaffold" to "Places blocks under you as you walk.",
        "Timer" to "Changes how fast the game ticks.",

        // ---------------- Exploit ----------------
        "AbortBreaking" to "Cancels block breaking without the server noticing.",
        "AntiReducedDebugInfo" to "Restores the F3 information a server tried to hide.",
        "AntiVanish" to "Reveals players the server tried to hide from you.",
        "Clip" to "Teleports you a short distance through blocks.",
        "Damage" to "Takes controlled fall damage to reach a chosen health.",
        "Disabler" to "Registered but inert: its only purpose is to break the server.",
        "ForceUnicodeChat" to "Sends chat in a form some filters do not read.",
        "GhostHand" to "Opens containers through walls.",
        "Kick" to "Registered but inert: its only purpose is to break the server.",
        "MoreCarry" to "Carries more than one stack out of a container.",
        "NameCollector" to "Collects the names of everyone you see.",
        "NoPitchLimit" to "Removes the +/-90 degree pitch clamp.",
        "PingSpoof" to "Reports a different latency to the server.",
        "Plugins" to "Tries to list the plugins the server runs.",
        "PortalMenu" to "Lets you use inventory screens while in a portal.",
        "ResourceSpoof" to "Pretends to accept the server resource pack.",
        "ServerCrasher" to "Registered but inert: its only purpose is to break the server.",
        "SleepWalker" to "Keeps moving while you are in a bed.",
        "Spoofer" to "Reports different client details to the server.",
        "VehicleOneHit" to "Destroys a vehicle in a single hit.",

        // ---------------- Misc ----------------
        "AntiBot" to "Decides which entities are fake players, so targeting skips them.",
        "AutoAccount" to "Needs CCBlueX's account backend; registered but inert.",
        "AutoChatGame" to "Answers simple chat mini-games.",
        "AutoConfig" to "Needs CCBlueX's config CDN; registered but inert.",
        "CapeTransfer" to "Needs CCBlueX's cape service; registered but inert.",
        "ClickRecorder" to "Needs CCBlueX's recording backend; registered but inert.",
        "DebugRecorder" to "Needs CCBlueX's recording backend; registered but inert.",
        "EnchantCracker" to "Cracks the enchantment seed and tells you how many items to drop.",
        "Focus" to "Throttles the frame rate while the window is in the background.",
        "FriendClicker" to "Middle-click a player to add or remove them as a friend.",
        "HideClient" to "Stops the client announcing its brand to the server.",
        "KeepChatAfterDeath" to "Keeps the chat history when you respawn.",
        "NameProtect" to "Censors your own name in chat and on screen.",
        "Notifier" to "Warns you about things worth reacting to.",
        "Spammer" to "Repeats a message in chat.",
        "Teams" to "Recognises teammates so they are left alone.",

        // ---------------- Fun ----------------
        "DankBobbing" to "Exaggerates the walking bob.",
        "Derp" to "Spins the rotation the server sees, leaving your own view alone.",
        "HandDerp" to "Keeps your arm swinging wildly.",
        "SkinDerp" to "Flickers your skin layers on and off.",
    )
}
