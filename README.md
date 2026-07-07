# WorkClock

Kotlin-Multiplatform-Arbeitszeittracker mit gemeinsamer Compose-UI, MVVM-State und Android-/iOS-Hosts.

## Struktur

- `composeApp/src/commonMain`: gemeinsames Domain-Modell, ViewModel, Mapper und Compose UI
- `composeApp/src/androidMain`: Android Activity und Android Studio Preview
- `composeApp/src/iosMain`: Compose `UIViewController` fuer iOS
- `iosApp`: SwiftUI Host-App mit Xcode-Scheme und iOS Preview
- `mockups/time-tracker`: urspruengliches HTML/CSS-Mockup

## Architektur

- `TimeTrackerViewModel` haelt den aktuellen Arbeitstag und verarbeitet Start, Pause, Weiterarbeiten und Beenden.
- `TimeTrackerUiStateMapper` berechnet Restzeit, konkrete Ziel-Uhrzeit, Pausenstand, Wochenstand, Timeline und Kalenderdaten.
- Jedes sichtbare Compose-Bauteil liegt als eigenes `object` mit eigenem `@Composable operator fun invoke(...)` in einer eigenen Datei unter `ui/components`.

## Previews

- Android: `composeApp/src/androidMain/kotlin/com/iamapo/timetracker/AndroidTimeTrackerPreview.kt`
- iOS: `iosApp/iosApp/TimeTrackerPreview.swift`

## Gepruefte Befehle

```bash
./gradlew :composeApp:compileDebugKotlinAndroid --offline
./gradlew :composeApp:compileKotlinIosSimulatorArm64 --offline
./gradlew :composeApp:allTests --offline
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```
