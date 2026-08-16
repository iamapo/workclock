import { useState } from "react";
import {
  CalendarIcon,
  CheckCircledIcon,
  ClockIcon,
  GearIcon,
  ListBulletIcon,
} from "@radix-ui/react-icons";
import "@fontsource/roboto/400.css";
import "@fontsource/roboto/500.css";
import "@fontsource/roboto/700.css";
import "@fontsource/roboto/900.css";
import { MobileScroll } from "./mobile";

type WorkState = "working" | "paused" | "finished";

type TimelineItem = {
  time: string;
  title: string;
  detail?: string;
  tone: "mint" | "sand";
};

const timeline: TimelineItem[] = [
  { time: "08:02", title: "Arbeitsbeginn", tone: "mint" },
  { time: "10:14", title: "Pause", detail: "30 Min.", tone: "sand" },
  { time: "10:44", title: "Weitergearbeitet", tone: "mint" },
];

export default function Prototype() {
  const [workState, setWorkState] = useState<WorkState>("working");

  const isPaused = workState === "paused";
  const isFinished = workState === "finished";

  const handlePrimaryAction = () => {
    if (isFinished) {
      setWorkState("working");
      return;
    }
    setWorkState(isPaused ? "working" : "paused");
  };

  return (
    <div className="workclock-app" data-state={workState}>
      <MobileScroll className="app-screen">
        <main className="screen-content" aria-label="WorkClock Heute">
          <section className="hero" aria-labelledby="today-title">
            <div className="hero-copy">
              <h1 id="today-title">Heute</h1>
              <p className="date">So, 16. August</p>

              <div className="work-status" aria-live="polite">
                <span className="status-dot" aria-hidden="true" />
                {isFinished ? "TAG BEENDET" : isPaused ? "IN PAUSE" : "AM ARBEITEN"}
              </div>

              <p className="hero-kicker">
                {isFinished ? "HEUTE GEARBEITET" : isPaused ? "PAUSE SEIT 14:46" : "VORAUSSICHTLICH FEIERABEND"}
              </p>
              <p className="hero-time">{isFinished ? "06:44" : isPaused ? "00:12" : "17:34"}</p>
              <p className="hero-remaining">
                {isFinished ? "Tag erfolgreich abgeschlossen" : isPaused ? "Pflichtpause noch 0:03" : "noch 2:48"}
              </p>
            </div>
          </section>

          <section className="day-progress" aria-label="Tagesverlauf">
            <div className="time-labels" aria-hidden="true">
              <span>08:02</span>
              <strong>{isFinished ? "Beendet 14:46" : isPaused ? "Pause 14:46" : "Jetzt 14:46"}</strong>
              <span>17:34</span>
            </div>

            <div className="progress-track" role="img" aria-label="65 Prozent des Tagesziels erreicht">
              <span className="track-work track-work-first" />
              <span className="track-pause" />
              <span className="track-work track-work-second" />
              <span className="track-rest" />
              <span className="now-marker" />
            </div>

            <div className="progress-legend">
              <span><i className="legend-dot mint" />Gearbeitet 4:44</span>
              <span><i className="legend-dot sand" />Pause 1:28</span>
              <span><i className="legend-dot rest" />Rest 2:48</span>
            </div>

            <div className="progress-value">
              <strong>{isFinished ? "100 %" : "65 %"}</strong>
              <span>{isFinished ? "gebucht" : "des Solls"}</span>
            </div>
          </section>

          <section className="actions" aria-label="Zeiterfassung steuern">
            <button className="primary-action" type="button" onClick={handlePrimaryAction}>
              {isFinished ? "Neuen Tag beginnen" : isPaused ? "Arbeit fortsetzen" : "Pause starten"}
            </button>
            {!isFinished && (
              <button className="secondary-action" type="button" onClick={() => setWorkState("finished")}>
                Tag beenden
              </button>
            )}
          </section>

          <section className="bookings" aria-labelledby="bookings-title">
            <div className="section-heading">
              <div className="section-title">
                <span className="section-icon" aria-hidden="true"><ListBulletIcon /></span>
                <h2 id="bookings-title">Heute gebucht</h2>
              </div>
              <p>Woche +1:12 h</p>
            </div>

            <div className="timeline-list">
              {timeline.map((entry) => (
                <div className="timeline-row" key={`${entry.time}-${entry.title}`}>
                  <span className={`timeline-dot ${entry.tone}`} aria-hidden="true" />
                  <time>{entry.time}</time>
                  <p>
                    <strong>{entry.title}</strong>
                    {entry.detail ? <span> · {entry.detail}</span> : null}
                  </p>
                </div>
              ))}
              {isPaused && (
                <div className="timeline-row live-row">
                  <span className="timeline-dot coral" aria-hidden="true" />
                  <time>14:46</time>
                  <p><strong>Pause gestartet</strong></p>
                </div>
              )}
              {isFinished && (
                <div className="timeline-row live-row">
                  <span className="timeline-dot coral" aria-hidden="true" />
                  <time>14:46</time>
                  <p><strong>Feierabend</strong></p>
                </div>
              )}
            </div>
          </section>
        </main>
      </MobileScroll>

      <nav className="bottom-nav" aria-label="Hauptnavigation">
        <button className="nav-item is-active" type="button" aria-current="page">
          <span className="nav-icon"><ClockIcon /></span>
          <span>Heute</span>
        </button>
        <button className="nav-item" type="button" aria-label="Kalender – nicht Teil dieses Prototyps">
          <span className="nav-icon"><CalendarIcon /></span>
          <span>Kalender</span>
        </button>
        <button className="nav-item" type="button" aria-label="Einstellungen – nicht Teil dieses Prototyps">
          <span className="nav-icon"><GearIcon /></span>
          <span>Einstellungen</span>
        </button>
      </nav>

      <div className="state-confirmation" role="status" aria-live="polite">
        <CheckCircledIcon />
        {isFinished ? "Arbeitstag gespeichert" : isPaused ? "Pause läuft" : "Arbeitszeit läuft"}
      </div>
    </div>
  );
}
