package com.github.kennarddh.mindustry.genesis.core.handlers

interface Handler {
    /**
     * Called when handler registration
     */
    suspend fun onInit() {}

    /**
     * Called after handler registration
     */
    suspend fun onRegistered() {}

    /**
     * Called when the Application is destroyed.
     *
     * Will be run at the same time for every handler's onDispose including AbstractPlugin onDispose.
     */
    suspend fun onDispose() {}
}