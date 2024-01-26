package com.github.kennarddh.mindustry.genesis.core.handlers

abstract class Handler {
    open fun onInit() {}

    /**
     * Called when the Application is destroyed.
     */
    open fun onDispose() {}
}