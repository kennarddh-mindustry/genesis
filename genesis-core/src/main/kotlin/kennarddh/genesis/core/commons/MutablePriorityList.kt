package kennarddh.genesis.core.commons

class MutablePriorityList<E> : ArrayList<PriorityContainer<E>>(), MutableList<PriorityContainer<E>> {
    fun forEachPrioritized(action: (E) -> Unit) {
        this.sorted().forEach { action(it.data) }
    }

}