# KeyPass Architecture Documentation

## Table of Contents
- [Project Overview](#project-overview)
- [Module Structure](#module-structure)
- [Technology Stack](#technology-stack)
- [Architecture Patterns](#architecture-patterns)
- [Data Layer](#data-layer)
- [UI Layer](#ui-layer)
- [Security Architecture](#security-architecture)
- [Dependency Injection](#dependency-injection)
- [Build Configuration](#build-configuration)
- [Key Components](#key-components)
- [Getting Started for Contributors](#getting-started-for-contributors)

## Project Overview

KeyPass is an offline, open-source password manager for Android that prioritizes security and user privacy. The application does not require internet permissions and stores all data locally on the device using encrypted storage.

### Core Principles
- **Complete Offline**: No network permissions, all data stays on device
- **Security First**: SQLCipher database encryption, Android KeyStore integration
- **Privacy**: No telemetry, analytics, or external data sharing
- **Modern Android**: Jetpack Compose UI, Material Design 3, latest Android APIs

## Module Structure

The project is organized into a multi-module architecture for better separation of concerns, reusability, and build performance:

```
KeyPass/
├── app/                    # Main Android application module
├── common/                 # Android library with shared business logic
├── shared/                 # Kotlin Multiplatform module for UI components
├── baselineprofile/        # Baseline profiles for performance optimization
├── desktop/                # Desktop application (currently not included in build)
└── buildSrc/              # Build configuration and dependency management
```

### Module Details

#### `app` Module
- **Type**: Android Application
- **Purpose**: Main entry point, UI screens, navigation, and Android-specific features
- **Dependencies**: Depends on `common` and `shared` modules
- **Key Components**:
  - UI screens built with Jetpack Compose
  - Navigation system
  - Autofill service implementation
  - Import/export functionality
  - Redux state management

#### `common` Module
- **Type**: Android Library
- **Purpose**: Core business logic, data persistence, encryption, and utilities
- **Key Components**:
  - Room database and DAOs
  - Encryption utilities (SQLCipher, Android KeyStore)
  - Backup/restore functionality
  - Data models
  - Dependency injection setup
  - Background workers

#### `shared` Module
- **Type**: Kotlin Multiplatform Library
- **Purpose**: Platform-agnostic UI components and utilities
- **Targets**: Android, Desktop (JVM)
- **Key Components**:
  - Reusable Compose components
  - Redux/state management utilities
  - Platform-independent business logic

#### `baselineprofile` Module
- **Type**: Android Test
- **Purpose**: Generates baseline profiles for improved app startup and runtime performance

## Technology Stack

### Core Technologies
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material Design 3
- **Build System**: Gradle (Kotlin DSL)
- **Minimum SDK**: API 23 (Android 6.0)
- **Target SDK**: API 35
- **Java Version**: 17

### Key Libraries

#### Data & Persistence
- **Room Database** (`2.6.x`): Local database abstraction
- **SQLCipher** (`4.7.2`): Database encryption
- **DataStore Preferences** (`1.1.4`): Settings storage
- **Gson** (`2.13.2`): JSON serialization

#### Dependency Injection
- **Hilt/Dagger** (`2.56.2`): Dependency injection framework
- **Hilt Work**: Integration with WorkManager

#### UI & Compose
- **Jetpack Compose** (`1.8.3`): Declarative UI framework
- **Material3**: Material Design 3 components
- **Compose Runtime**: Core Compose runtime

#### Security & Encryption
- **Android Security Crypto** (`1.1.0-alpha07`): Encrypted SharedPreferences
- **Apache Commons Codec** (`1.18.0`): Encoding/decoding utilities

#### Utilities
- **WorkManager** (`2.8.1`): Background task scheduling
- **OpenCSV** (`5.11`): CSV import/export
- **Kotlinx Serialization** (`1.8.1`): Kotlin serialization

#### State Management
- **Redux Kotlin Compose** (`0.6.0`): Redux pattern for Compose

## Architecture Patterns

### MVI (Model-View-Intent) with Redux

KeyPass implements the MVI pattern using Redux for state management:

```
┌─────────┐         ┌────────┐         ┌─────────┐
│  View   │────────>│ Action │────────>│ Reducer │
│ (Compose)│         └────────┘         └─────────┘
└─────────┘              │                    │
     ^                   │                    v
     │                   v              ┌─────────┐
     │            ┌────────────┐        │  State  │
     └────────────│ Middleware │<───────└─────────┘
                  └────────────┘
```

#### State Management Components

**States** (`app/src/main/java/com/yogeshpaliyal/keypass/ui/redux/states/`)
- Each screen has its own state class
- `KeyPassState`: Root state containing all screen states
- Examples: `HomeState`, `AuthState`, `AccountDetailState`, `SettingsState`

**Actions** (`app/src/main/java/com/yogeshpaliyal/keypass/ui/redux/actions/`)
- Represent user intents and system events
- `NavigationAction`: Navigation between screens
- `StateUpdateAction`: Update current screen state
- `BottomSheetAction`: Control bottom sheet visibility
- `UpdateDialogAction`: Manage dialog states

**Reducers** (`KeyPassRedux.kt`)
- Pure functions that transform state based on actions
- Handle navigation stack management
- Update screen states immutably

**Middleware** (`app/src/main/java/com/yogeshpaliyal/keypass/ui/redux/middlewares/`)
- `UtilityMiddleware`: Side effects and utility operations
- `IntentNavigationMiddleware`: Handle Android intents and deep links

### Screen Architecture

Each screen follows this structure:
```
ScreenName/
├── ScreenNameScreen.kt      # Composable UI
├── ScreenNameViewModel.kt   # Business logic (if needed)
└── ScreenNameState.kt        # Redux state
```

## Data Layer

### Database Architecture

#### Entity Model
The app uses a single main entity:

**AccountModel** (`common/src/main/java/com/yogeshpaliyal/common/data/AccountModel.kt`)
```kotlin
@Entity(tableName = "account")
data class AccountModel(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var title: String? = null,
    var uniqueId: String? = getRandomString(),
    var username: String? = null,
    var password: String? = null,
    var secret: String? = null,      // TOTP secret
    var site: String? = null,
    var notes: String? = null,
    var tags: String? = null,
    var type: Int? = AccountType.DEFAULT
)
```

**Account Types**:
- `DEFAULT (0)`: Standard password account
- `TOTP (1)`: Time-based One-Time Password account

#### Database Access

**AppDatabase** (`common/src/main/java/com/yogeshpaliyal/common/AppDatabase.kt`)
- Room database with SQLCipher encryption
- Current version: 8
- Migration support for schema changes

**DbDao** (`common/src/main/java/com/yogeshpaliyal/common/db/DbDao.kt`)
- Provides CRUD operations for accounts
- Search and filter functionality
- Tag management
- Sorting capabilities

### Encryption & Security

#### Database Encryption
- Uses **SQLCipher** for transparent database encryption
- Auto-generated encryption key stored in Android KeyStore
- Automatic migration from unencrypted to encrypted database

**Key Generation Flow**:
1. Check if database password exists in UserSettings
2. If not, generate random password using `getRandomString()`
3. Store password securely using DataStore
4. Migrate existing unencrypted database if present

#### Backup Encryption
**CryptoManager** (`common/src/main/java/com/yogeshpaliyal/common/utils/CryptoManager.kt`)
- Uses Android KeyStore for encryption keys
- AES encryption with randomized IV
- Algorithm: `AES/CBC/PKCS7Padding`
- Automatic key generation and management

**Backup Flow**:
1. User initiates backup with password
2. Export all accounts from database
3. Encrypt data using user-provided password
4. Save to user-selected location

**Restore Flow**:
1. User selects backup file and provides password
2. Decrypt backup data
3. Parse and validate accounts
4. Import into database

### Data Store

**UserSettings** (`common/src/main/java/com/yogeshpaliyal/common/data/UserSettings.kt`)
- Stores app preferences using DataStore
- Settings include:
  - Database password
  - Auto-backup configuration
  - Default password length
  - Theme preferences
  - Security settings

## UI Layer

### Jetpack Compose

The entire UI is built using Jetpack Compose with Material Design 3:

#### Key UI Packages
```
app/src/main/java/com/yogeshpaliyal/keypass/ui/
├── home/              # Main account list screen
├── detail/            # Account detail/edit screen
├── auth/              # Authentication screen
├── settings/          # Settings screen
├── backup/            # Backup/restore screens
├── generate/          # Password generator
├── about/             # About screen
├── nav/               # Navigation components
├── commonComponents/  # Reusable UI components
├── style/             # Theme and styling
└── redux/             # Redux state management
```

### Navigation

Navigation is managed through Redux actions:
- `NavigationAction`: Navigate to new screen
- `GoBackAction`: Navigate back
- Navigation stack maintained in `KeyPassRedux`
- Support for clearing back stack

### Common Components

Reusable Compose components in `ui/commonComponents/`:
- `AppTextField`: Styled text input fields
- `AppButton`: Styled buttons
- `AccountCard`: Display account information
- `PasswordField`: Password input with visibility toggle
- `OTPDisplay`: TOTP code display with progress

## Security Architecture

### Multi-Layer Security

```
┌─────────────────────────────────────────┐
│     User Authentication                 │
│  (PIN/Pattern/Biometric via Android)    │
└─────────────────────────────────────────┘
                   ▼
┌─────────────────────────────────────────┐
│      App-Level Security                 │
│  • Screenshot blocking                  │
│  • Secure flags on sensitive screens    │
│  • App password/hint                    │
└─────────────────────────────────────────┘
                   ▼
┌─────────────────────────────────────────┐
│     Data Encryption Layer               │
│  • SQLCipher database encryption        │
│  • Android KeyStore for keys            │
│  • Encrypted backups                    │
└─────────────────────────────────────────┘
                   ▼
┌─────────────────────────────────────────┐
│      Storage Layer                      │
│  • Local SQLite database                │
│  • DataStore for preferences            │
│  • No cloud/network access              │
└─────────────────────────────────────────┘
```

### Security Features

1. **Database Encryption**
   - SQLCipher transparent encryption
   - Unique encryption key per installation
   - Key stored in Android KeyStore

2. **Backup Security**
   - User-provided password for backups
   - AES encryption with secure parameters
   - Salt and IV stored with encrypted data

3. **Screenshot Protection**
   - `FLAG_SECURE` on sensitive screens
   - Prevents screenshots and screen recording

4. **Authentication**
   - Device credentials (PIN/Pattern/Biometric)
   - Optional app-level password
   - Password hint support

5. **TOTP (Time-based OTP)**
   - RFC 6238 compliant
   - Supports custom time steps
   - Progress indicator for token validity

## Dependency Injection

### Hilt/Dagger Setup

**Application Component**:
```kotlin
@HiltAndroidApp
class MyApplication : CommonMyApplication()
```

**Module Organization** (`common/src/main/java/com/yogeshpaliyal/common/di/`):
- `AppModule`: Provides database instance and dependencies
- Singleton scope for database and DAOs
- Automatic injection into ViewModels and Services

**Key Providers**:
```kotlin
@Provides
@Singleton
fun getDb(@ApplicationContext context: Context): AppDatabase
```

### Injection Points

- **ViewModels**: Annotated with `@HiltViewModel`
- **Activities**: Annotated with `@AndroidEntryPoint`
- **Services**: Autofill service with Hilt injection
- **Workers**: WorkManager workers with HiltWorker

## Build Configuration

### Product Flavors

```kotlin
productFlavors {
    create("free") {
        isDefault = true
    }
    create("pro") {
        applicationIdSuffix = ".pro"
    }
}
```

### Build Types

- **Release**:
  - Minification enabled (R8)
  - Resource shrinking enabled
  - ProGuard rules applied
  
- **Debug**:
  - Application ID suffix: `.staging`
  - Debug signing config
  - No minification for easier debugging

### Gradle Plugins

- `com.android.application`: Android app plugin
- `kotlin-android`: Kotlin Android support
- `kotlin-kapt`: Annotation processing
- `dagger.hilt.android.plugin`: Hilt DI
- `org.jetbrains.kotlin.plugin.serialization`: Kotlinx serialization
- `androidx.baselineprofile`: Performance optimization
- `com.spotify.ruler`: APK size analysis

### Build Versions

- **Compile SDK**: 35
- **Min SDK**: 23
- **Target SDK**: 35
- **Version Code**: Auto-incremented
- **Version Name**: Semantic versioning (e.g., 1.4.42)

## Key Components

### Autofill Service

**Location**: `app/src/main/java/com/yogeshpaliyal/keypass/autofill/`

**Components**:
- `MyAutofillService`: Main service implementation
- `AutofillHelper`: Autofill response building
- `StructureParser`: Parse view structure for fillable fields
- `PackageVerifier`: Verify app authenticity

**Flow**:
1. Android triggers autofill on compatible fields
2. Service parses view structure to identify fields
3. Query matching accounts from database
4. Present authentication UI if needed
5. Fill fields with selected account credentials

### Import/Export

**Supported Formats**:
- Google Chrome CSV
- KeePass CSV
- KeyPass native encrypted format

**Importers** (`app/src/main/java/com/yogeshpaliyal/keypass/importer/`):
- Base importer interface
- Format-specific parsers
- Duplicate detection
- Validation

### Password Generator

**Features**:
- Configurable length
- Character set options (uppercase, lowercase, numbers, symbols)
- Secure random generation
- Integration with account creation

### TOTP (2FA) Support

**Implementation** (`common/src/main/java/com/yogeshpaliyal/common/utils/TOTPHelper.kt`):
- HMAC-SHA1 based token generation
- 30-second time windows
- QR code import support
- Progress indicator for token expiry

### Background Workers

**Auto-Backup Worker** (`common/src/main/java/com/yogeshpaliyal/common/worker/`):
- Periodic automatic backups
- Configured through settings
- Uses WorkManager for reliability
- Stores backups in user-configured location

## Getting Started for Contributors

### Development Environment Setup

1. **Prerequisites**:
   - Android Studio Ladybug Feature Drop (2024.2.2) or later
   - JDK 17
   - Git

2. **Clone Repository**:
   ```bash
   git clone https://github.com/yogeshpaliyal/KeyPass.git
   cd KeyPass
   ```

3. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to cloned directory
   - Wait for Gradle sync

4. **Run the App**:
   - Select `app` configuration
   - Choose an emulator or connected device
   - Click Run (Shift+F10)

### Building the Project

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Generate baseline profile
./gradlew :baselineprofile:pixel6Api31BenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
```

### Code Structure Guidelines

1. **Follow existing patterns**:
   - Use Redux for state management
   - Create screen-specific states in `redux/states/`
   - Define actions in `redux/actions/`
   - Place UI in appropriate screen package

2. **Security considerations**:
   - Never log sensitive data
   - Use secure flags on sensitive screens
   - Follow encryption best practices
   - Validate all user input

3. **Testing**:
   - Write unit tests for business logic
   - Test database migrations
   - Verify encryption/decryption
   - Test import/export functionality

4. **UI Development**:
   - Follow Material Design 3 guidelines
   - Support dark mode
   - Ensure accessibility
   - Test on various screen sizes

### Key Files to Understand

1. **Entry Point**:
   - `app/src/main/java/com/yogeshpaliyal/keypass/MyApplication.kt`

2. **Database**:
   - `common/src/main/java/com/yogeshpaliyal/common/AppDatabase.kt`
   - `common/src/main/java/com/yogeshpaliyal/common/db/DbDao.kt`

3. **Redux**:
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/redux/KeyPassRedux.kt`

4. **Security**:
   - `common/src/main/java/com/yogeshpaliyal/common/utils/CryptoManager.kt`
   - `common/src/main/java/com/yogeshpaliyal/common/di/module/AppModule.kt`

5. **Main Screen**:
   - `app/src/main/java/com/yogeshpaliyal/keypass/ui/home/`

### Contributing Workflow

1. Check existing issues or create a new one
2. Fork the repository
3. Create a feature branch (`git checkout -b feature/amazing-feature`)
4. Make your changes following the code style
5. Write/update tests as appropriate
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

For more details, see [CONTRIBUTING.md](./CONTRIBUTING.md).

### Useful Resources

- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [SQLCipher for Android](https://www.zetetic.net/sqlcipher/sqlcipher-for-android/)
- [Redux Pattern](https://redux.js.org/understanding/thinking-in-redux/three-principles)

## Troubleshooting

### Common Issues

1. **Build Failures**:
   - Ensure JDK 17 is installed and selected
   - Clear Gradle cache: `./gradlew clean`
   - Invalidate caches in Android Studio

2. **Database Migration Errors**:
   - Check migration definitions in `AppModule.kt`
   - Test migrations with unit tests
   - Verify version numbers are sequential

3. **Dependency Injection Issues**:
   - Ensure all modules are annotated correctly
   - Check Hilt component hierarchy
   - Verify @Inject constructors

4. **Encryption Problems**:
   - Verify Android KeyStore is available
   - Check device API level compatibility
   - Ensure proper key generation

## Future Architecture Considerations

### Planned Improvements

1. **Desktop Support**: Full integration of desktop module for Windows/Mac/Linux
2. **Cloud Sync**: Optional encrypted cloud synchronization
3. **Plugin System**: Extensible architecture for custom importers/exporters
4. **Multi-Database**: Support for multiple encrypted databases
5. **Biometric Vault**: Enhanced biometric authentication integration

### Scalability

The current architecture supports:
- Thousands of accounts per database
- Multiple concurrent operations
- Background processing
- Efficient memory usage

For extremely large datasets (10,000+ accounts), consider:
- Implementing pagination in list views
- Adding database indexes for frequently queried fields
- Using Flow-based reactive queries
- Optimizing search algorithms

---

**Last Updated**: December 2024
**KeyPass Version**: 1.4.42
**Document Version**: 1.0

For questions or clarifications about the architecture, please open an issue on [GitHub](https://github.com/yogeshpaliyal/KeyPass/issues).
