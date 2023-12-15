package kennarddh.genesis.core.commons

data class PriorityContainer<T>(val priority: PriorityEnum, val data: T) : Comparable<PriorityContainer<T>> {
    override fun compareTo(other: PriorityContainer<T>): Int {
        return priority.value - other.priority.value
    }

}
