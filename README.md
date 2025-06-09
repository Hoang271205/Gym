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
## Project Structure

```
Gym/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hoang/gymapp/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/       # Room entities & DAOs
│   │   │   │   │   ├── remote/      # Retrofit services
│   │   │   │   │   └── repository/  # Repositories
│   │   │   │   ├── di/              # Hilt modules
│   │   │   │   ├── ui/
│   │   │   │   │   ├── main/        # MainActivity & ViewModels
│   │   │   │   │   ├── workout/     # Workout screens
│   │   │   │   │   └── profile/     # Profile screens
│   │   │   │   └── utils/           # Utility classes
│   │   │   ├── res/
│   │   │   │   ├── layout/          # XML layouts
│   │   │   │   ├── drawable/        # Icons & images
│   │   │   │   ├── values/          # colors, strings, styles
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # Unit tests
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

---

## Architecture Overview

We follow the MVVM (Model-View-ViewModel) pattern:

1. **Model**
    - Data classes representing domain objects (e.g., `Workout`, `Exercise`, `UserProfile`)
    - Entities for Room persistence
2. **View**
    - XML layouts & Jetpack Compose (where applicable)
    - Activities & Fragments observing LiveData/StateFlow
3. **ViewModel**
    - Provides data to UI
    - Handles business logic and transforms repository data
4. **Repository**
    - Mediates between data sources (local DB, remote API)
    - Exposes a clean API to ViewModels

Dependency injection (Hilt) is used to provide singletons for Database, Retrofit, and other utilities.

---

## Screens & Navigation

| Screen Name        | Description                                            |
| ------------------ | ------------------------------------------------------ |
| **Login**          | User authentication (Email/Google)                     |
| **Sign Up**        | Register new account                                   |
| **Home**           | Overview of upcoming workouts & stats                  |
| **Workout Plan**   | Browse or create custom workout routines               |
| **Exercise Detail**| Instructions, animations, muscle group highlights      |
| **Progress**       | Charts and logs of past workouts                       |
| **Profile**        | User settings, preferences, and account management      |

Navigation is handled by the AndroidX Navigation Component with a single-Activity pattern.

---

## Sample Code Snippets

### ViewModel Example (Kotlin)

```kotlin
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts

    init {
        viewModelScope.launch {
            repository.getAllWorkouts().collect {
                _workouts.value = it
            }
        }
    }
}
```

### Retrofit Service Interface

```kotlin
interface GymApiService {
  @GET("exercises")
  suspend fun fetchExercises(): List<ExerciseDto>

  @POST("users/{id}/workouts")
  suspend fun uploadWorkout(
    @Path("id") userId: String,
    @Body workout: WorkoutDto
  ): Response<Void>
}
```

### Room DAO Interface

```kotlin
@Dao
interface ExerciseDao {
  @Query("SELECT * FROM exercise_table WHERE muscleGroup = :group")
  fun getByMuscle(group: String): Flow<List<ExerciseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(exercises: List<ExerciseEntity>)
}
```

---
## Database Schema

```sql
-- User table
CREATE TABLE user_profile (
  uid TEXT PRIMARY KEY,
  displayName TEXT,
  email TEXT,
  createdAt INTEGER
);

-- Exercise table
CREATE TABLE exercise_table (
  id TEXT PRIMARY KEY,
  name TEXT,
  description TEXT,
  muscleGroup TEXT,
  imageUrl TEXT
);

-- Workout log
CREATE TABLE workout_log (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  userUid TEXT,
  workoutDate INTEGER,
  exerciseId TEXT,
  sets INTEGER,
  reps INTEGER,
  weight REAL
);
```

---

## Notifications & Alarms

We schedule daily reminders using `AlarmManager`:

```kotlin
fun scheduleDailyWorkoutReminder(context: Context, hour: Int, minute: Int) {
  val alarmIntent = Intent(context, WorkoutReminderReceiver::class.java)
  val pendingIntent = PendingIntent.getBroadcast(
    context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT
  )
  val calendar = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, 0)
  }
  val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  alarmManager.setInexactRepeating(
    AlarmManager.RTC_WAKEUP,
    calendar.timeInMillis,
    AlarmManager.INTERVAL_DAY,
    pendingIntent
  )
}
```

---
## Work with APIs

1. **Base URL**
   - Defined in `NetworkModule.kt` as `https://api.yourgymapp.com/`
2. **Timeouts**
   - 30 seconds for connect/read/write via OkHttp client
3. **Serialization**
   - Moshi converter for JSON

```kotlin
fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
  Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create())
    .client(okHttpClient)
    .build()
```

---

## Testing

- **Unit Tests**
   - JUnit5 + Mockito for ViewModels & Repositories
- **Instrumentation Tests**
   - Espresso for UI flows
- **Sample Unit Test**

```kotlin
@Test
fun `viewModel emits workouts from repository`() = runBlockingTest {
  val fakeList = listOf(Workout("1", "Leg Day"))
  whenever(repository.getAllWorkouts()).thenReturn(flowOf(fakeList))

  val viewModel = WorkoutViewModel(repository)
  val emitted = viewModel.workouts.first()

  assertEquals(fakeList, emitted)
}
```

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on coding standards and the process.

---

## Roadmap

- [x] Basic workout logging
- [x] Firebase Authentication
- [ ] Social sharing
- [ ] In-app purchase for premium content
- [ ] Wear OS integration
- [ ] Real-time chat with personal trainers

---

## License

Distributed under the MIT License. See `LICENSE` for more information.

---

## Contact

Your Name – hoang271205@gmail.com  
Project Link: [https://github.com/Hoang271205/Gym](https://github.com/Hoang271205/Gym)

## FAQ

### Q1: How do I reset my password?
If you’ve forgotten your password, open the app and tap “Forgot Password?” on the login screen. Enter the email associated with your account, and you’ll receive a reset link. Follow the instructions in the email to set a new password.

### Q2: Can I use the app offline?
Yes. The Room database caches your workout plans and exercise library locally. Any changes made offline will synchronize automatically when an internet connection is restored.

### Q3: How do I switch between light and dark mode?
Go to **Profile → Settings → Appearance**. You can choose **Light**, **Dark**, or **System Default**. The theme will apply immediately across the app.

### Q4: How can I contact support?
For general inquiries or feedback, email us at support@yourgymapp.com. For bug reports, please open an issue on GitHub:  
https://github.com/Hoang271205/Gym/issues

---
## Troubleshooting

1. **Build fails with “Cannot find symbol”**
   - Ensure you’ve run `./gradlew clean` and that Android Studio has synced all Gradle dependencies.
   - Verify that `kapt` annotation processing is enabled in `build.gradle` for Hilt and Room.

2. **Missing `google-services.json` error**
   - Confirm you placed `google-services.json` in the `app/` directory.
   - Check that your applicationId in `build.gradle` matches the Firebase project.

3. **Crash on startup (NullPointerException)**
   - Use Android Studio’s Logcat to inspect the stack trace.
   - Common culprits: uninitialized ViewBinding, missing Manifest entries, or misconfigured Hilt modules.

4. **Slow image loading**
   - Ensure you’re using the latest Glide dependency.
   - Consider enabling disk cache: `.diskCacheStrategy(DiskCacheStrategy.ALL)`.

---

## Environment Variables & Configuration

The following values can be customized in `gradle.properties` or via CI/CD secrets:

- `API_BASE_URL`
- `FIREBASE_API_KEY`
- `FIREBASE_PROJECT_ID`
- `FIREBASE_APP_ID`
- `GOOGLE_CLIENT_ID`

Example (`~/.gradle/gradle.properties`):
```properties
API_BASE_URL=https://api.yourgymapp.com/
FIREBASE_API_KEY=YOUR_FIREBASE_API_KEY
FIREBASE_PROJECT_ID=your-gym-app
FIREBASE_APP_ID=1:1234567890:android:abcdef123456
GOOGLE_CLIENT_ID=1234567890-abcdef.apps.googleusercontent.com
```

---

## CI/CD Pipeline

Our project uses GitHub Actions to automate builds, tests, and releases.

```
.github/workflows/android-ci.yml
  - Checks out code
  - Sets up JDK 11
  - Installs Android SDK 30
  - Runs `./gradlew lint assembleDebug`
  - Executes unit & instrumentation tests
  - Uploads artifacts on success
```

To trigger a release build on a new tag, see `.github/workflows/release.yml`.

---
## Localization & Internationalization

We support multiple languages via Android’s resource system:

- res/values/strings.xml (English – default)
- res/values-es/strings.xml (Spanish)
- res/values-fr/strings.xml (French)

To add a new language:

1. Duplicate `res/values/strings.xml` into `res/values-<locale>/`.
2. Translate all `<string>` entries.
3. Test by changing your device/emulator locale.

---

## Security Considerations

- All network traffic is encrypted with HTTPS.
- Sensitive data (e.g., auth tokens) stored in EncryptedSharedPreferences.
- Firebase rules enforce read/write access only to authenticated users.
- Regular dependency audits with `./gradlew dependencyCheck`.

---

## Performance Optimization

- Uses `Flow`/`LiveData` to observe DB changes efficiently.
- Paging library for large exercise lists.
- Image placeholders & thumbnails to reduce full-size downloads.
- ProGuard/R8 rules enabled for release builds to shrink & obfuscate code.

---
## Release Notes & Versioning

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR** version when you make incompatible API changes.
- **MINOR** version when you add functionality in a backwards-compatible manner.
- **PATCH** version when you make backwards-compatible bug fixes.

### v1.2.0 (2025-06-01)
- Added social sharing of workout stats
- Improved dark mode contrast
- Fixed crash on habit tracker screen

### v1.1.0 (2025-04-10)
- Introduced progress analytics charts
- Refactored networking layer to use Moshi
- Added Spanish localization

### v1.0.0 (2025-01-15)
- Initial release with core workout planning & tracking

---

## Known Issues

- Occasionally, sync may delay up to 10 seconds on slow connections.
- Wear OS integration (beta) may crash on some emulator images.
- Export to CSV currently exports only current week’s data.

---
## Contributing & Code of Conduct

Please read our [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before submitting contributions. We adhere to a Contributor Covenant to maintain a welcoming community.

---

## Acknowledgements

- [Jetpack Compose](https://developer.android.com/jetpack/compose) team for UI frameworks
- [Firebase](https://firebase.google.com/) for backend services
- [Glide](https://github.com/bumptech/glide) for image loading
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) for data visualization

---

Thank you for using Gym App! Happy training! 🚀



