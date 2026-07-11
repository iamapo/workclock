# WorkClock

Kotlin-Multiplatform-Arbeitszeittracker mit gemeinsamer Compose-UI, MVVM-State und Android-/iOS-Hosts.

## Struktur

- `composeApp/src/commonMain`: gemeinsame Presentation, Mapper und Compose UI
- `androidApp`: eigenstaendige Android-App und Android-spezifische Integrationen
- `core/domain`: Domain-Modell, Repository-Vertrag und Use Cases
- `core/data`: gemeinsame Persistenz sowie plattformspezifische iOS-Infrastruktur
- `composeApp/src/iosMain`: Compose `UIViewController` fuer iOS
- `iosApp`: SwiftUI Host-App mit Xcode-Scheme und iOS Preview
- `mockups/time-tracker`: urspruengliches HTML/CSS-Mockup
- `mockups/workclock-playful`: neuer, bunter V2-Mockup-Entwurf mit Apple-Watch-Interface

## Architektur

- Lifecycle-gebundene ViewModels verarbeiten Aktionen und projizieren den persistierten Zustand auf die UI.
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
