package com.github.kennarddh.mindustry.genesis.standard

import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin
import com.github.kennarddh.mindustry.genesis.standard.handlers.commands.CommandsHandler
import com.github.kennarddh.mindustry.genesis.standard.handlers.foo.FooHandler
import com.github.kennarddh.mindustry.genesis.standard.handlers.server.ServerHandler

class GenesisStandard : AbstractPlugin() {
    override fun init() {
        Logger.info("Loaded")

        Genesis.registerHandler(CommandsHandler())
        Genesis.registerHandler(ServerHandler())
        Genesis.registerHandler(FooHandler())
    }
}