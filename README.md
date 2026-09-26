# Injection Tracker

100% Vibe Coded

Personal, local-first Android app for a single recurring injectable (every week or every other week).

The Android app lives at the **repo root** (standard Gradle project).

## Open in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (Koala / 2024.2 or newer is fine) with Android SDK 35 and a JDK 17+.
2. Clone this repository and **Open** the repo root (the folder that contains `settings.gradle.kts`).
3. Let Gradle sync. SDK path is written to `local.properties` by Android Studio.
4. Run the `app` configuration on an emulator or device with **API 26+**.

Package: `com.deeeelay.injectiontracker`

## Command line

```bash
./gradlew :app:test
./gradlew :app:assembleDebug
```

Install the debug APK from `app/build/outputs/apk/debug/`.

## What it does

- **Setup once:** medicine name, dosage (free text), frequency (**Every week** | **Every other week**), start date and time. Next injection starts at that date/time. Both reminders default **on**.
- **Home:** next shot (with Overdue when due time has passed), last site as a single period-sized primary dot on the frontal silhouette, **Log injection**, **I missed it**.
- **Log done:** editable date/time (defaults to now) and one of 18 frontal zones (3 bands on each upper arm, abdomen side, and upper thigh). Next due = log time + 7 or 14 days.
- **Log missed:** confirmation sheet, no site. Next due = now + interval (not the original due time).
- **Calendar:** past actual logs plus the next upcoming mark (completed circle, missed diamond, upcoming outline).
- **Settings:** independent Day-before and At-time reminder switches, plus Edit regimen.
- **Edit regimen:** same fields as setup. Frequency or start (anchor) changes recalculate the upcoming date; past logs are not rewritten.

Reminders: day-before fires at the same clock as the injection minus 24 hours; at-time fires at `nextInjectionAt`. They are scheduled with `AlarmManager` and restored after reboot.

Data stays on-device (DataStore + Room). There is no account, backend, export, heatmap, back-body map, Watch/iOS client, or multi-med support.

## Tests

Unit tests cover scheduling math (interval, done/missed advancing from the terminal log timestamp, regimen recalculation, day-before = due − 24h) and the locked zone coordinates.
