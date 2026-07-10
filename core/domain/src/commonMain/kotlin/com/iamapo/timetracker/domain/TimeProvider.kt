package com.iamapo.timetracker.domain

fun interface TimeProvider {
    fun now(): TimeSnapshot
}
