package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayEventEditor
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.presentation.state.TimelineEditUiModel
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.presentation.state.TimelineKind

internal class TimelineMapper {
    fun map(day: WorkDay, endMinute: Int, nowMinute: Int): List<TimelineItemUiModel> {
        val eventItems = day.events.mapIndexed { index, event ->
            TimelineItemUiModel(
                time = TimeTextFormatter.clock(event.minuteOfDay),
                title = event.title,
                kind = when (event.kind) {
                    WorkEventKind.Work -> TimelineKind.Work
                    WorkEventKind.Break -> TimelineKind.Break
                    WorkEventKind.Target -> TimelineKind.Target
                },
                edit = WorkDayEventEditor.editableRange(day, index, nowMinute)?.let { range ->
                    TimelineEditUiModel(
                        eventIndex = index,
                        minuteOfDay = event.minuteOfDay,
                        earliestMinute = range.first,
                        latestMinute = range.last
                    )
                }
            )
        }

        if (day.status == WorkStatus.Finished) return eventItems

        return eventItems + TimelineItemUiModel(
            time = TimeTextFormatter.clock(endMinute),
            title = "Geplanter Feierabend",
            kind = TimelineKind.Target
        )
    }
}
