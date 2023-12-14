package kennarddh.genesis.core.commons

class PriorityMutableList<E> : ArrayList<PriorityContainer<E>>(), MutableList<PriorityContainer<E>> {
    fun forEachPrioritized(action: (E) -> Unit) {
        this.sorted().forEach { action(it.data) }
    }

}