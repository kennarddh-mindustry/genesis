package com.github.kennarddh.mindustry.genesis.core.handlers

abstract class Handler {
    /**
     * Called when handler registration
     */
    open suspend fun onInit() {}

    /**
     * Called after handler registration
     */
    open suspend fun onRegistered() {}

    /**
     * Called when the Application is destroyed.
     */
    open suspend fun onDispose() {}
}