package com.github.kennarddh.mindustry.genesis.standard

import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin
import com.github.kennarddh.mindustry.genesis.standard.handlers.commands.CommandsHandler
import com.github.kennarddh.mindustry.genesis.standard.handlers.foo.FooHandler
import com.github.kennarddh.mindustry.genesis.standard.handlers.server.ServerHandler
import com.github.kennarddh.mindustry.genesis.standard.handlers.tap.DoubleTapHandler

class GenesisStandard : AbstractPlugin() {
    override suspend fun onInit() {
        Genesis.registerHandler(CommandsHandler())
        Genesis.registerHandler(ServerHandler())
        Genesis.registerHandler(FooHandler())
        Genesis.registerHandler(DoubleTapHandler())

        Logger.info("Loaded")
    }
}