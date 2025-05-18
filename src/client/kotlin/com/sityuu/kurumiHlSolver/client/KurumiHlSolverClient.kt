package com.sityuu.kurumiHlSolver.client

import net.fabricmc.api.ClientModInitializer

class KurumiHlSolverClient : ClientModInitializer {

    override fun onInitializeClient() {
        println("aa")
        EntityTracker.register()
        NearbyHudRenderer.register()
        HighLow.register()
    }
}
