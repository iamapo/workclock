# WorkClock Mockup

Erster visueller Entwurf fuer eine Kotlin-Multiplatform-App mit iOS/macOS/Android-Client und Apple-Watch-Begleitung.

## Kernscreen

- Status des aktuellen Arbeitstags: nicht gestartet, aktiv, pausiert, beendet
- Tagesziel, Pflichtpause und Wochenziel als direkt sichtbare Vorgaben
- Prognose: verbleibende Arbeitszeit und voraussichtlicher Feierabend
- Tageswerte: gearbeitete Zeit, Pause heute, fehlende Pause, letzte Pause
- Wochenkonto: Ist-Stunden, Soll-Stunden und Saldo
- Timeline fuer Start, Pausen, Fortsetzung und geplantes Ende
- Prominente Ziel-Uhrzeit: bis wann heute gearbeitet werden muss
- Kalenderansicht mit Tagesstatus, geplanten Tagen und Wochen-Summen
- Dunkles App-Design passend zur Apple-Watch-Ansicht

## Apple Watch

Die Watch-App fokussiert auf kurze Interaktionen:

- Arbeitstag starten
- Pause starten oder beenden
- Arbeitstag beenden
- Restzeit und voraussichtliches Ende anzeigen

## Naechster Umsetzungsschritt

Der Entwurf kann als Compose-Multiplatform-Screen umgesetzt werden. Sinnvolle erste Datenmodelle waeren `WorkDay`, `WorkSession`, `BreakSession`, `DailyTarget` und `WeekSummary`.
