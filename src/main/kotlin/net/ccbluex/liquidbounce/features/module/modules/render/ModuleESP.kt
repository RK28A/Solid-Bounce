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

import net.ccbluex.liquidbounce.event.events.WorldRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.entity.player.Player

/**
 * ESP — draws colored outline boxes around players (line-box mode).
 *
 * A boxed/2D/glow mode set will be ported later; this proves the WorldRenderEvent pipeline.
 */
object ModuleESP : Module("ESP", Category.RENDER) {

    private val onlyPlayers by boolean("OnlyPlayers", true)

    @Suppress("unused")
    val renderHandler = handler<WorldRenderEvent> { event ->
        val poseStack = event.matrixStack
        val cam = event.camera.position

        val buffers = mc.renderBuffers().bufferSource()
        val consumer = buffers.getBuffer(RenderType.lines())

        poseStack.pushPose()
        poseStack.translate(-cam.x, -cam.y, -cam.z)

        for (entity in world.entitiesForRendering()) {
            if (entity === player) continue
            if (onlyPlayers && entity !is Player) continue

            val box = entity.boundingBox
            LevelRenderer.renderLineBox(poseStack, consumer, box, 1.0f, 0.2f, 0.2f, 1.0f)
        }

        poseStack.popPose()
        buffers.endBatch(RenderType.lines())
    }
}
