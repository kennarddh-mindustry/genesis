package com.github.kennarddh.mindustry.genesis.core.filters.annotations

import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority
import com.github.kennarddh.mindustry.genesis.core.filters.FilterType


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Filter(val type: FilterType, val priority: Priority)
