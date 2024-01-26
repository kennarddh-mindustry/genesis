package com.github.kennarddh.mindustry.genesis.core.handlers

abstract class Handler {
    open fun onInit() {}

    /**
     * Called when the applications exit gracefully, either through `Core.app.exit()` or through a window closing.
     * Never called after a crash, unlike dispose().
     */
    open fun onExit() {}

    /**
     * Called when the Application is destroyed.
     */
    open fun onDispose() {}
}