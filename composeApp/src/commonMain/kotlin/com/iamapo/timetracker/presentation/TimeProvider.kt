package com.iamapo.timetracker.presentation

fun interface TimeProvider {
    fun now(): TimeSnapshot
}
