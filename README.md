# ScheduleFlow 📅

A modern timetable-management Android app built with **Jetpack Compose** and **[Miuix](https://github.com/compose-miuix-ui/miuix)** (Xiaomi HyperOS / MIUI design system).

---

## ✨ Features

- **Initial Setup Wizard**:
  - Configurable normal periods per day.
  - Configurable number of days (default Monday–Saturday, 6 days) with customizable names and toggles.
  - Per-day period customization (e.g. Mon: 7, Tue: 7, Wed: 6, Thu: 7, Fri: 7, Sat: 5).
- **Intelligent Home View**:
  - **Dynamic Schedule**: Automatically displays "Today's Timetable" before the cutoff (default 5:00 PM) and "Tomorrow's Timetable" at or after 5:00 PM.
  - **Non-School Day Rollover**: Seamlessly rolls over to the next scheduled school day (e.g., Saturday evening -> Monday timetable).
  - Prominent current date, live system clock, and cutoff status banner.
  - Mode switchers: Smart (Auto), Today, Tomorrow, and Full Week view chips.
- **Timetable Editor**:
  - Full period customization with subject name, teacher, classroom/room, start/end time, custom colors, and icons.
  - Preset subject quick-fill chips for rapid scheduling.
- **Dedicated Weekly Timetable Screen**:
  - 2D synchronized scrollable weekly grid table showing all configured days and periods.
  - Tabbed day-card view for single-day focus with full period details.
  - Graceful handling of days with varying period counts.
- **Settings & Preferences**:
  - Configurable daily cutoff time (default 5:00 PM).
  - Edit active days, day names, and periods per day.
  - Theme mode selection (System, Light, Dark).
  - Timetable reset & reconfiguration.
- **Data Persistence**:
  - Built with Room Database, persisting completely offline on-device. No accounts or internet required.

---

## 🚀 GitHub Actions Automated Build & APK Download

Because Termux environments lack local Android SDKs and toolchains, this repository includes an automated GitHub Actions workflow (`.github/workflows/build.yml`) that builds and tests the app automatically in the cloud.

### How to Build & Download APK:
1. Create a repository on GitHub (e.g., `scheduleflow`).
2. Push this project to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: ScheduleFlow with Miuix UI"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo-name>.git
   git push -u origin main
   ```
3. Go to the **Actions** tab on your GitHub repository.
4. Click on the running/completed workflow run.
5. In the **Artifacts** section at the bottom, download **`ScheduleFlow-Debug-APK`**.
6. Unzip and install the APK on your Android device!
