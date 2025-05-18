package com.sityuu.kurumiHlSolver.client

import com.sityuu.kurumiHlSolver.RandUtil
import com.sityuu.kurumiHlSolver.client.EntityTracker.getEntityNamesContaining
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.world.World

object HighLow {
    private var hasStarted = false
    private var rendererRegistered = false
    private var ticks = 0
    private const val RANGE = 32.0
    private var hudMessage = mutableListOf("...", "...", "...", "...", "...")

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client: MinecraftClient ->
            if (++ticks % 20 != 0) return@register

            if (EntityTracker.containsEntityWithName("High and Low in PlayerRealms") && !hasStarted) {
                start()
                hasStarted = true
                hudMessage[0] = "§9HL Solver Active"
            } else if (!EntityTracker.containsEntityWithName("High and Low in PlayerRealms")) {
                hasStarted = false // Reset when Kurumi leaves
                hudMessage[0] = "§cHL Feature Disabled"
            }

            if (hasStarted) {
                ticker(client)
            }
        }

        // HUD は一度のみ登録
        if (!rendererRegistered) {
            HudRenderCallback.EVENT.register(HudRenderCallback { drawContext, tickDelta ->
                if (hasStarted) {
                    // Draw your HUD element
                    val client = MinecraftClient.getInstance()
                    val tr = client.textRenderer
                    var y = 150
                    for (line in hudMessage) {
                        drawContext.drawText(tr, line, 10, y, 0xFFFFFF, true)
                        y += tr.fontHeight + 2
                    }
                }
            })
            rendererRegistered = true
        }
    }


    private fun start() {
        val client = MinecraftClient.getInstance()
        client.player?.sendMessage(Text.literal("aa"), false)
        /*
        HudRenderCallback.EVENT.register { ctx, _ ->
            val mc = MinecraftClient.getInstance()
            val player = mc.player ?: return@register
            val tr = mc.textRenderer
            var y = 700

            ctx.drawText(tr, "§9HL True", 10, y, 0xFFFFFF, true)
        }

         */
    }

    private fun ticker(client: MinecraftClient) {
        val numbers = getNumbersOnWool(client.world!!, EntityTracker.nearby)
        val data = getOrderedArmorStandsOnWool(client.world!!, EntityTracker.nearby)
        hudMessage[1] = "${numbers}"
        hudMessage[2] = "${data}"

        // それぞれの当たる確率を計算
        // そのために Key の位置を特定
        var keyIndex = -1
        for (d in data) {
            val index = d.first
            val names = d.second
            for (nm in names) {
                if (nm.contains("Key")) {
                    keyIndex = index
                }
            }
        }
        if (keyIndex == -1) {
            hudMessage[4] = "§cKey not found"
            return
        } else {
            var lowSum = 0.0
            var highSum = 0.0
            var totalSum = 0.0
            // Low
            for (i in 0 until keyIndex + 1) {
                lowSum += numbers[i] ?: 0
            }
            // High
            for (i in keyIndex until numbers.size) {
                highSum += numbers[i] ?: 0
            }
            // Total
            for (i in 0 until numbers.size) {
                totalSum += numbers[i] ?: 0
            }

            // 倍率を取得
            val lowS = getEntityNamesContaining("Low :")
            var lowMul = 0.0
            if (lowS.isNotEmpty()) {
                lowMul = lowS[0].substringAfter(":").substringBefore("倍").trim().toDoubleOrNull()!!
            } else {
                lowMul = 0.0
            }
            val highS = getEntityNamesContaining("High :")
            var highMul = 0.0
            if (highS.isNotEmpty()) {
                highMul = highS[0].substringAfter(":").substringBefore("倍").trim().toDoubleOrNull()!!
            } else {
                highMul = 0.0
            }

            // 期待値も計算
            val lowChance = lowSum/totalSum
            val highChance = highSum/totalSum
            val lowExp = lowMul * lowChance
            val highExp = highMul * highChance
            hudMessage[4] = "§9Low : ${RandUtil.round(100*lowChance, 2)}%, ${RandUtil.round(lowExp, 2)}x §7/ §cHigh : ${RandUtil.round(100*highChance, 4)}%, ${RandUtil.round(highExp, 4)}x"
        }

        hudMessage[3] = "${getEntityNamesContaining("Low :")} / ${getEntityNamesContaining("High :")}"
    }

    // 数字を取得, 順番通りに
    private fun getNumbersOnWool(
        world: World,
        entities: List<Entity>
    ): List<Int?> {
        val orderedWools = listOf(
            Blocks.PURPLE_WOOL,
            Blocks.BLUE_WOOL,
            Blocks.CYAN_WOOL,
            Blocks.LIGHT_BLUE_WOOL,
            Blocks.GREEN_WOOL,
            Blocks.LIME_WOOL,
            Blocks.ORANGE_WOOL,
            Blocks.YELLOW_WOOL,
            Blocks.RED_WOOL
        )
        val woolIndexMap = orderedWools.withIndex().associate { it.value to it.index }

        val candidates = entities
            .filterIsInstance<ArmorStandEntity>()
            .mapNotNull { armorStand ->
                val blockBelow = world.getBlockState(armorStand.blockPos.down()).block
                val index = woolIndexMap[blockBelow]
                if (index != null) {
                    val name = armorStand.displayName?.string ?: "<no name>"
                    if (name.toIntOrNull() != null) index to name else null
                } else null
            }
            .groupBy { it.first }
            .mapValues { it.value.first() }

        return (0 until orderedWools.size).map { idx ->
            candidates[idx]?.second?.toIntOrNull() ?: -1
        }
    }

    // 羊毛の上のを全て取得 (没)
    private fun getOrderedArmorStandsOnWool(
        world: World,
        entities: List<Entity>
    ): List<Pair<Int, List<String>>> {
        // Define wool color order
        val orderedWools = listOf(
            Blocks.PURPLE_WOOL,
            Blocks.BLUE_WOOL,
            Blocks.CYAN_WOOL,
            Blocks.LIGHT_BLUE_WOOL,
            Blocks.GREEN_WOOL,
            Blocks.LIME_WOOL,
            Blocks.ORANGE_WOOL,
            Blocks.YELLOW_WOOL,
            Blocks.RED_WOOL
        )
        val woolIndexMap = orderedWools.withIndex().associate { it.value to it.index }

        // Map of wool index -> mutable list of armor stand names
        val indexToNames = mutableMapOf<Int, MutableList<String>>()

        entities
            .filterIsInstance<ArmorStandEntity>()
            .forEach { armorStand ->
                val blockBelow = world.getBlockState(armorStand.blockPos.down()).block
                val index = woolIndexMap[blockBelow]
                if (index != null) {
                    val name = armorStand.displayName?.string ?: "<no name>"
                    indexToNames.getOrPut(index) { mutableListOf() }.add(name)
                }
            }

        // Create result as a list, preserving the wool color order (even if some indices are empty)
        return (0 until orderedWools.size).map { idx ->
            idx to (indexToNames[idx] ?: emptyList())
        }
    }


}