package com.github.kennarddh.mindustry.genesis.core.handlers

abstract class Handler {
    open suspend fun onInit() {}

    /**
     * Called when the Application is destroyed.
     */
    open suspend fun onDispose() {}
}