package kennarddh.genesis.core.commons.priority

data class PriorityContainer<T>(val priority: PriorityEnum, val data: T) : Comparable<PriorityContainer<T>> {
    override fun compareTo(other: PriorityContainer<T>): Int {
        return other.priority.value - priority.value
    }

}
