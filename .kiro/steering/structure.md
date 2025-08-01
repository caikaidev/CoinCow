# Project Structure

## Package Organization

The project follows Clean Architecture principles with clear separation of concerns:

```
com.kcode.gankotlin/
├── data/                    # Data layer
│   ├── local/              # Local data sources
│   │   ├── dao/            # Room DAOs
│   │   └── entity/         # Database entities
│   ├── mapper/             # Data mappers (DTO ↔ Domain ↔ Entity)
│   ├── realtime/           # Real-time data management
│   ├── remote/             # Network data sources
│   │   ├── dto/            # API response models
│   │   └── interceptor/    # HTTP interceptors
│   ├── repository/         # Repository implementations
│   ├── scheduler/          # Data update scheduling
│   ├── sync/               # Data synchronization
│   ├── validator/          # Data validation
│   └── worker/             # Background workers
├── di/                     # Dependency injection modules
├── domain/                 # Domain layer (business logic)
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business use cases
├── presentation/           # Presentation layer
│   ├── component/          # Reusable UI components
│   ├── navigation/         # Navigation setup
│   ├── screen/             # Screen composables
│   ├── util/               # UI utilities
│   └── viewmodel/          # ViewModels and UI state
├── ui/                     # UI theme and styling
│   └── theme/              # Colors, typography, themes
└── widget/                 # App widget implementations
```

## Key Architectural Patterns

### Clean Architecture Layers
- **Domain**: Pure business logic, no Android dependencies
- **Data**: Repository implementations, network/database access
- **Presentation**: UI components, ViewModels, navigation

### Naming Conventions
- **Entities**: `*Entity.kt` (database models)
- **DTOs**: `*Dto.kt` (API response models)
- **Domain Models**: Plain names (e.g., `CoinMarketData.kt`)
- **ViewModels**: `*ViewModel.kt`
- **Use Cases**: `*UseCase.kt`
- **Repositories**: `*Repository.kt` (interface), `*RepositoryImpl.kt` (implementation)
- **DAOs**: `*Dao.kt`
- **Screens**: `*Screen.kt`
- **Components**: Descriptive names (e.g., `CoinListItem.kt`)

### File Organization Rules
- One public class per file
- File name matches the main class name
- Group related functionality in packages
- Keep interfaces and implementations in separate packages when applicable

## Data Flow Architecture

### Typical Data Flow
1. **UI** triggers action via ViewModel
2. **ViewModel** calls appropriate UseCase
3. **UseCase** orchestrates business logic, calls Repository
4. **Repository** checks cache, fetches from network if needed
5. **Data** flows back through layers with proper mapping
6. **UI** observes StateFlow/SharedFlow for updates

### State Management
- **StateFlow**: For UI state that needs to be observed
- **SharedFlow**: For one-time events (navigation, snackbars)
- **Sealed Classes**: For representing different UI states (Loading, Success, Error)

## Testing Structure

```
test/                       # Unit tests
├── presentation/
│   ├── component/          # Component tests
│   └── integration/        # Integration tests
└── [mirror main structure] # Unit tests for each layer

androidTest/                # Instrumented tests
└── [mirror main structure] # UI and integration tests
```

## Resource Organization

- **Strings**: Localized in `res/values/strings.xml`
- **Colors**: Defined in Compose theme files
- **Dimensions**: Use Compose dp values directly
- **Icons**: Vector drawables in `res/drawable/`

## Configuration Files

- **Gradle**: Version catalog in `gradle/libs.versions.toml`
- **Proguard**: Rules in `app/proguard-rules.pro`
- **API Keys**: Stored in `local.properties` (not committed)
- **Manifest**: Minimal configuration, permissions declared explicitly