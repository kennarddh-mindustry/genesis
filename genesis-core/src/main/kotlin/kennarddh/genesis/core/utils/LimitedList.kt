package kennarddh.genesis.core.utils

class LimitedListSizeGreaterThanCapacityException(message: String) : Exception(message)

@Suppress("UNUSED")
class LimitedList<E>(val capacity: Int) : Iterable<E> {
    private val backingMutableList: MutableList<E> = mutableListOf()

    constructor(capacity: Int, original: Collection<E>) : this(capacity) {
        if (original.size > capacity)
            throw LimitedListSizeGreaterThanCapacityException("Original size is ${original.size} where capacity is smaller with $capacity size.")

        addAll(original)
    }

    @Suppress("UNUSED")
    fun isEmpty(): Boolean = backingMutableList.isEmpty()

    @Suppress("UNUSED")
    fun isNotEmpty(): Boolean = backingMutableList.isNotEmpty()

    operator fun contains(element: @UnsafeVariance E): Boolean = backingMutableList.contains(element)

    @Suppress("UNUSED")
    fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean =
        backingMutableList.containsAll(elements)

    @Suppress("UNUSED")
    fun remove(element: E): Boolean = backingMutableList.remove(element)

    @Suppress("UNUSED")
    fun removeAll(elements: Collection<E>): Boolean = backingMutableList.removeAll(elements)

    @Suppress("UNUSED")
    fun clear() = backingMutableList.clear()

    @Suppress("UNUSED")
    fun removeAt(index: Int): E = backingMutableList.removeAt(index)

    @Suppress("UNUSED")
    fun subList(fromIndex: Int, toIndex: Int): LimitedList<E> =
        LimitedList(capacity, backingMutableList.subList(fromIndex, toIndex))

    override fun iterator(): Iterator<E> = backingMutableList.iterator()

    @Suppress("MemberVisibilityCanBePrivate")
    fun add(element: E): Boolean {
        if (backingMutableList.size == capacity)
            backingMutableList.removeAt(0)

        backingMutableList.add(element)

        return true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun addAll(elements: Collection<E>): Boolean {
        elements.forEach {
            add(it)
        }

        return true
    }
}