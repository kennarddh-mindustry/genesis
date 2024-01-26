package kennarddh.genesis.common

import kennarddh.genesis.common.handlers.commands.CommandsHandler
import kennarddh.genesis.common.handlers.foo.FooHandler
import kennarddh.genesis.common.handlers.server.ServerHandler
import kennarddh.genesis.core.Genesis
import kennarddh.genesis.core.commons.AbstractPlugin

class GenesisCommon : AbstractPlugin() {
    override fun init() {
        Logger.info("Loaded")

        Genesis.addHandler(CommandsHandler())
        Genesis.addHandler(ServerHandler())
        Genesis.addHandler(FooHandler())
    }
}