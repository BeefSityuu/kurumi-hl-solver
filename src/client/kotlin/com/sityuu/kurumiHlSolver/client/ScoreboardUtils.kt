package com.sityuu.kurumiHlSolver.client

import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.ScoreboardEntry
import net.minecraft.text.Text
import net.minecraft.world.World

object ScoreboardUtils {
    fun sidebarLines(world: World): List<Pair<String, Int>> {
        val board: Scoreboard = world.scoreboard

        // Which objective is bound to the SIDEBAR slot right now?
        val objective = board.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR)   // :contentReference[oaicite:0]{index=0}
            ?: return emptyList()

        // Entries tracked for that objective
        return board.getScoreboardEntries(objective)                               // :contentReference[oaicite:1]{index=1}
            .filter { !it.hidden() }                                               // ignore ‘fake’/hidden rows
            .sortedByDescending(ScoreboardEntry::value)                            // Minecraft draws high→low
            .map { entry ->
                // Prefer a custom display text, fall back to the owner name
                val text: Text = entry.display() ?: entry.name()                   // :contentReference[oaicite:2]{index=2}
                text.string to entry.value()
            }
    }
}