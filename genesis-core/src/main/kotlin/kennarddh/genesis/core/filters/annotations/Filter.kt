package kennarddh.genesis.core.filters.annotations

import kennarddh.genesis.core.commons.priority.PriorityEnum
import kennarddh.genesis.core.filters.FilterType


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Filter(val type: FilterType, val priority: PriorityEnum)
