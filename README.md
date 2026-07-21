# WorkClock

Kotlin Multiplatform work time tracker with a shared Compose UI, MVVM state, and Android/iOS hosts.

## Structure

- `composeApp`: app shell, navigation, composition root, and platform-specific iOS entry point
- `androidApp`: Android host and Android-specific integrations
- `feature/timetracking`: time tracking, timeline, and Today UI
- `feature/calendar`: calendar state, editing, and calendar UI
- `feature/settings`: settings state and settings UI
- `feature/backup`: backup import/export
- `feature/lockscreen`: cross-platform lock screen coordination
- `core/domain`: domain model, repository contracts, calculations, and use cases
- `core/data`: persistence and platform-specific data infrastructure
- `core/design`: shared theme and reusable UI building blocks
- `core/resources`: localized Compose resources and text formatting
- `iosApp`: SwiftUI host app with Xcode scheme and iOS preview
- `mockups/time-tracker`: original HTML/CSS mockup
- `mockups/workclock-playful`: newer colorful V2 mockup concept with Apple Watch interface

## Architecture

WorkClock uses a feature-oriented modular monolith with a Clean Architecture dependency direction:

```text
androidApp / iosApp
        |
    composeApp                 App shell and composition root
        |
 feature:*  ------------->  core:design / core:resources
        |
    core:data  ----------->  core:domain
```

- `core:domain` knows nothing about UI or platform code. Domain actions such as start, pause, and stop are passed to use cases as typed `TimeTrackingCommand`s.
- Each feature owns its own UI state, mapper, view model, and Compose surface. That keeps tracking, calendar, and settings independently evolvable and testable.
- `composeApp` wires the features together, manages tab navigation, and injects the `WorkClockDependencies` created by the platform hosts.
- External side effects sit behind interfaces or coordinators. For example, `LockScreenStatusCoordinator` publishes lock screen state independently of the tracking view model.
- Data flow is unidirectional: UI action -> ViewModel -> Use case/repository -> StateFlow -> UI state.
- Feature modules do not rely on implementation details from other features. Cross-feature presentation is composed by the app shell; stable shared building blocks belong in an appropriate `core:*` module.

## Previews

- Android: `composeApp/src/androidMain/kotlin/com/iamapo/timetracker/AndroidTimeTrackerPreview.kt`
- iOS: `iosApp/iosApp/TimeTrackerPreview.swift`

## Verified Commands

```bash
./gradlew :androidApp:compileDebugKotlin --offline
./gradlew :composeApp:compileKotlinIosSimulatorArm64 --offline
./gradlew :composeApp:iosSimulatorArm64Test :core:domain:testDebugUnitTest :feature:settings:iosSimulatorArm64Test --offline
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```
