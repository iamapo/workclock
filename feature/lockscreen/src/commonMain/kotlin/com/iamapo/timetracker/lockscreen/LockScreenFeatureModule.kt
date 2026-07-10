package com.iamapo.timetracker.lockscreen

import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin entry point of the lock-screen feature. */
fun lockScreenFeatureModule(
    controller: LockScreenStatusController = NoOpLockScreenStatusController
): Module = module {
    single<LockScreenStatusController> { controller }
}
