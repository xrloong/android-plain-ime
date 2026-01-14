# AGENTS.md

## Overview
This is an Android input method project implementing a Chinese IME with support for Cangjie input method. The project follows a "資料-邏輯-介面" (Data-Logic-UI) layered architecture with clear separation of concerns.

## Build System Commands

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests cin.CINParserTest
./gradlew test --tests table.TableManagerTest
./gradlew test --tests engine.InputMethodEngineTest

# Run specific test method
./gradlew test --tests cin.CINParserTest.testParse_validCIN_basic
./gradlew test --tests table.TableManagerTest.testLoadTable_success_updatesStateToSuccess

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all checks (tests + lint if configured)
./gradlew check

# Ignore test cache
./gradlew test --rerun-tasks
```

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install debug APK to connected device
./gradlew installDebug
```

### Test Reports
- HTML Report: `app/build/reports/tests/testDebugUnitTest/index.html`
- XML Report: `app/build/test-results/testDebugUnitTest/`

## Code Style Guidelines

### Language and Framework
- **Language**: Kotlin 100% (no Java code)
- **Target**: Android API 21-35, JVM 1.8
- **Architecture**: Clean layered architecture (Data/Logic/UI separation)

### Naming Conventions
- **Classes**: PascalCase (e.g., `InputMethodEngine`, `CINParser`, `TableManager`)
- **Packages**: lowercase, domain-based (e.g., `cin`, `engine`, `table`, `ui`)
- **Functions/Methods**: camelCase, descriptive verbs (e.g., `loadTable()`, `processKey()`, `getCandidates()`)
- **Properties**: camelCase, use `val` for immutable, `var` for mutable
- **Constants**: UPPER_SNAKE_CASE (e.g., `BUILTIN_CANGJIE`)
- **Test Methods**: `testFeature_condition_expectedResult` pattern (e.g., `testParse_validCIN_basic`)

### Code Organization

#### Layer Structure
```
cin/           # Data Layer - CIN Parser (independent module)
table/         # Data Layer - Table Management  
engine/        # Logic Layer - Input Processing
ui/            # UI Layer - User Interface
sample/        # Examples/Usage patterns
```

#### Key Patterns
- **Sealed Classes**: Use for state management (`TableLoadState`, `EngineState`)
- **Data Classes**: Use for models with `data class` keyword
- **LiveData**: Use `MutableLiveData` privately, expose as `LiveData`
- **ViewModel**: Separate UI logic from business logic, lifecycle-aware

### Kotlin Style
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Prefer under 100 characters
- **Null Safety**: Explicit nullable types with `?`, avoid `!!` operator
- **Immutability**: Prefer `val` over `var`
- **Extension Functions**: Use where appropriate for utility functions
- **When Expressions**: Prefer over if-else chains for enums/sealed classes

### Documentation
- **KDoc**: Required for all public classes and functions
- **Comments**: Use Chinese language to match project README
- **TODO**: Use sparingly, include action items and owner if known

### Error Handling
- **Specific Exceptions**: Use custom exception types (`CINParseException`)
- **Error States**: Include detailed messages and retry suggestions
- **Try-Catch**: Handle specific exceptions, avoid generic catches
- **User Messages**: Provide actionable error messages with Chinese translations

### Testing Standards

#### Test Structure
```kotlin
class ClassNameTest {
    private lateinit var subject: ClassName
    
    @Before
    fun setup() {
        subject = ClassName()  // Fresh instance per test
    }
    
    @Test
    fun testFeature_condition_expectedResult() {
        // Arrange
        val input = "value"
        
        // Act
        val result = subject.doSomething(input)
        
        // Assert
        assertEquals("expected", result)
    }
}
```

#### Testing Guidelines
- **Framework**: JUnit 4.13.2 with AndroidX Arch Core Testing
- **Coverage**: Aim for comprehensive coverage of core logic
- **Test Data**: Use `app/src/test/resources/` for test files
- **Async Testing**: Use `kotlinx-coroutines-test` for coroutine testing
- **LiveData Testing**: Use `InstantTaskExecutorRule` for LiveData tests

### Android-Specific Patterns

#### Custom Views
- **Constructors**: Use `@JvmOverloads` for View constructors
- **Initialization**: Programmatic layout (avoid XML for custom components)
- **Colors**: Use 0xFFAARRGGBB format for color constants
- **Threading**: Background operations for file I/O, UI updates via `postValue()`

#### IME Service
- **Service Class**: Extend `InputMethodService`
- **Lifecycle**: Implement `onCreateInputView()`, `onStartInputView()`
- **Text I/O**: Use `currentInputConnection` for text operations
- **Keyboard Management**: Handle display/hide lifecycle properly

#### Dependencies Management
- **ViewModel**: Use `androidx.lifecycle:lifecycle-viewmodel-ktx`
- **LiveData**: Use `androidx.lifecycle:lifecycle-livedata-ktx`
- **Coroutines**: Use `kotlinx-coroutines-test` for testing
- **Material**: Use `com.google.android.material` components

### File and Asset Management
- **CIN Files**: Store in `app/src/main/assets/`
- **Test Resources**: Store in `app/src/test/resources/`
- **Built-in Data**: `qhcj.cin` (27,674 lines of Cangjie data)
- **Access Pattern**: Use `context.assets.open(filename)` for asset loading

### State Management Patterns
```kotlin
// Sealed class for states
sealed class TableLoadState {
    object Loading : TableLoadState()
    data class Success(val data: CINParseResult) : TableLoadState()
    data class Error(
        val message: String, 
        val throwable: Throwable? = null,
        val retrySuggestion: String? = null
    ) : TableLoadState()
}

// LiveData pattern
private val _state = MutableLiveData<TableLoadState>()
val state: LiveData<TableLoadState> = _state
```

### Import Organization
- **Android/AndroidX**: Group Android framework imports first
- **Kotlin**: Kotlin standard library imports second
- **Project**: Local project imports third
- **Blank Line**: Separate groups with blank lines

### Performance Guidelines
- **Background Loading**: Use single-threaded executor for CIN parsing
- **Caching**: Implement caching in TableManager to avoid repeated parsing
- **Memory**: Be mindful of large CIN files (27k+ lines)
- **UI Thread**: Never block UI thread with file operations

## Architecture Principles

1. **Separation of Concerns**: Clear layer boundaries (data/logic/ui)
2. **Independence**: CIN parser is completely standalone and testable
3. **Reactivity**: Use LiveData for state-driven UI updates
4. **Error Handling**: Detailed error messages with actionable suggestions
5. **Testing First**: High test coverage from project inception
6. **Extensibility**: Architecture supports multiple input methods
7. **Resource Management**: Background loading, caching to avoid blocking UI

## Development Workflow

1. **Before Coding**: Run existing tests to ensure clean state
2. **During Development**: Add unit tests for new functionality
3. **After Changes**: Run `./gradlew test` to verify no regressions
4. **Before Commit**: Ensure all tests pass and code follows conventions
5. **UI Changes**: Add corresponding Espresso tests for UI modifications

## Project Maturity Status

- ✅ Phase 1.1: TableManager & CIN parser (100%)
- ✅ Phase 1.2: Cangjie table data (100%)
- ✅ Phase 1.3: InputMethodEngine integration (100%)
- ✅ Phase 1.4: QWERTY keyboard UI (80%)
- ⏳ Phase 1.5: IME service integration (next)

## Quick Start Checklist

When working on this project:

- [ ] Run `./gradlew test` before starting
- [ ] Follow layered architecture (don't cross layer boundaries)
- [ ] Add unit tests for new features
- [ ] Use sealed classes for state management
- [ ] Include KDoc for public APIs
- [ ] Handle errors with detailed Chinese messages
- [ ] Test with `cangjie_test.cin` in test resources
- [ ] Verify no regressions with `./gradlew test` before commit