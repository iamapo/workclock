# WorkClock

Kotlin-Multiplatform-Arbeitszeittracker mit gemeinsamer Compose-UI, MVVM-State und Android-/iOS-Hosts.

## Struktur

- `composeApp`: App-Shell, Navigation, Composition Root und plattformspezifischer iOS-Einstieg
- `androidApp`: Android-Host und Android-spezifische Integrationen
- `feature/timetracking`: Zeiterfassung, Timeline und Today-UI
- `feature/calendar`: Kalenderzustand, Bearbeitung und Kalender-UI
- `feature/settings`: Einstellungszustand und Settings-UI
- `feature/backup`: Import/Export von Sicherungen
- `feature/lockscreen`: plattformuebergreifende Lock-Screen-Koordination
- `core/domain`: Domain-Modell, Repository-Vertraege, Berechnungen und Use Cases
- `core/data`: Persistenz und plattformspezifische Dateninfrastruktur
- `core/design`: gemeinsames Theme und wiederverwendbare UI-Bausteine
- `core/resources`: lokalisierte Compose-Ressourcen und Textformatierung
- `iosApp`: SwiftUI Host-App mit Xcode-Scheme und iOS Preview
- `mockups/time-tracker`: urspruengliches HTML/CSS-Mockup
- `mockups/workclock-playful`: neuer, bunter V2-Mockup-Entwurf mit Apple-Watch-Interface

## Architektur

WorkClock verwendet einen feature-orientierten modularen Monolithen mit Clean-Architecture-Abhaengigkeitsrichtung:

```text
androidApp / iosApp
        |
    composeApp                 App-Shell und Composition Root
        |
 feature:*  ------------->  core:design / core:resources
        |
    core:data  ----------->  core:domain
```

- `core:domain` kennt weder UI noch Plattformcode. Fachliche Aktionen wie Start, Pause und Stopp werden als typisierte `TimeTrackingCommand`s an Use Cases uebergeben.
- Jedes Feature besitzt seinen eigenen UI-State, Mapper, ViewModel und seine Compose-Oberflaeche. Tracking, Kalender und Einstellungen koennen dadurch unabhaengig weiterentwickelt und getestet werden.
- `composeApp` verbindet die Features, steuert die Tab-Navigation und injiziert die in den Plattform-Hosts erzeugten `WorkClockDependencies`.
- Externe Seiteneffekte liegen hinter Schnittstellen beziehungsweise Koordinatoren. Beispielsweise publiziert `LockScreenStatusCoordinator` den Lock-Screen-Zustand unabhaengig vom Tracking-ViewModel.
- Der Datenfluss ist unidirektional: UI-Aktion -> ViewModel -> Use Case/Repository -> StateFlow -> UI-State.
- Feature-Module greifen nicht auf Implementierungsdetails anderer Features zu. Uebergreifende Darstellung wird von der App-Shell zusammengesetzt; stabile gemeinsame Bausteine gehoeren in ein passendes `core:*`-Modul.

## Previews

- Android: `composeApp/src/androidMain/kotlin/com/iamapo/timetracker/AndroidTimeTrackerPreview.kt`
- iOS: `iosApp/iosApp/TimeTrackerPreview.swift`

## Gepruefte Befehle

```bash
./gradlew :androidApp:compileDebugKotlin --offline
./gradlew :composeApp:compileKotlinIosSimulatorArm64 --offline
./gradlew :composeApp:iosSimulatorArm64Test :core:domain:testDebugUnitTest :feature:settings:iosSimulatorArm64Test --offline
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```
