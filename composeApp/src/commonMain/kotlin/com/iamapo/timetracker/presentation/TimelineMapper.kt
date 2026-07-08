package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.presentation.state.TimelineKind

internal class TimelineMapper {
    fun map(day: WorkDay, endMinute: Int): List<TimelineItemUiModel> =
        day.events.map { event ->
            TimelineItemUiModel(
                time = TimeTextFormatter.clock(event.minuteOfDay),
                title = event.title,
                kind = when (event.kind) {
                    WorkEventKind.Work -> TimelineKind.Work
                    WorkEventKind.Break -> TimelineKind.Break
                    WorkEventKind.Target -> TimelineKind.Target
                }
            )
        } + TimelineItemUiModel(
            time = TimeTextFormatter.clock(endMinute),
            title = "Geplanter Feierabend",
            kind = TimelineKind.Target
        )
}
