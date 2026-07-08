package com.iamapo.timetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.iamapo.timetracker.data.AndroidWorkDayStore
import com.iamapo.timetracker.ui.TimeTrackerRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val workDayStore = remember { AndroidWorkDayStore(applicationContext) }
            TimeTrackerRoute(workDayStore = workDayStore)
        }
    }
}
