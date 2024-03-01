package com.github.kennarddh.mindustry.genesis.standard.commons

import mindustry.gen.Player

val PlayerComparator = Comparator<Player> { player1, player2 -> player1.id - player2.id }