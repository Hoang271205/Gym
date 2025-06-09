# Gym App

[![Android CI](https://github.com/Hoang271205/Gym/workflows/Android%20CI/badge.svg)](https://github.com/Hoang271205/Gym/actions?query=workflow%3A%22Android+CI%22)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)]()

> Gym App is a fully-featured Android application built with Android Studio. It helps gym-goers and personal trainers to track workouts, plan routines, monitor progress, and stay motivated.

## Table of Contents

1. [About the Project](#about-the-project)
2. [Key Features](#key-features)
3. [Built With](#built-with)
4. [Prerequisites](#prerequisites)
5. [Installation & Setup](#installation--setup)
6. [Project Structure](#project-structure)
7. [Architecture Overview](#architecture-overview)
8. [Screens & Navigation](#screens--navigation)
9. [Sample Code Snippets](#sample-code-snippets)
10. [Database Schema](#database-schema)
11. [Notifications & Alarms](#notifications--alarms)
12. [Work with APIs](#work-with-apis)
13. [Testing](#testing)
14. [Contributing](#contributing)
15. [Roadmap](#roadmap)
16. [License](#license)
17. [Contact](#contact)  


## About the Project

Gym App is designed to be your personal workout companion. Whether you’re a beginner or an advanced athlete, it provides:

- Customized workout plans
- Detailed exercise database with instructions
- Progress tracking & analytics
- Notifications for scheduled workouts
- Integration with Firebase for user authentication and cloud sync  
## Built With

- **Language**: Kotlin (preferred), Java (legacy modules)
- **IDE**: Android Studio Arctic Fox (2020.3.1) or higher
- **Architecture**: MVVM + Repository Pattern
- **UI Toolkit**: XML layouts & Material Components
- **Dependency Injection**: Hilt
- **Network**: Retrofit + OkHttp
- **Database**: Room (Jetpack)
- **Authentication & Sync**: Firebase Auth & Firestore
- **Image Loading**: Glide
- **Charting**: MPAndroidChart

---

## Prerequisites

Before you begin, ensure you have met the following requirements:

- Android Studio installed (Arctic Fox or newer)
- Android SDK Platform 30+
- JDK 8 or higher
- Internet connection for Firebase & API requests
- (Optional) A physical device or emulator running Android 6.0 (API 23)+

---

## Installation & Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/Hoang271205/Gym.git
   cd Gym
   ```

2. **Open in Android Studio**
    - `File` → `Open` → Select the `Gym` directory

3. **Configure Firebase**
    - Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/)
    - Add an Android app with your package name (e.g., `com.hoang.gymapp`)
    - Download `google-services.json` and place it in `app/`

4. **Build the project**
    - Gradle will sync and download dependencies
    - Run `./gradlew assembleDebug` or click the Run button in Android Studio

5. **Run on Device/Emulator**
    - Select a device or emulator and hit **Run**

---
