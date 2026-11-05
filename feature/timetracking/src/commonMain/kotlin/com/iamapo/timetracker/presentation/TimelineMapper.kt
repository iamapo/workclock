package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayEventEditor
import com.iamapo.timetracker.domain.WorkDaySummary
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.presentation.state.DayProgressSegmentUiModel
import com.iamapo.timetracker.presentation.state.TimelineEditUiModel
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.presentation.state.TimelineKind

internal class TimelineMapper {
    fun map(day: WorkDay, endMinute: Int, nowMinute: Int): List<TimelineItemUiModel> {
        val eventItems = day.events.mapIndexed { index, event ->
            TimelineItemUiModel(
                time = TimeTextFormatter.clock(event.minuteOfDay),
                title = event.title,
                kind = timelineKind(event),
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

    /** Splits the day into its phases in booking order, for example work, break, work. */
    fun progressSegments(
        day: WorkDay,
        summary: WorkDaySummary,
        nowMinute: Int,
        scheduledDayMinutes: Int
    ): List<DayProgressSegmentUiModel> {
        // A finished day ends with its last event, a running one with the current minute.
        val phaseEvents = if (day.status == WorkStatus.Finished) day.events.dropLast(1) else day.events
        val phaseEnds = day.events.drop(1).map(WorkEvent::minuteOfDay) + nowMinute
        val phases = phaseEvents.zip(phaseEnds)
            .filter { (event, _) -> event.kind != WorkEventKind.Target }
            .map { (event, endMinute) ->
                timelineKind(event) to (endMinute - event.minuteOfDay).mod(MinutesPerDay)
            }
            .filter { (_, minutes) -> minutes > 0 }
            // Days entered manually have no phases, only totals.
            .ifEmpty {
                listOf(
                    TimelineKind.Work to summary.workedMinutes,
                    TimelineKind.Break to summary.breakMinutes
                ).filter { (_, minutes) -> minutes > 0 }
            }
        val barMinutes = maxOf(scheduledDayMinutes, phases.sumOf { (_, minutes) -> minutes })
        return phases.map { (kind, minutes) ->
            DayProgressSegmentUiModel(kind, minutes.toFloat() / barMinutes)
        }
    }

    private fun timelineKind(event: WorkEvent): TimelineKind = when (event.kind) {
        WorkEventKind.Work -> TimelineKind.Work
        WorkEventKind.Break -> TimelineKind.Break
        WorkEventKind.Target -> TimelineKind.Target
    }

    private companion object {
        const val MinutesPerDay = 24 * 60
    }
}
