package com.github.kennarddh.mindustry.genesis.common

import com.github.kennarddh.mindustry.genesis.common.handlers.commands.CommandsHandler
import com.github.kennarddh.mindustry.genesis.common.handlers.foo.FooHandler
import com.github.kennarddh.mindustry.genesis.common.handlers.server.ServerHandler
import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin

class GenesisCommon : AbstractPlugin() {
    override fun init() {
        Logger.info("Loaded")

        Genesis.addHandler(CommandsHandler())
        Genesis.addHandler(ServerHandler())
        Genesis.addHandler(FooHandler())
    }
}