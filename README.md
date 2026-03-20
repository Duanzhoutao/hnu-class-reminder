# Hainanu Smart Class Reminder

Android MVP for Hainan University students. After importing an Excel timetable, the app parses classes, computes the current academic week, and schedules local reminders before class, at class start, and after class.

## Download

- Latest release page:
  - [GitHub Releases](https://github.com/Duanzhoutao/hnu-class-reminder/releases/latest)
- One-click APK download:
  - [Download latest APK](https://github.com/Duanzhoutao/hnu-class-reminder/releases/latest/download/hnu-class-reminder-v1.0.0-debug.apk)

## MVP Features

- Import Hainanu `.xls` / `.xlsx` timetable files
- Parse course name, teacher, location, weeks, and sections
- Support multi-course cells from the real timetable format
- Read section time slots from the first Excel column
- Set first-week Monday and compute the current week automatically
- Show today's classes and the current week's schedule
- Per-course reminder switch
- Daily mute switch
- Three reminder types: pre-class, on-class, post-class
- Three reminder tiers:
  - A Normal: notification only, no vibration, no ringtone
  - B Standard: optional vibration/ringtone, higher priority
  - C Strong: stronger local notification behavior, no full-screen popup yet
- Holiday skip support
- Debug page for scheduled reminders, notification logs, and parse errors

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM + Repository
- Room
- DataStore
- Hilt
- AlarmManager + BroadcastReceiver
- WorkManager
- Apache POI

## Project Layout

```text
app/
  src/main/java/com/hainanu/signinassistant/
    alarm/
    data/
    di/
    domain/
    notification/
    receiver/
    ui/
  src/main/assets/holidays/
```

## Build

1. Install Android Studio with Android SDK.
2. Configure `local.properties` to point to your local SDK.
3. Run:

```powershell
.\gradlew.bat assembleDebug
```

4. Install on Android 8.0+ devices.

## Import Flow

1. Open the import page.
2. Choose a Hainanu Excel timetable file.
3. Review the parse summary and preview.
4. Tap "Confirm import and overwrite old data".
5. The app stores the timetable and rebuilds the next 14 days of reminders.

## Export Tutorial

1. Log in to [jxgl.hainanu.edu.cn](https://jxgl.hainanu.edu.cn/).
2. Open `培养管理 -> 学期课表`.
3. Click export.
4. Return to the app and import the exported Excel file.

## First Week Setup

Open the settings page and choose the first Monday of the semester. This immediately recalculates the current week and reschedules reminders.

Built-in known default:

- `2025-2026-2` first-week Monday: `2026-03-02`

Built-in holiday dates:

- `2026-04-05`
- `2026-05-01`
- `2026-06-19`

To add a makeup-workday entry, extend the matching term JSON:

```json
{
  "date": "2026-04-11",
  "type": "MAKEUP_WORKDAY",
  "makeupWeekday": 5,
  "title": "Saturday makeup for Friday classes"
}
```

## Reminder Behavior

- Prefer `AlarmManager.setExactAndAllowWhileIdle()`
- Degrade safely when exact alarm permission is unavailable
- Reschedule after import, settings changes, reboot, and date/time changes
- Use WorkManager to maintain the rolling 14-day reminder horizon

## Privacy

- Personal timetable files are not bundled in the repository or APK.
- Holiday data is stored separately from user timetable data.

## Known Limits

- Currently tuned for the Hainanu timetable format only
- Bottom "note-style" courses without a fixed weekday/section are stored as notes and do not trigger reminders
- Ringtone support currently uses the system notification sound toggle only
- Strong reminders do not use full-screen alerts yet
- If a device already has an incorrect stored first-week Monday, the app currently respects that value until the user corrects it in settings or reimports the timetable
