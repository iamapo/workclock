import { useMemo, useState } from "react";
import {
  CalendarBlank,
  CheckCircle,
  CloudCheck,
  GearSix,
  House,
  DotsThree,
  Timer,
} from "@phosphor-icons/react";

const week = [
  ["Mo", "3. Aug.", "7:58", "+0:12", 99],
  ["Di", "4. Aug.", "8:05", "+0:05", 100],
  ["Mi", "5. Aug.", "8:00", "±0:00", 100],
  ["Do", "6. Aug.", "7:45", "−0:15", 97],
  ["Fr", "7. Aug.", "8:10", "+0:10", 100],
  ["Sa", "8. Aug.", "—", "—", 0],
  ["So", "9. Aug.", "4:48", "+0:18", 60],
];

const bookings = [
  ["08:03", "Arbeitsbeginn"],
  ["10:15", "Pause gestartet"],
  ["10:30", "Pause beendet"],
  ["12:45", "Weitergearbeitet"],
];

function Sidebar({ view, setView }) {
  const items = [
    ["today", House, "Heute"],
    ["calendar", CalendarBlank, "Kalender"],
    ["settings", GearSix, "Einstellungen"],
  ];
  return (
    <aside className="sidebar">
      <div className="brand">
        <strong>WorkClock</strong>
        <span>LEDGER</span>
      </div>
      <nav aria-label="Hauptnavigation">
        {items.map(([id, Icon, label]) => (
          <button
            key={id}
            className={view === id ? "nav-item active" : "nav-item"}
            onClick={() => setView(id)}
          >
            <Icon size={19} weight={view === id ? "fill" : "regular"} />
            {label}
          </button>
        ))}
      </nav>
      <div className="profile">
        <span className="avatar">AH</span>
        <span><strong>Anna Huber</strong><small>Persönlich</small></span>
      </div>
    </aside>
  );
}

function TodayView() {
  const [mode, setMode] = useState("working");
  const state = useMemo(() => ({
    working: { stamp: "AM ARBEITEN", remaining: "3:12", finish: "17:15", progress: 63, primary: "Pause starten" },
    paused: { stamp: "IN PAUSE", remaining: "3:12", finish: "17:30", progress: 63, primary: "Weiterarbeiten" },
    finished: { stamp: "ABGESCHLOSSEN", remaining: "0:00", finish: "16:58", progress: 100, primary: "Neuen Tag starten" },
  })[mode], [mode]);

  const primaryAction = () => setMode(mode === "working" ? "paused" : mode === "paused" ? "working" : "working");
  return (
    <div className="workspace today-layout">
      <section className="day-sheet">
        <header className="sheet-heading">
          <div>
            <p>SO, 9. AUGUST 2026</p>
            <h1>Sonntag, 9. August 2026</h1>
            <span>Eintrag Nr. 217</span>
          </div>
          <span className="stamp">{state.stamp}</span>
        </header>

        <div className="time-pair">
          <div><span>RESTZEIT</span><strong>{state.remaining}</strong></div>
          <div><span>FEIERABEND CA.</span><strong>{state.finish}</strong></div>
        </div>
        <div className="progress-track" aria-label={`${state.progress} Prozent des Solls`}>
          <span style={{ width: `${state.progress}%` }} />
        </div>
        <p className="progress-caption">{state.progress}% des Solls · Pflichtpause 45 min</p>
        <div className="action-row">
          <button className="primary-action" onClick={primaryAction}>{state.primary}</button>
          <button className="secondary-action" onClick={() => setMode("finished")}>Feierabend</button>
        </div>

        <div className="metrics">
          <div><span>KOMMEN</span><strong>08:03</strong><small>pünktlich</small></div>
          <div><span>GEPLANTE ARBEITSZEIT</span><strong>8:00 h</strong><small>Soll</small></div>
          <div><span>HEUTE GEARBEITET</span><strong>{mode === "finished" ? "8:12" : "4:48"}</strong><small>+0:18 h</small></div>
        </div>

        <section className="bookings">
          <header><span>HEUTIGE BUCHUNGEN</span><span>BELEG 9.8.</span></header>
          {bookings.map(([time, label]) => (
            <div className="booking-row" key={time}>
              <time>{time}</time><strong>{label}</strong><span><CheckCircle size={15} weight="fill" /> gebucht</span>
            </div>
          ))}
          <div className="booking-row planned">
            <time>{state.finish}</time><strong>Feierabend (geplant)</strong><span>geplant</span>
          </div>
        </section>
        <button className="text-action">Eintrag bearbeiten</button>
      </section>

      <aside className="week-panel">
        <header><span>DIESE WOCHE</span><strong>32. Woche</strong></header>
        <div className="week-list">
          {week.map(([day, date, hours, balance, progress]) => (
            <button className={day === "So" ? "week-row selected" : "week-row"} key={day}>
              <strong>{day}</strong><span>{date}</span><b>{hours}</b>
              <i><em style={{ width: `${progress}%` }} /></i><small>{balance}</small>
            </button>
          ))}
        </div>
        <section className="recent">
          <header>AKTUELLE BUCHUNGEN</header>
          {bookings.slice(0, 3).map(([time, label]) => (
            <div key={time}><time>{time}</time><span>{label}</span><small>gebucht</small></div>
          ))}
        </section>
        <button className="text-action bottom-link">Alle Buchungen anzeigen</button>
      </aside>
    </div>
  );
}

function PlaceholderView({ type }) {
  return (
    <div className="placeholder-view">
      {type === "calendar" ? <CalendarBlank size={44} /> : <GearSix size={44} />}
      <h1>{type === "calendar" ? "Kalender" : "Einstellungen"}</h1>
      <p>{type === "calendar" ? "Die Monats- und Historienansicht folgt in der nächsten Ausarbeitung." : "Hier werden Arbeitszeit, Pausen und Synchronisierung konfiguriert."}</p>
    </div>
  );
}

export function App() {
  const [view, setView] = useState("today");
  return (
    <main className="prototype-stage">
      <section className="mac-window" aria-label="WorkClock Mac-App Mockup">
        <header className="window-toolbar">
          <div className="traffic-lights" aria-hidden="true"><span /><span /><span /></div>
          <div className="toolbar-title"><Timer size={18} weight="fill" /><strong>WorkClock</strong></div>
          <div className="sync"><CloudCheck size={19} weight="fill" /><span>Synchronisiert</span></div>
          <button className="toolbar-more" aria-label="Weitere Optionen"><DotsThree size={20} weight="bold" /></button>
        </header>
        <div className="window-body">
          <Sidebar view={view} setView={setView} />
          {view === "today" ? <TodayView /> : <PlaceholderView type={view} />}
        </div>
      </section>
    </main>
  );
}
