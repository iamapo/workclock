package com.iamapo.timetracker.presentation

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val calendarFeatureModule = module {
    factoryOf(::CalendarViewModel)
}
