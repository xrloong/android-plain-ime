# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

樸素輸入法 (Plain IME) is an Android Chinese input method focusing on table-based input systems, starting with Cangjie (倉頡). The architecture emphasizes clean separation between data, logic, and UI layers.

## Build Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Clean build artifacts
./gradlew clean
```

## Testing Commands

```bash
# Run all unit tests
./gradlew test

# Run all Android instrumentation tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests cin.CINParserTest
./gradlew test --tests table.TableManagerTest
./gradlew test --tests engine.InputMethodEngineTest

# Run specific test method
./gradlew test --tests cin.CINParserTest.testParse_validCIN_basic

# Run tests with fresh execution (ignore cache)
./gradlew test --rerun-tasks

# View test reports at:
# app/build/reports/tests/testDebugUnitTest/index.html
```

## Architecture

### Component Hierarchy

```
SimpleInputMethodService (IME entry point)
├── InputMethodView (UI integration layer)
│   ├── ComposeView (displays current input code)
│   ├── CandidateView (shows candidate characters)
│   └── QwertyKeyboardView (keyboard layout with Chinese roots)
└── InputMethodEngineManager (high-level engine API)
    ├── TableManager (table loading & state management)
    │   ├── TableCache (caches parsed tables)
    │   ├── TableLoader (loads from assets)
    │   └── CINParser (parses CIN format files)
    └── InputMethodEngine (input logic & candidate lookup)
```

### Key Design Patterns

**State Management via LiveData**
- `TableManager` uses `TableLoadState` sealed class (Loading/Success/Error) with LiveData
- `InputMethodEngineManager` translates to `EngineState` for UI consumption
- Background loading on single-thread executor to avoid blocking main thread

**Data Flow**
1. User presses key → `QwertyKeyboardView` → `InputMethodView` → `SimpleInputMethodService`
2. Service calls `InputMethodEngineManager.processKey()`
3. Engine queries `TableManager.getCandidates()` which uses cached `CINParseResult`
4. Candidates flow back through the chain to update `CandidateView`

**Separation of Concerns**
- `CINParser`: Pure parsing logic, completely independent (easy to unit test)
- `TableManager`: Async loading, caching, state management
- `InputMethodEngine`: Stateful input logic (code buffer, candidate management)
- `InputMethodEngineManager`: Bridges table loading with input engine lifecycle
- UI components: Pure views that delegate events upward

### Critical Files

**Core Engine**
- `app/src/main/java/idv/xrloong/plainime/SimpleInputMethodService.kt` - Android IME service entry point
- `app/src/main/java/engine/InputMethodEngineManager.kt` - High-level engine API that integrates TableManager
- `app/src/main/java/engine/InputMethodEngine.kt` - Input logic: processes keys, manages code buffer and candidates

**Table Management**
- `app/src/main/java/table/TableManager.kt` - Async table loading with LiveData state management
- `app/src/main/java/cin/CINParser.kt` - CIN format parser (independent module)
- `app/src/main/java/table/TableLoader.kt` - Loads tables from assets

**UI Components**
- `app/src/main/java/ui/InputMethodView.kt` - Main view integrating all UI components
- `app/src/main/java/ui/keyboard/QwertyKeyboardView.kt` - Keyboard with Chinese root labels (字根)
- `app/src/main/java/ui/candidate/CandidateView.kt` - Candidate character selection
- `app/src/main/java/ui/compose/ComposeView.kt` - Current input code display

**Data**
- `app/src/main/assets/qhcj.cin` - Built-in Cangjie lookup table (~27,000 characters)

## Important Implementation Details

### Keyboard Display
The keyboard shows **Chinese roots (字根)** as primary labels instead of English letters. For example, key "q" displays "手", key "w" displays "田". The `QwertyKeyboardView` maintains the mapping but sends English letter codes to the engine.

### CIN File Format
- Uses `%chardef begin`/`%chardef end` sections for character definitions
- Format: `<code><TAB or space><character>`
- Supports metadata: `%ename`, `%cname`, `%encoding`, `%selkey`
- Parser skips comments (lines starting with `#`) and `%keyname` sections

### State Management
When TableManager state changes:
- **Loading**: UI shows progress indicator, engine is null
- **Success**: Creates new InputMethodEngine with parsed table data
- **Error**: Shows error message with retry button if `canRetry=true`

### Input Logic
- Keys a-z: Added to code buffer, triggers candidate lookup
- Space/Enter: Commits first candidate (or code itself if no candidates)
- Backspace: Removes last character from code buffer, or deletes text if buffer empty
- Candidate selection: Clears code buffer and commits selected character

## Testing Strategy

**Unit Tests** (app/src/test/java/)
- `CINParserTest`: 13 tests covering parsing, error handling, edge cases
- `TableManagerTest`: 11 tests for state management, async loading, caching
- `InputMethodEngineTest`: Input logic, candidate selection, commit behavior
- `InputMethodEngineManagerTest`: Integration between TableManager and Engine

**Integration Tests** (app/src/androidTest/java/)
- `QhcjCINIntegrationTest`: Verifies built-in Cangjie table loads correctly

All tests use standard JUnit 4 with AndroidX test libraries.

## Development Workflow

1. **Adding New Input Method**: Create new CIN file in assets, update `TableLoader` constant
2. **Modifying Keyboard Layout**: Edit `QwertyKeyboardView.setupKeyboard()` row definitions
3. **Changing Input Logic**: Modify `InputMethodEngine` methods (processKey, commit, etc.)
4. **UI Changes**: Update corresponding view in `ui/` package, maintain listener pattern

## Project Metadata

- **Package**: idv.xrloong.plainime
- **minSdk**: 21 (Android 5.0)
- **targetSdk**: 35 (Android 15)
- **Language**: Kotlin 1.9.23
- **Build System**: Gradle 8.2.2

## Code Conventions

- Use Kotlin property access syntax over getter/setter methods
- Sealed classes for state modeling (TableLoadState, EngineState, KeyboardState)
- LiveData for observable state in managers
- Listener interfaces for UI event delegation
- Package-private visibility by default, explicit modifiers when needed
