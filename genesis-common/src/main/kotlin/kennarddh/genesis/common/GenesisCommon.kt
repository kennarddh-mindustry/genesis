package kennarddh.genesis.common

import arc.util.Log
import kennarddh.genesis.common.handlers.foo.FooHandler
import kennarddh.genesis.common.handlers.server.ServerHandler
import kennarddh.genesis.core.Genesis
import kennarddh.genesis.core.common.AbstractPlugin

@Suppress("unused")
class GenesisCommon : AbstractPlugin() {
    override fun init() {
        Log.info("[GenesisCommon] Loaded")

        Genesis.addHandler(ServerHandler())
        Genesis.addHandler(FooHandler())
    }
}