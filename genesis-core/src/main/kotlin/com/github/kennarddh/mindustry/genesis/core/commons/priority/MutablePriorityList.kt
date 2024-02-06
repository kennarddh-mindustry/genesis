package com.github.kennarddh.mindustry.genesis.core.commons.priority

class MutablePriorityList<E> : ArrayList<PriorityContainer<E>>(), MutableList<PriorityContainer<E>> {
    inline fun forEachPrioritized(action: (E) -> Unit) {
        sorted().forEach { action(it.data) }
    }
}