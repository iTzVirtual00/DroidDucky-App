# DroidDucky

An Android application for managing and executing DuckyScript payloads via USB HID gadget devices. This app turns your Android device into a USB Rubber Ducky, allowing you to automate keyboard input on connected computers.

## Requirements

- HID gadget device configured at `/dev/hidg0` (configurable)

## Project Structure

```
app/src/main/java/me/itzvirtual/droidducky/
├── MainActivity.kt              # Main entry point, navigation setup
├── data/
│   ├── Script.kt               # Script data model
│   └── ScriptRepository.kt     # Data persistence layer
├── hid/
│   ├── HidKeyCodes.kt          # USB HID keyboard scan codes
│   ├── HidKeyboard.kt          # Low-level HID device communication
│   └── DuckyScriptExecutor.kt  # DuckyScript parser and executor
└── ui/
    ├── MainViewModel.kt        # Main screen state management
    ├── ScriptEditorViewModel.kt # Editor screen state management
    ├── navigation/
    │   └── Navigation.kt       # Navigation route definitions
    ├── screens/
    │   ├── MainScreen.kt       # Main UI with device status & script list
    │   └── ScriptEditorScreen.kt # Script editor UI
    └── theme/                  # Material 3 theming
```

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with Jetpack Compose for UI.

### Data Layer

#### `Script` (data/Script.kt)
Simple data class representing a script:
```kotlin
data class Script(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val content: String = ""
)
```

#### `ScriptRepository` (data/ScriptRepository.kt)
Handles data persistence using SharedPreferences with JSON serialization.

**Key methods:**
- `getScripts()` / `saveScripts()` - CRUD operations for scripts
- `getDevicePath()` / `setDevicePath()` - HID device path configuration
- Scripts are stored as JSON array in SharedPreferences

### HID Layer

#### `HidKeyCodes` (hid/HidKeyCodes.kt)
Contains USB HID keyboard scan codes and modifiers as defined in the USB HID Usage Tables specification.

**Modifier flags (byte 0 of HID report):**
- `MOD_LEFT_CTRL` (0x01), `MOD_LEFT_SHIFT` (0x02), `MOD_LEFT_ALT` (0x04), `MOD_LEFT_GUI` (0x08)
- Right-hand variants available

**Key method:**
```kotlin
fun getKeyForChar(char: Char): Pair<Byte, Byte>  // Returns (modifier, keyCode)
```

#### `HidKeyboard` (hid/HidKeyboard.kt)
Low-level interface to the HID gadget device.

**HID Report Format (8 bytes):**
```
[0] Modifier byte (ctrl, shift, alt, gui flags)
[1] Reserved (always 0)
[2] Key code 1
[3-7] Additional key codes (unused, set to 0)
```

**Key methods:**
- `sendKey(modifier, keyCode)` - Send single keystroke
- `typeString(text)` - Type a string character by character
- `isDeviceAvailable()` - Check if HID device exists and is writable

#### `DuckyScriptExecutor` (hid/DuckyScriptExecutor.kt)
Parses and executes DuckyScript commands.

**Supported Commands:**

| Command | Description | Example |
|---------|-------------|---------|
| `STRING` | Type text | `STRING Hello World` |
| `ENTER` / `RETURN` | Press Enter | `ENTER` |
| `DELAY` | Wait (milliseconds) | `DELAY 1000` |
| `DEFAULT_DELAY` | Set delay between commands | `DEFAULT_DELAY 100` |
| `GUI` / `WINDOWS` | Windows/Super key combo | `GUI r` |
| `CTRL` / `CONTROL` | Control key combo | `CTRL c` |
| `ALT` | Alt key combo | `ALT F4` |
| `SHIFT` | Shift key combo | `SHIFT INSERT` |
| `CTRL-ALT` | Ctrl+Alt combo | `CTRL-ALT DELETE` |
| `CTRL-SHIFT` | Ctrl+Shift combo | `CTRL-SHIFT ESCAPE` |
| `ALT-SHIFT` | Alt+Shift combo | `ALT-SHIFT` |
| `TAB`, `SPACE`, `BACKSPACE`, `DELETE` | Special keys | `TAB` |
| `UP`, `DOWN`, `LEFT`, `RIGHT` | Arrow keys | `DOWN` |
| `F1`-`F12` | Function keys | `F5` |
| `ESC` / `ESCAPE` | Escape key | `ESC` |
| `INSERT`, `HOME`, `END`, `PAGEUP`, `PAGEDOWN` | Navigation | `HOME` |
| `CAPSLOCK`, `NUMLOCK`, `SCROLLLOCK` | Lock keys | `CAPSLOCK` |
| `PRINTSCREEN`, `PAUSE` / `BREAK` | System keys | `PRINTSCREEN` |
| `MENU` / `APP` | Context menu key | `MENU` |
| `REM` | Comment (ignored) | `REM This is a comment` |
| `REPEAT` | Repeat last command N times | `REPEAT 5` |

**Execution with cancellation support:**
```kotlin
suspend fun execute(
    script: String,
    onProgress: ((Int, Int) -> Unit)? = null,  // (currentLine, totalLines)
    onLog: ((String) -> Unit)? = null
): ExecutionResult
```

The executor checks for coroutine cancellation before each line, allowing scripts to be stopped mid-execution.

### UI Layer

#### ViewModels

**`MainViewModel`** - Manages:
- Device status (path, exists, error)
- Script list
- Script execution with progress tracking
- Cancellation support via `Job` tracking

**`ScriptEditorViewModel`** - Manages:
- Current script content
- Save state tracking
- Content updates

#### Screens

**`MainScreen`** - Features:
- Device status card (green = ready, red = error)
- Tap to edit device path
- Refresh button for device status
- Script list with play/stop/delete actions
- FAB to add new scripts

**`ScriptEditorScreen`** - Features:
- Monospace text editor
- Unsaved changes indicator
- Save button (auto-navigates back)
- Discard confirmation on back with unsaved changes

### Navigation

Uses Jetpack Navigation Compose with two routes:
- `main` - Main screen
- `script_editor/{scriptId}` - Editor with script ID parameter

## Adding New DuckyScript Commands

1. Add key code constants to `HidKeyCodes.kt` if needed
2. Add command handling in `DuckyScriptExecutor.executeCommand()`
3. Follow the existing pattern:
```kotlin
"NEW_COMMAND" -> {
    lastCommand = Command.KeyPress(modifiers, keyCode)
    keyboard.sendKey(modifiers, keyCode)
}
```

## Key Implementation Details

### HID Report Writing
The app writes directly to `/dev/hidg0` (or configured path). Each keystroke requires:
1. Press report (modifier + key code)
2. Short delay (10ms)
3. Release report (all zeros)
4. Short delay (10ms)

### Script Execution Flow
1. User taps play → `MainViewModel.playScript()`
2. Creates `HidKeyboard` and `DuckyScriptExecutor`
3. Executor parses script line by line
4. Each command translates to HID reports
5. Progress updates via callback
6. Cancellation checked before each line

### State Management
- UI state exposed via `StateFlow`
- Single source of truth in ViewModels
- Repository pattern for data access
- Coroutines for async operations

## Dependencies

- Jetpack Compose + Material 3
- Navigation Compose
- Lifecycle ViewModel
- Kotlin Coroutines

## Common Issues

**Device not found**: Ensure HID gadget is configured and `/dev/hidg0` exists

**Permission denied**: App needs write access to `/dev/hidg0`, may require root

**Keys not working**: Check target system keyboard layout matches US QWERTY (default)

## Future Improvements

- [ ] Keyboard layout support (international layouts)
- [ ] Script import/export
- [ ] Syntax highlighting in editor
- [ ] Script variables and loops
- [ ] Mouse HID support
