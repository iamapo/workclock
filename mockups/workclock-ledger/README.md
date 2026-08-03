# WorkClock — Ledger Design (gewaehlte Richtung, ausgearbeitet)

Die "Ledger"-Richtung aus `mockups/workclock-fresh` wurde als Favorit ausgewaehlt und hier auf alle
Kernscreens der App ausgeweitet. Gruenes Kolumnenpapier, tintenblaue Serifenschrift, ein roter
Eintragsstempel und eine rote Randlinie ziehen sich durch jede Ansicht — der Tag wird wie ein
gebuchter Stundenzettel dargestellt, jede Zeile ein Beleg.

## Enthaltene Screens

- **Heute** — aktueller Eintrag mit Restzeit, Feierabend-Prognose, Fortschrittsbalken und den
  Buchungen des Tages als Belegliste.
- **Kalender** — Wochenkonto, eine Legende der Statusfarben und ein Monatsraster (Erledigt, Heute,
  Geplant, Urlaub/Feiertag, Krank, Wochenende), darunter die Detailzeile fuer den gewaehlten Tag
  (Pause anpassen, in Urlaub/Krank umwandeln, Eintrag loeschen).
- **Einstellungen** — Vorgaben (Pause, Sperrbildschirm-Status, Erinnerungen), Arbeitsplan pro
  Wochentag, Feiertage (inkl. Bundesland-Auswahl), Backup (Export/Import mit Statusmeldung) und
  Daten loeschen — jede Sektion eine eigene Karteikarte.
- **Apple Watch** — dieselbe Tinte und derselbe Stempel auf dunklem Ziffernblatt, plus drei
  Komplikationen (Heute, Pause, Saldo).
- **Dialoge** — "Arbeitstag bearbeiten" (Zeiten anpassen, berechnete Arbeitszeit) und "Bundesland
  waehlen" (Radioliste), beide als eingelegtes Formularblatt im selben Karten-Stil.

## Naechste Schritte

- Light/Dark-Feinschliff fuer das produktive Compose-Theme ableiten (Farbtokens in
  `core/design/.../AppColors.kt`).
- Weitere Zustaende der Heute-Karte durchspielen (Bereit, Pause, Feierabend).
- Pruefen, ob die Rasterlinie (Ledger-Linien) auf echten Geraeten performant bleibt, sonst als
  Bild-Hintergrund statt Repeat-Gradient umsetzen.

## Datei

`index.html` kann direkt im Browser geoeffnet werden (verlinkt `styles.css`).
