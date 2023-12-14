package kennarddh.genesis.core.commons

data class PriorityContainer<T>(val data: T, val priority: Int) : Comparable<PriorityContainer<T>> {
    override fun compareTo(other: PriorityContainer<T>): Int {
        return priority - other.priority
    }

}
