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
     *
     * Will be run at the same time for every handler's onDispose including AbstractPlugin dispose.
     */
    open suspend fun onDispose() {}
}