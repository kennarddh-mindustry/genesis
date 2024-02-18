package com.github.kennarddh.mindustry.genesis.standard

import com.github.kennarddh.mindustry.genesis.core.GenesisAPI
import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin
import com.github.kennarddh.mindustry.genesis.standard.handlers.commands.CommandsHandler
import com.github.kennarddh.mindustry.genesis.standard.handlers.foo.FooHandler
import com.github.kennarddh.mindustry.genesis.standard.handlers.server.ServerHandler
import com.github.kennarddh.mindustry.genesis.standard.handlers.tap.DoubleTapHandler

class GenesisStandard : AbstractPlugin() {
    override suspend fun onInit() {
        GenesisAPI.registerHandler(CommandsHandler())
        GenesisAPI.registerHandler(ServerHandler())
        GenesisAPI.registerHandler(FooHandler())
        GenesisAPI.registerHandler(DoubleTapHandler())

        Logger.info("Loaded")
    }
}