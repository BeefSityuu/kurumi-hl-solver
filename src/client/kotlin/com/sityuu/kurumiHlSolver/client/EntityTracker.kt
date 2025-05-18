package com.sityuu.kurumiHlSolver.client

/* ---------- Fabric / Minecraft ---------- */
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient          // the lambda parameter’s type
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box                   // returned by boundingBox.expand()

/* ---------- Kotlin / Java std-lib ---------- */

@Environment(EnvType.CLIENT)
object EntityTracker {
    private const val RANGE = 32.0
    private var ticks = 0
    private val _nearby = mutableListOf<Entity>()
    val nearby: List<Entity> get() = _nearby   // read-only view

    fun register() =
        ClientTickEvents.END_CLIENT_TICK.register { client: MinecraftClient ->
            if (++ticks % 20 != 0) return@register
            val player = client.player ?: return@register
            val box: Box = player.boundingBox.expand(RANGE)

            _nearby.clear()
            _nearby += client.world?.getOtherEntities(
                player, box
            ) { e -> e is LivingEntity && !e.isSpectator } ?: emptyList()
        }

    fun containsEntityWithName(name: String): Boolean {
        return nearby.any { it.displayName?.string == name }
    }

    fun getEntityNamesContaining(substring: String, ignoreCase: Boolean = false): List<String> {
        return nearby
            .mapNotNull { it.displayName?.string }
            .filter { it.contains(substring, ignoreCase) }
    }


}
