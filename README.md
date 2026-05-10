# 🐊 LaterGator

Eine minimalistische Android-App für Menschen mit ADHS, die kurze Aufgaben sofort festhalten und auf einen späteren Zeitpunkt verschieben können — ohne von komplexen Menüs abgelenkt zu werden.

> „Ruf mich später nochmal an" — einfach tippen, Snooze drücken, fertig.

---

## Features

- **Sofort-Eingabe** — ein Textfeld, keine Menüs, keine Ablenkung
- **Snooze-Buttons** mit relativen Zeitangaben:
  - `+2h` — Standard, auch per Enter-Taste
  - `+Heute Abend` — 19:00 Uhr
  - `+Morgen früh` — 08:00 Uhr
  - `+Nächste Woche` — Montag, 09:00 Uhr
  - `+Eigene Zeit…` — Datum- und Uhrzeitpicker
- **Haptisches Feedback** — kurzes Vibrieren beim Speichern
- **Erinnerungen bearbeiten** — Beschreibung und Zeit nachträglich ändern
- **Sofortiges Feedback** — 🐊-Animation nach dem Speichern
- **Notification-Aktionen** — direkt aus der Benachrichtigung: Erledigt / +10 Min / +2 Std
- **Auto-Resnooze** — wird eine Benachrichtigung weggewischt, kommt sie nach 2h automatisch wieder
- **Gerätestart** — alle offenen Erinnerungen werden nach einem Neustart neu geplant
- **Dark Mode** — System / Hell / Dunkel / Zeitplan (automatisch nach Uhrzeit)
- **Ruhemodus** — keine Benachrichtigungen in konfigurierbaren Nacht-Stunden
- **Verlauf** — erledigte und verpasste Erinnerungen einsehen
- **Homescreen-Widget** — zeigt Anzahl offener Erinnerungen + nächste fällige
- **Orts-Tags** — erkennt automatisch Schlüsselwörter (Supermarkt, Apotheke, Baumarkt …) und markiert Erinnerungen mit einem Tag

---

## Technologie-Stack

| Bereich | Technologie |
|---|---|
| Sprache | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architektur | MVVM |
| Datenbank | Room 2.6.1 |
| Einstellungen | DataStore Preferences |
| Benachrichtigungen | AlarmManager (`setExactAndAllowWhileIdle`) |
| Widget | AppWidgetProvider + RemoteViews |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

**Warum AlarmManager statt WorkManager?**
WorkManager kann bei aktivem Energiesparmodus verzögert werden. Für ADHS-Nutzer ist Timing-Präzision entscheidend — ein Alarm der 20 Minuten zu spät kommt ist wertlos.

---

## Projektstruktur

```
app/src/main/
├── java/com/latergator/app/
│   ├── LaterGatorApp.kt              # Application-Klasse, Notification-Channel
│   ├── MainActivity.kt               # Entry-Point, Screen-Navigation, Permission-Handling
│   ├── data/
│   │   ├── Reminder.kt               # Entity + ReminderStatus Enum + placeType
│   │   ├── ReminderDao.kt            # Room DAO
│   │   ├── ReminderDatabase.kt       # Room Database (Singleton, Migrations)
│   │   ├── ReminderRepository.kt     # Single Source of Truth
│   │   ├── AppSettingsRepository.kt  # DataStore Preferences (Dark Mode, Ruhemodus)
│   │   └── PlaceTypeDetector.kt      # Keyword-basierte Orts-Erkennung
│   ├── ui/
│   │   ├── MainScreen.kt             # Hauptscreen mit Eingabe + Erinnerungsliste
│   │   ├── HistoryScreen.kt          # Verlauf (erledigt / verpasst)
│   │   ├── SettingsScreen.kt         # Dark Mode + Ruhemodus-Einstellungen
│   │   ├── EditReminderDialog.kt     # Erinnerung bearbeiten
│   │   ├── TimeOnlyPickerDialog.kt   # Uhrzeit-Picker
│   │   ├── ReminderViewModel.kt      # Business Logic, Zeit-Berechnung
│   │   ├── ReminderViewModelFactory.kt
│   │   ├── SettingsViewModel.kt
│   │   ├── SettingsViewModelFactory.kt
│   │   └── theme/                    # Color, Type, Theme (Gator-Grün + Dark)
│   ├── notification/
│   │   ├── NotificationHelper.kt     # AlarmManager, Notification-Builder
│   │   ├── ReminderReceiver.kt       # BroadcastReceiver für Alarme & Aktionen
│   │   └── BootReceiver.kt           # Alarme nach Gerätestart neu planen
│   └── widget/
│       └── LaterGatorWidget.kt       # Homescreen-Widget (AppWidgetProvider)
└── res/
    ├── drawable/                     # Launcher-Icon, Widget-Hintergrund
    ├── layout/                       # Widget-Layout (RemoteViews)
    ├── mipmap-anydpi-v26/            # Adaptive Icons
    ├── mipmap-{mdpi…xxxhdpi}/        # Legacy Icon-PNGs
    ├── xml/                          # Widget-Metadaten
    └── values/                       # strings, colors, themes
```

---

## Setup

### Voraussetzungen
- Android Studio Hedgehog (2023.1) oder neuer
- JDK 17
- Android SDK 34

### Projekt öffnen
```bash
git clone https://github.com/autrion/LaterGator.git
```
Dann in Android Studio: **File → Open → LaterGator** auswählen.

Android Studio lädt den Gradle-Wrapper automatisch herunter.

### Alternativ: CLI-Build
```bash
# Gradle-Wrapper einmalig generieren (Gradle muss installiert sein)
gradle wrapper --gradle-version=8.9

# Debug-APK bauen
./gradlew assembleDebug

# Direkt auf angeschlossenem Gerät installieren
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Berechtigungen
Die App fragt beim ersten Start folgende Berechtigungen an:

| Berechtigung | Zweck |
|---|---|
| `POST_NOTIFICATIONS` | Erinnerungen als Benachrichtigung anzeigen (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Exakte Alarmplanung (Android 12+, ggf. manuell in Einstellungen) |
| `RECEIVE_BOOT_COMPLETED` | Erinnerungen nach Gerätestart wiederherstellen |

---

## Designprinzipien (ADHS-spezifisch)

1. **Immediate Feedback** — die 🐊-Animation nach dem Speichern bestätigt die Aktion sofort und reduziert Unsicherheit
2. **No Distractions** — keine Navigationsleiste, keine Tabs, minimale UI
3. **Relative Zeitangaben** — `+2h` statt `14:35 Uhr`, weil ADHS-Hirne relative Zeit besser verarbeiten
4. **Niedrige Hürde** — Text eintippen + einen Button drücken = fertig
5. **Fehlertoleranz** — weggewischte Notification? Kommt automatisch nach 2h wieder
6. **Haptisches Feedback** — physische Bestätigung beim Speichern reduziert Zweifel

---

## Lizenz

MIT License — siehe [LICENSE](LICENSE)
