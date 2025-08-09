---
inclusion: always
---

# Technology Stack & Development Guidelines

## Core Architecture Principles

- **Clean Architecture**: Strict separation of Domain/Data/Presentation layers
- **MVVM Pattern**: ViewModels expose StateFlow/SharedFlow, never direct repository access
- **Dependency Injection**: Use Hilt for all dependencies, avoid manual instantiation
- **Single Responsibility**: Each class should have one clear purpose
- **Immutable State**: Use data classes and sealed classes for state management

## Technology Stack

### Build & Configuration

- **Gradle**: Kotlin DSL only (`.gradle.kts`), version catalog in `gradle/libs.versions.toml`
- **Android**: Target SDK 34, Min SDK 26, AGP 8.11.1
- **Kotlin**: 2.0.21 with coroutines for all async operations
- **API Keys**: Store in `local.properties` as `COINGECKO_API_KEY`

### UI & Navigation

- **Jetpack Compose**: All UI components, no XML layouts
- **Material 3**: Use theme colors and components consistently
- **Navigation Compose**: Type-safe navigation with sealed class routes
- **Instagram-style Design**: Card-based layouts with gradient elements

### Data & Networking

- **Retrofit + Moshi**: API calls with proper error handling
- **Room**: Local caching with coroutines, entities mirror DTOs
- **DataStore**: User preferences, no SharedPreferences
- **Coil**: Image loading with proper placeholder handling

### State Management Rules

- **StateFlow**: UI state that persists across configuration changes
- **SharedFlow**: One-time events (navigation, snackbars, toasts)
- **Sealed Classes**: Represent loading/success/error states consistently
- **No LiveData**: Use StateFlow/SharedFlow exclusively

## Code Style Guidelines

### Naming Conventions

- **Files**: `CoinDetailsScreen.kt`, `CryptoRepositoryImpl.kt`
- **Classes**: PascalCase, descriptive names
- **Functions**: camelCase, verb-based for actions
- **Constants**: SCREAMING_SNAKE_CASE in companion objects
- **Packages**: lowercase, no underscores

### Compose Best Practices

- **Stateless Composables**: Pass state and callbacks as parameters
- **Preview Functions**: Include `@Preview` for all major components
- **Modifier Parameter**: Always include `modifier: Modifier = Modifier`
- **State Hoisting**: Lift state to appropriate level, prefer ViewModel

### Error Handling

- **NetworkResult**: Wrap API responses in sealed class
- **Try-Catch**: Use in repositories, not ViewModels
- **User-Friendly Messages**: Convert technical errors to readable text
- **Offline Support**: Always check cache before network calls

## Development Commands

```bash
# Essential commands for this project
./gradlew clean assembleDebug    # Clean build
./gradlew installDebug          # Install on device
./gradlew test                  # Run unit tests
./gradlew lint                  # Code quality check
```

## API Integration Rules

- **CoinGecko API**: Primary data source, respect rate limits
- **Caching Strategy**: 1-minute for market data, 5-minutes for coin details
- **Error Handling**: Graceful degradation when API unavailable
- **Background Updates**: Use WorkManager for periodic data refresh

## Testing Requirements

- **Unit Tests**: All ViewModels, UseCases, and Repositories
- **Compose Tests**: Critical user flows and component behavior
- **Mocking**: Use MockK for Kotlin-friendly mocking
- **Coroutines**: Use TestCoroutineDispatcher for async testing
