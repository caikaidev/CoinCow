# Technology Stack

## Build System & Configuration

- **Build System**: Gradle with Kotlin DSL (`.gradle.kts`)
- **Android Gradle Plugin**: 8.11.1
- **Kotlin**: 2.0.21
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 26 (Android 8.0)
- **Java Version**: 11

## Core Technologies

### UI Framework

- **Jetpack Compose**: Modern declarative UI toolkit
- **Material 3**: Latest Material Design components
- **Compose BOM**: 2024.02.00 for version alignment
- **Navigation Compose**: Type-safe navigation

### Architecture & DI

- **Hilt**: Dependency injection framework
- **MVVM Pattern**: ViewModel + StateFlow/SharedFlow
- **Clean Architecture**: Domain/Data/Presentation layers
- **Repository Pattern**: Data abstraction layer

### Networking & Data

- **Retrofit**: HTTP client with Moshi converter
- **OkHttp**: HTTP client with logging and interceptors
- **Moshi**: JSON serialization with Kotlin support
- **Room**: Local database with coroutines support
- **DataStore**: Preferences storage

### Async & State Management

- **Kotlin Coroutines**: Async programming
- **StateFlow/SharedFlow**: Reactive state management
- **WorkManager**: Background task scheduling

### Charts & Visualization

- **Vico**: Modern charting library for Compose
- **Coil**: Image loading for Compose

### Widgets & Background

- **Glance**: Jetpack Compose for App Widgets
- **WorkManager**: Periodic data updates

## Common Commands

### Build & Run

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install debug build
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Development

```bash
# Check dependencies
./gradlew dependencies

# Lint check
./gradlew lint

# Generate build reports
./gradlew build --scan
```

## API Configuration

- **CoinGecko API**: Primary data source
- **API Key**: Configured via `local.properties` as `COINGECKO_API_KEY`
- **Rate Limiting**: Custom interceptor for API throttling
- **Caching Strategy**: 1-minute cache for market data, 5-minute for details

## Testing Strategy

- **Unit Tests**: JUnit + Coroutines Test
- **Integration Tests**: Hilt Testing
- **UI Tests**: Compose Testing with Espresso
