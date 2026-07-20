package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDayConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsStateMapperTest {
    @Test
    fun mapsConfiguredTargetsAndLimits() {
        val state = SettingsStateMapper.map(
            config = WorkDayConfig(
                dailyTargetMinutes = 7 * 60 + 30,
                requiredBreakMinutes = 30,
                weeklyTargetMinutes = 37 * 60 + 30
            ),
            lockScreenStatusEnabled = true
        )

        assertEquals("7:30 h", state.dailyTarget)
        assertEquals("30 min", state.requiredBreak)
        assertEquals("37:30 h", state.weeklyTarget)
        assertTrue(state.lockScreenStatusEnabled)
        assertTrue(state.canDecreaseDailyTarget)
        assertFalse(SettingsStateMapper.map(WorkDayConfig(requiredBreakMinutes = 0), false).canDecreaseRequiredBreak)
    }
}
