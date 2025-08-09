# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CoinCow is a modern Android cryptocurrency tracking application built with Kotlin and Jetpack Compose. It provides real-time market data, portfolio tracking, interactive charts, and home screen widgets for cryptocurrency enthusiasts.

## Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp with Moshi
- **Charts**: Vico (modern charting library)
- **Image Loading**: Coil
- **Widgets**: Glance (Jetpack Compose for widgets)
- **Data Storage**: DataStore for preferences
- **Background Work**: WorkManager

### Module Structure
```
app/src/main/java/com/kcode/gankotlin/
├── data/               # Data layer
│   ├── cache/          # Cache management
│   ├── local/          # Room database & DAOs
│   ├── remote/         # API services & DTOs
│   ├── repository/     # Repository implementations
│   └── worker/         # Background workers
├── domain/             # Domain layer
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic
├── presentation/       # Presentation layer
│   ├── component/      # Reusable UI components
│   ├── screen/         # Screen composables
│   ├── viewmodel/      # ViewModels
│   └── navigation/     # Navigation setup
├── di/                 # Dependency injection modules
├── widget/             # Home screen widgets
└── ui/theme/          # Design system & theming
```

### Key Components

#### Data Layer
- **CryptoRepository**: Main repository for cryptocurrency data with caching
- **CoinGeckoApi**: REST API client for market data
- **CryptoDatabase**: Room database for local storage
- **DataSyncManager**: Handles data synchronization
- **SmartCacheStrategy**: Intelligent caching with expiration

#### Domain Layer
- **Use Cases**: Single-responsibility business logic (GetMarketDataUseCase, etc.)
- **Models**: Clean domain models (CoinMarketData, CoinDetails, etc.)

#### Presentation Layer
- **ViewModels**: State management with Compose integration
- **UI Components**: Instagram-style design with Material 3
- **Navigation**: Jetpack Navigation Compose

## Development Commands

### Building
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release AAB (requires keystore configuration)
./gradlew bundleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumentation tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lintDebug
```

### Testing & Analysis
```bash
# Local build test (mimics GitHub Actions)
./scripts/test-build.sh

# Comprehensive build analysis
./scripts/analyze-build.sh

# Size analysis only
./scripts/analyze-build.sh size

# Compare debug vs release builds
./scripts/analyze-build.sh compare
```

### Version Management
```bash
# Print current version info
./gradlew printVersionInfo

# Print version code
./gradlew printVersionCode

# Print version name
./gradlew printVersionName
```

### Keystore Management
```bash
# Setup new keystore (first time)
./scripts/setup-keystore.sh

# Validate keystore configuration
./scripts/validate-keystore.sh

# Rotate keystore for security
./scripts/rotate-keystore.sh
```

## Configuration

### API Keys
- **CoinGecko API**: Set `COINGECKO_API_KEY` in `local.properties` or environment variable
- API key is injected into BuildConfig for secure access

### Keystore (Release Builds)
Required environment variables:
- `KEYSTORE_FILE`: Path to keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias name
- `KEY_PASSWORD`: Key password

### Build Variants
- **Debug**: Development build with logging enabled
- **Release**: Production build with ProGuard/R8 optimization, signing, and resource shrinking

## Key Features

### Market Data
- Real-time cryptocurrency prices from CoinGecko API
- Market cap, volume, and price change data
- Smart caching with offline support
- Background data synchronization

### Portfolio Tracking
- Personal watchlist management
- Price tracking for favorite coins
- Historical data and charts

### UI Components
- Instagram-inspired card-based design
- Material 3 design system with dark/light themes
- Smooth animations and transitions
- Skeleton loading states
- Error handling with user-friendly messages

### Widgets
- Home screen widgets using Glance
- Real-time price updates
- Customizable layouts and refresh intervals

## Testing

### Unit Tests
- Repository implementations
- Use cases and business logic
- ViewModel state management
- Widget data providers

### Integration Tests
- Database operations
- API integration
- Screen navigation

### Test Structure
```
app/src/test/java/         # Unit tests
app/src/androidTest/java/  # Integration tests
```

## Deployment

### GitHub Actions Workflows
- **build-check.yml**: Continuous integration for PRs and pushes
- **release.yml**: Automated AAB building and GitHub releases

### Release Process
1. Create version tag: `git tag v1.0.0`
2. Push tag: `git push origin v1.0.0`
3. GitHub Actions builds signed AAB
4. Download from GitHub release
5. Upload to Google Play Console

### Required GitHub Secrets
- `KEYSTORE_BASE64`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias name
- `KEY_PASSWORD`: Key password
- `COINGECKO_API_KEY`: CoinGecko API key

## Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Prefer composition over inheritance
- Write self-documenting code

### Architecture Patterns
- Use Clean Architecture principles
- Separate concerns between layers
- Implement repository pattern for data access
- Use use cases for business logic

### Compose Best Practices
- Use `remember` for expensive calculations
- Implement proper state hoisting
- Create reusable components
- Handle lifecycle events properly

### Performance Optimization
- Implement smart caching strategies
- Use lazy loading for lists
- Optimize image loading with Coil
- Implement proper memory management

### Error Handling
- Use NetworkResult wrapper for API responses
- Implement graceful fallbacks
- Show user-friendly error messages
- Log errors for debugging

### Security
- Never commit API keys or credentials
- Use ProGuard/R8 obfuscation in release builds
- Validate all user inputs
- Implement proper certificate pinning

## Troubleshooting

### Common Issues
- **Build failures**: Check Java version (requires JDK 17)
- **API errors**: Verify CoinGecko API key configuration
- **Keystore issues**: Use validation script to check credentials
- **Widget problems**: Ensure Glance dependencies are correct

### Debugging
- Enable verbose logging in debug builds
- Use performance monitoring for memory issues
- Check crash recovery manager for app stability
- Monitor API rate limits and caching behavior

## Documentation References
- [Android App Bundle Guide](GOOGLE_PLAY_DEPLOYMENT.md)
- [GitHub Actions Fixes](GITHUB_ACTIONS_FIXES.md)
- [Build Verification Checklist](VERIFICATION_CHECKLIST.md)
- [Keystore Management](scripts/README.md)