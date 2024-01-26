package com.github.kennarddh.mindustry.genesis.core.commons.priority

class MutablePriorityList<E> : ArrayList<PriorityContainer<E>>(), MutableList<PriorityContainer<E>> {
    fun forEachPrioritized(action: (E) -> Unit) {
        this.sorted().forEach { action(it.data) }
    }
}