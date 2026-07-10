package com.iamapo.timetracker.presentation

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

internal fun localized(resource: StringResource, vararg args: Any): String =
    runBlocking { getString(resource, *args) }
