# Sora 🎵

A social music discovery and tracking Android application that connects users through their music listening experiences with location-based features.

## Overview

Sora is a Jetpack Compose-based Android app that allows users to:
- Track their music listening history with location data
- Discover what friends are listening to in real-time
- Share playlists and favorite songs
- Visualize listening patterns on an interactive map
- Connect with friends through music

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Supabase (PostgreSQL, Auth, Storage, Realtime)
- **Music Integration**: Spotify API
- **Maps**: Google Maps Android API
- **Authentication**: OAuth 2.0 with AppAuth
- **Networking**: Ktor Client
- **Serialization**: Kotlinx Serialization
- **Image Loading**: Coil
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
app/src/main/java/com/example/sora/
├── data/
│   ├── model/          # Data models (User, Song, Artist, Album, etc.)
│   └── repository/     # Data repositories for API interactions
├── feed/               # Social feed features
├── friends/            # Friends management UI
├── library/            # User's music library (playlists, songs)
├── main/               # Home screen and main feed components
├── map/                # Location-based music visualization
├── service/            # Background services (song tracking)
├── ui/                 # Reusable UI components and theme
├── utils/              # Utility classes and helpers
├── viewmodel/          # ViewModels for business logic
├── MainActivity.kt     # Main application activity
└── SplashActivity.kt   # Splash screen
```

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 34
- An Android device or emulator running API 24+

### Configuration

1. **Clone the repository**:
   ```bash
   git clone https://github.com/kellenGary/Sora.git
   cd Sora
   ```

2. **Create `local.properties` file** in the root directory:
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   
   # API Keys
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   SPOTIFY_CLIENT_ID=your_spotify_client_id
   SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
   GOOGLE_MAPS_API_KEY=your_google_maps_api_key
   ```

3. **Sync Gradle**:
   ```bash
   ./gradlew sync
   ```

4. **Build the project**:
   ```bash
   ./gradlew build
   ```

## Running the App

### From Android Studio
1. Open the project in Android Studio
2. Select a device or emulator
3. Click the "Run" button or press `Shift + F10`

### From Command Line
```bash
./gradlew installDebug
```

## Testing

### Unit Tests

Unit tests verify business logic, data models, and utility functions without requiring an Android device.

#### Running All Unit Tests
```bash
./gradlew :app:testDebugUnitTest
```

#### Running Specific Test Class
```bash
./gradlew :app:testDebugUnitTest --tests "com.example.sora.SoraUnitTests"
```

#### View Unit Test Results
After running tests, open the HTML report:
```bash
open app/build/reports/tests/testDebugUnitTest/index.html
```

#### Unit Test Coverage
The project includes the following unit tests:

**SoraUnitTests.kt**:
- `testUserModelCreation` - Validates User data model
- `testDisplayNameValidation` - Tests display name validation rules
- `testGeographicFunctions` - Tests coordinate validation and distance calculations
- `testSongFormatting` - Tests song/artist formatting
- `testSongModelProperties` - Tests Song model properties

**Test Files Location**: `app/src/test/java/com/example/sora/`

### UI Tests (Instrumented Tests)

UI tests run on an Android device or emulator and test the actual UI components using Compose Testing.

#### Prerequisites
- A connected Android device or running emulator
- Verify device connection:
  ```bash
  adb devices
  ```

#### Running All UI Tests
```bash
./gradlew connectedDebugAndroidTest
```

#### Running Specific UI Test Class
```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.sora.HeaderComposeTest
```

#### View UI Test Results
After running tests, open the HTML report:
```bash
open app/build/reports/androidTests/connected/index.html
```

#### UI Test Coverage
The project includes the following Compose UI tests:

**HeaderComposeTest.kt**:
- `headerDisplaysSoraLogoAndTitle` - Tests header component rendering
- `headerComponentRendersWithoutCrashing` - Tests component stability

**ActiveUserCardComposeTest.kt**:
- `activeUserCardDisplaysUserName` - Tests user name display
- `activeUserCardHandlesClickInteraction` - Tests click interactions
- `activeUserCardDisplaysInitialWhenNoAvatar` - Tests fallback avatar display

**SettingCardComposeTest.kt**:
- `settingCardRendersSuccessfully` - Tests settings card rendering
- `settingOptionBoxDisplaysWithCorrectStyling` - Tests option box styling
- `settingCardHasCorrectLayout` - Tests layout structure

**Test Files Location**: `app/src/androidTest/java/com/example/sora/`

### Test Best Practices

1. **Run unit tests frequently** during development (they're fast!)
2. **Run UI tests before committing** to ensure UI stability
3. **Check test coverage** to identify untested code paths
4. **Keep tests isolated** - each test should be independent
5. **Use meaningful test names** that describe what is being tested

### Continuous Integration

To run all tests in CI:
```bash
# Unit tests (fast)
./gradlew :app:testDebugUnitTest

# UI tests (requires emulator/device)
./gradlew connectedDebugAndroidTest

# Both
./gradlew check connectedDebugAndroidTest
```

## Features

### 🎧 Music Tracking
- Real-time song tracking with location data
- Integration with Spotify API
- Background service for continuous tracking

### 👥 Social Features
- Follow friends and see their listening activity
- Share playlists and favorite songs
- Real-time activity feed
- Mutual friends discovery

### 🗺️ Location-Based Discovery
- Interactive map showing where songs were listened to
- Discover music from specific locations
- Location history visualization

### 📱 Modern UI
- Material 3 Design
- Dark mode support
- Smooth animations and transitions
- Responsive layouts

## Dependencies

### Core Dependencies
- `androidx.core:core-ktx:1.13.1`
- `androidx.activity:activity-compose:1.9.3`
- `androidx.compose.bom:2024.05.00`

### Supabase
- `io.github.jan-tennert.supabase:postgrest-kt`
- `io.github.jan-tennert.supabase:gotrue-kt`
- `io.github.jan-tennert.supabase:storage-kt`
- `io.github.jan-tennert.supabase:realtime-kt`

### Testing
- `junit:junit:4.13.2` - Unit testing framework
- `androidx.compose.ui:ui-test-junit4` - Compose UI testing
- `androidx.test.ext:junit:1.1.5` - Android JUnit extensions
- `androidx.test.espresso:espresso-core:3.5.1` - UI testing

See `app/build.gradle.kts` for complete dependency list.

## Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Coding Standards
- Follow Kotlin coding conventions
- Write tests for new features
- Use meaningful variable and function names
- Document complex logic with comments
- Run tests before submitting PR

## License

This project is private and proprietary.

## Contact

Kellen Gary - [@kellenGary](https://github.com/kellenGary)

Project Link: [https://github.com/kellenGary/Sora](https://github.com/kellenGary/Sora)

## Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Supabase](https://supabase.com/)
- [Spotify Web API](https://developer.spotify.com/documentation/web-api/)
- [Google Maps Platform](https://developers.google.com/maps)
