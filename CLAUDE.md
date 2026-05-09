# LaterGator – Coding Standards & Architektur

## Projektübersicht
Minimalistische Android-App für Menschen mit ADHS als Wiedervorlage-System (Snooze).

## Technologie-Stack
- **Sprache**: Kotlin
- **UI**: Jetpack Compose (Material3)
- **Architektur**: MVVM
- **Datenbank**: Room 2.6.1
- **Benachrichtigungen**: AlarmManager + BroadcastReceiver (NICHT WorkManager — exakte Zeitplanung ist für ADHS-Nutzer kritisch)
- **Min SDK**: 26 (Android 8.0) — alle Geräte unterstützen Adaptive Icons
- **Target SDK**: 34 (Android 14)

## Paketstruktur
```
com.latergator.app/
├── data/          # Room Entity, DAO, Database, Repository
├── ui/            # Compose Screens, ViewModel, ViewModelFactory
│   └── theme/     # Color, Typography, Theme
└── notification/  # NotificationHelper, ReminderReceiver, BootReceiver
```

## Setup (nach dem Klonen)
Die `gradlew`/`gradlew.bat`-Skripte und `gradle-wrapper.jar` werden nicht committed.
- **Android Studio**: Projekt öffnen → Studio setzt Gradle-Wrapper automatisch auf
- **CLI**: `gradle wrapper --gradle-version=8.9` einmalig ausführen

## Design-Prinzipien (ADHS-spezifisch)
1. **Sofortiges Feedback**: 🐊-Animation erscheint unmittelbar nach dem Speichern
2. **Keine Ablenkung**: Minimale UI, keine unnötigen Menüs
3. **Relative Zeitangaben**: "+2h" statt fixer Uhrzeiten — ADHS-Hirne verstehen relative Zeit besser
4. **Standard-Snooze**: 2 Stunden — auch wenn Benachrichtigung weggewischt wird

## Coding-Standards
- Keine Kommentare außer bei nicht-offensichtlichem Verhalten
- Coroutines ausschließlich in `viewModelScope`
- Repository als Single Source of Truth
- Keine Hilt/DI — einfache Factory-basierte ViewModel-Erstellung
