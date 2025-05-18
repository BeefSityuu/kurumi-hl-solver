package com.sityuu.kurumiHlSolver.client

import com.sityuu.kurumiHlSolver.RandUtil
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import kotlin.math.round

object NearbyHudRenderer {
    fun register() =
        HudRenderCallback.EVENT.register { ctx, _ ->
            val mc = MinecraftClient.getInstance()
            val player = mc.player ?: return@register
            val tr = mc.textRenderer
            var y = 10

            EntityTracker.nearby
                .sortedBy { it.distanceTo(player) }
                .take(12)
                .forEach { e ->
                    val d = "%.1f".format(e.distanceTo(player))
                    // val coloredName: String = TextUtil.toLegacyString(entity.displayName)
                    if (e.type == EntityType.ARMOR_STAND) {
                        val entityPos = e.blockPos
                        val belowPos = entityPos.down()
                        val blockState = e.world.getBlockState(belowPos)
                        val block = blockState.block

                        ctx.drawText(tr, "${e.name.string} (§7${d} m§r) | §7${RandUtil.round(e.x, 2)}, ${RandUtil.round(e.y, 2)}, ${RandUtil.round(e.z, 2)} §7| ${block.name}", 10, y, 0xFFFFFF, true)
                    } else {
                        ctx.drawText(tr, "${e.name.string} (§7${d} m§r) | §7${RandUtil.round(e.x, 2)}, ${RandUtil.round(e.y, 2)}, ${RandUtil.round(e.z, 2)}", 10, y, 0xFFFFFF, true)
                    }
                    y += tr.fontHeight + 2
                }
        }
}

