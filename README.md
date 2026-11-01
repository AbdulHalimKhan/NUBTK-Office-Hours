# NUBTK Timer

**Track Your Time. Know Your Progress.**

A personal Android application for tracking office attendance, working hours, live daily countdown, home screen widget, weekends, holidays, medical leave, casual leave, and attendance reports. Built specifically for faculty and staff at Northern University of Business and Technology, Khulna (NUBTK).

---

## Features

* **Today's Live Working Countdown**: Live second-by-second countdown towards reaching your daily working hours target, with interactive progress indicator.
* **Target Met Notification**: Automatic Android system notification when your daily office hours target is completed to remind you to punch out.
* **Home Screen App Widget**: Dedicated Android widget featuring Punch In and Punch Out quick-action buttons, today's status, and live time countdown.
* **Off-Day Detection**: Automatically recognizes weekends, holidays, and leaves with "Today is your off day" notice and disabled punch actions.
* **Separate Punch In & Punch Out Editing**: Dedicated edit dialogs with calendar defaulting to current date for easy timestamp corrections.
* **Custom Attendance Periods**: Configure attendance cycles (e.g., 21st to 20th of the month) with active status and date validation.
* **Daily Working-Hour Configuration**: Set expected daily hours (e.g., 8 hours / 480 minutes) per period.
* **Interactive Punch In / Punch Out**: One-tap attendance logging with automatic duration tracking.
* **Working-Hour Calculation**: Precise calculation of completed working days, expected hours, actual hours, and delay/extra hours.
* **Dynamic Delay vs. Extra Status**:
  * Displays **Extra** in **Green (`#2E7D32`)** when actual hours exceed expected hours.
  * Displays **Delay** in **Red (`#D32F2F`)** when actual hours are less than expected hours.
  * Displays **On Time** in **Amber (`#F57F17`)** when actual hours equal expected hours.
* **Configurable Weekends**: Toggle weekend days (default: Friday & Saturday) with automatic zero-hour expected work day exclusion.
* **Bangladesh Public Holidays Integration**: Pre-populated official Bangladesh national holidays with support for toggling individual holidays as active office days.
* **Leave Management**: Dedicated support for Medical Leave and Casual Leave with full persistence and summary calculation.
* **Current & Previous Records**: Access past attendance cycles and current period records.
* **10-Day & Custom Range Views**: Filter attendance by 10-day segments or pick custom date ranges using interactive calendar modal starting from the current date.
* **Downloadable PDF Attendance Reports**:
  * Generates clean, printable multi-page PDF documents.
  * Displays user profile header (Name, Designation, Department, Employee ID).
  * Lists date-by-date punch details, status, and comprehensive summary.
  * Shows exact storage location and provides one-tap file opening / path copying.
* **Clean Initial Installation**: Profile inputs remain blank until configured by user.
* **Offline-First Architecture**: 100% local persistence via Android Room Database with SQLite.
* **Material Design 3**: Modern, responsive UI with customized lime-green theme and automatic light/dark mode support.

---

## Developer

**Md. Abdul Halim Khan**  
Lecturer  
Department of Computer Science and Engineering  
Northern University of Business and Technology, Khulna  

---

## Tech Stack & Architecture

* **Language**: 100% Kotlin
* **UI Framework**: Jetpack Compose with Material Design 3 (M3)
* **Architecture**: MVVM (Model-View-ViewModel) + Clean Domain Separations
* **Database & Persistence**: Android Room Database (SQLite) with KSP
* **Concurrency**: Kotlin Coroutines & `StateFlow`
* **Navigation**: Navigation Compose with type-safe routing
* **Document Engine**: Android Native `PdfDocument` & Canvas Vector Rendering
* **CI/CD**: GitHub Actions Automated Build Pipeline (`build-apk.yml`)

---

## Project Structure

```text
NUBTK-Office-Hours/
├── .github/
│   └── workflows/
│       └── build-apk.yml            # Automated CI/CD pipeline for APK builds & Releases
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/            # Room Database, DAOs, Entities, Repository
│   │   │   │   ├── domain/          # Business logic, AttendanceCalculator, Domain Models
│   │   │   │   ├── pdf/             # AttendancePdfGenerator (Custom PDF Engine)
│   │   │   │   ├── ui/              # Compose Screens, Dialogs, Theme, Custom Components
│   │   │   │   └── utils/           # DateTimeUtils, File & Intent Utilities
│   │   │   └── res/                 # Vector drawables, strings, themes, adaptive icons
│   │   └── test/                    # Unit tests and Robolectric test suite
│   ├── build.gradle.kts             # App-level build configurations and dependencies
│   └── proguard-rules.pro
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.properties
│   └── libs.versions.toml           # Version Catalog for dependencies
├── build.gradle.kts                 # Root build configuration
├── settings.gradle.kts              # Project settings & repositories
├── gradlew                          # Gradle wrapper script (Linux/macOS)
├── gradlew.bat                      # Gradle wrapper script (Windows)
└── README.md                        # Documentation
```

---

## Building the Project Locally

### Prerequisites
* **Android Studio** (Ladybug / Koala / Hedgehog or newer)
* **JDK 17** or **JDK 21**
* **Android SDK**: compileSdk 36, minSdk 24

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/mahk-nubtk/NUBTK-Office-Hours.git
   cd NUBTK-Office-Hours
   ```
2. Open the project in Android Studio or build via terminal:
   ```bash
   # Linux / macOS
   ./gradlew assembleDebug

   # Windows
   gradlew.bat assembleDebug
   ```
3. The generated APK will be available at:
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Automated GitHub Actions APK Build

This repository includes an automated GitHub Actions workflow (`.github/workflows/build-apk.yml`):
* **On Push/PR**: Automatically verifies builds and generates `app-debug.apk` and `NUBTK-Office-Hours-v1.1.apk`.
* **Artifact Upload**: Download the compiled APK anytime from the **Actions** tab as `NUBTK-Office-Hours-APK`.
* **On Release Tag (`v*`)**: Automatically creates a GitHub Release and attaches `NUBTK-Office-Hours-v1.1.apk` for direct download.

---

## Installation on Android Device

1. Download `NUBTK-Office-Hours-v1.1.apk` or `app-debug.apk` onto your Android phone.
2. Open the downloaded APK from your browser or file manager.
3. If prompted by Android, enable **"Allow installation from this source"**.
4. Tap **Install**.
5. Launch **NUBTK Office Hours** and start tracking your attendance.
