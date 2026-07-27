package com.iamapo.timetracker.ui

import androidx.compose.runtime.Composable

/**
 * Configures the platform Android context that Compose Multiplatform's
 * resource reader needs when running inside Android Studio's Preview
 * renderer. No-op on platforms where resource loading doesn't need it.
 */
@Composable
expect fun ComposePreviewContext()
