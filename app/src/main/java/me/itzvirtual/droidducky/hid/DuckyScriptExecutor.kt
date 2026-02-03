package me.itzvirtual.droidducky.hid

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * DuckyScript interpreter and executor
 * Parses DuckyScript commands and executes them via HID keyboard
 */
class DuckyScriptExecutor(private val keyboard: HidKeyboard) {
    
    private var defaultDelay: Long = 0
    private var lastCommand: Command? = null
    private var lastString: String = ""
    
    sealed class Command {
        data class StringCmd(val text: String) : Command()
        data class KeyPress(val modifiers: Int, val keyCode: Byte) : Command()
        data class Delay(val ms: Long) : Command()
        object Unsupported : Command()
    }
    
    data class ExecutionResult(
        val success: Boolean,
        val linesExecuted: Int,
        val error: String? = null,
        val wasCancelled: Boolean = false
    )
    
    /**
     * Execute a DuckyScript
     */
    suspend fun execute(
        script: String,
        onProgress: ((Int, Int) -> Unit)? = null,
        onLog: ((String) -> Unit)? = null
    ): ExecutionResult {
        val lines = script.lines()
        var lineNum = 0
        
        try {
            for (line in lines) {
                // Check for cancellation before each line
                if (!currentCoroutineContext().isActive) {
                    return ExecutionResult(false, lineNum, "Execution stopped", wasCancelled = true)
                }
                
                lineNum++
                onProgress?.invoke(lineNum, lines.size)
                
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) {
                    delay(defaultDelay)
                    continue
                }
                
                val parts = trimmedLine.split(" ", limit = 2)
                val cmd = parts[0].uppercase()
                val args = if (parts.size > 1) parts[1] else ""
                
                executeCommand(cmd, args, onLog)
                
                delay(defaultDelay)
            }
            
            return ExecutionResult(true, lineNum)
        } catch (e: CancellationException) {
            return ExecutionResult(false, lineNum, "Execution stopped", wasCancelled = true)
        } catch (e: Exception) {
            return ExecutionResult(false, lineNum, "Line $lineNum: ${e.message}")
        }
    }
    
    private suspend fun executeCommand(cmd: String, args: String, onLog: ((String) -> Unit)?) {
        when (cmd) {
            "STRING" -> {
                lastString = args
                lastCommand = Command.StringCmd(args)
                keyboard.typeString(args)
            }
            
            "ENTER", "RETURN" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_ENTER)
                keyboard.sendKey(0, HidKeyCodes.KEY_ENTER)
            }
            
            "DELAY" -> {
                lastCommand = Command.Unsupported
                val ms = args.toLongOrNull() ?: 0
                delay(ms)
            }
            
            "DEFAULT_DELAY", "DEFAULTDELAY" -> {
                lastCommand = Command.Unsupported
                defaultDelay = args.toLongOrNull() ?: 0
            }
            
            "GUI", "WINDOWS" -> {
                val keyCode = getKeyCodeForArg(args.lowercase())
                val modifiers = HidKeyCodes.MOD_LEFT_GUI.toInt() and 0xFF
                lastCommand = Command.KeyPress(modifiers, keyCode)
                keyboard.sendKey(modifiers, keyCode)
            }
            
            "CTRL", "CONTROL" -> {
                val modifiers = HidKeyCodes.MOD_LEFT_CTRL.toInt() and 0xFF
                if (args.isEmpty()) {
                    lastCommand = Command.KeyPress(modifiers, HidKeyCodes.KEY_NONE)
                    keyboard.sendKey(modifiers.toByte(), HidKeyCodes.KEY_NONE)
                } else {
                    val keyCode = getKeyCodeForArg(args)
                    lastCommand = Command.KeyPress(modifiers, keyCode)
                    keyboard.sendKey(modifiers, keyCode)
                }
            }
            
            "RCTRL", "RCONTROL", "RIGHT-CTRL", "RIGHT-CONTROL" -> {
                val modifiers = HidKeyCodes.MOD_RIGHT_CTRL.toInt() and 0xFF
                if (args.isEmpty()) {
                    lastCommand = Command.KeyPress(modifiers, HidKeyCodes.KEY_NONE)
                    keyboard.sendKey(modifiers.toByte(), HidKeyCodes.KEY_NONE)
                } else {
                    val keyCode = getKeyCodeForArg(args)
                    lastCommand = Command.KeyPress(modifiers, keyCode)
                    keyboard.sendKey(modifiers, keyCode)
                }
            }
            
            "ALT" -> {
                val modifiers = HidKeyCodes.MOD_LEFT_ALT.toInt() and 0xFF
                if (args.isEmpty()) {
                    lastCommand = Command.KeyPress(modifiers, HidKeyCodes.KEY_NONE)
                    keyboard.sendKey(modifiers.toByte(), HidKeyCodes.KEY_NONE)
                } else {
                    val keyCode = getKeyCodeForArg(args)
                    lastCommand = Command.KeyPress(modifiers, keyCode)
                    keyboard.sendKey(modifiers, keyCode)
                }
            }
            
            "RALT", "RIGHT-ALT", "ALTGR" -> {
                val modifiers = HidKeyCodes.MOD_RIGHT_ALT.toInt() and 0xFF
                if (args.isEmpty()) {
                    lastCommand = Command.KeyPress(modifiers, HidKeyCodes.KEY_NONE)
                    keyboard.sendKey(modifiers.toByte(), HidKeyCodes.KEY_NONE)
                } else {
                    val keyCode = getKeyCodeForArg(args)
                    lastCommand = Command.KeyPress(modifiers, keyCode)
                    keyboard.sendKey(modifiers, keyCode)
                }
            }
            
            "SHIFT" -> {
                val modifiers = HidKeyCodes.MOD_LEFT_SHIFT.toInt() and 0xFF
                if (args.isEmpty()) {
                    lastCommand = Command.KeyPress(modifiers, HidKeyCodes.KEY_NONE)
                    keyboard.sendKey(modifiers.toByte(), HidKeyCodes.KEY_NONE)
                } else {
                    // Handle SHIFT WINDOWS/GUI combo
                    val argParts = args.split(" ", limit = 2)
                    if (argParts[0].uppercase() in listOf("WINDOWS", "GUI")) {
                        val guiModifiers = (HidKeyCodes.MOD_LEFT_SHIFT.toInt() and 0xFF) or 
                                          (HidKeyCodes.MOD_LEFT_GUI.toInt() and 0xFF)
                        val keyCode = if (argParts.size > 1) getKeyCodeForArg(argParts[1]) else HidKeyCodes.KEY_NONE
                        lastCommand = Command.KeyPress(guiModifiers, keyCode)
                        keyboard.sendKey(guiModifiers, keyCode)
                    } else {
                        val keyCode = getKeyCodeForArg(args)
                        lastCommand = Command.KeyPress(modifiers, keyCode)
                        keyboard.sendKey(modifiers, keyCode)
                    }
                }
            }
            
            "CTRL-ALT" -> {
                val modifiers = (HidKeyCodes.MOD_LEFT_CTRL.toInt() and 0xFF) or 
                               (HidKeyCodes.MOD_LEFT_ALT.toInt() and 0xFF)
                val keyCode = if (args.isEmpty()) HidKeyCodes.KEY_NONE else getKeyCodeForArg(args)
                lastCommand = Command.KeyPress(modifiers, keyCode)
                keyboard.sendKey(modifiers, keyCode)
            }
            
            "CTRL-SHIFT" -> {
                val modifiers = (HidKeyCodes.MOD_LEFT_CTRL.toInt() and 0xFF) or 
                               (HidKeyCodes.MOD_LEFT_SHIFT.toInt() and 0xFF)
                val keyCode = if (args.isEmpty()) HidKeyCodes.KEY_NONE else getKeyCodeForArg(args)
                lastCommand = Command.KeyPress(modifiers, keyCode)
                keyboard.sendKey(modifiers, keyCode)
            }
            
            "ALT-SHIFT" -> {
                val modifiers = (HidKeyCodes.MOD_LEFT_ALT.toInt() and 0xFF) or 
                               (HidKeyCodes.MOD_LEFT_SHIFT.toInt() and 0xFF)
                lastCommand = Command.KeyPress(modifiers, HidKeyCodes.KEY_NONE)
                keyboard.sendKey(modifiers, HidKeyCodes.KEY_NONE)
            }
            
            "DOWNARROW", "DOWN" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_DOWN_ARROW)
                keyboard.sendKey(0, HidKeyCodes.KEY_DOWN_ARROW)
            }
            
            "UPARROW", "UP" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_UP_ARROW)
                keyboard.sendKey(0, HidKeyCodes.KEY_UP_ARROW)
            }
            
            "LEFTARROW", "LEFT" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_LEFT_ARROW)
                keyboard.sendKey(0, HidKeyCodes.KEY_LEFT_ARROW)
            }
            
            "RIGHTARROW", "RIGHT" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_RIGHT_ARROW)
                keyboard.sendKey(0, HidKeyCodes.KEY_RIGHT_ARROW)
            }
            
            "TAB" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_TAB)
                keyboard.sendKey(0, HidKeyCodes.KEY_TAB)
            }
            
            "SPACE" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_SPACE)
                keyboard.sendKey(0, HidKeyCodes.KEY_SPACE)
            }
            
            "BACKSPACE" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_BACKSPACE)
                keyboard.sendKey(0, HidKeyCodes.KEY_BACKSPACE)
            }
            
            "DELETE" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_DELETE)
                keyboard.sendKey(0, HidKeyCodes.KEY_DELETE)
            }
            
            "INSERT" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_INSERT)
                keyboard.sendKey(0, HidKeyCodes.KEY_INSERT)
            }
            
            "HOME" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_HOME)
                keyboard.sendKey(0, HidKeyCodes.KEY_HOME)
            }
            
            "END" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_END)
                keyboard.sendKey(0, HidKeyCodes.KEY_END)
            }
            
            "PAGEUP" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_PAGE_UP)
                keyboard.sendKey(0, HidKeyCodes.KEY_PAGE_UP)
            }
            
            "PAGEDOWN" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_PAGE_DOWN)
                keyboard.sendKey(0, HidKeyCodes.KEY_PAGE_DOWN)
            }
            
            "ESC", "ESCAPE" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_ESCAPE)
                keyboard.sendKey(0, HidKeyCodes.KEY_ESCAPE)
            }
            
            "PAUSE", "BREAK" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_PAUSE)
                keyboard.sendKey(0, HidKeyCodes.KEY_PAUSE)
            }
            
            "CAPSLOCK" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_CAPS_LOCK)
                keyboard.sendKey(0, HidKeyCodes.KEY_CAPS_LOCK)
            }
            
            "NUMLOCK" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_NUM_LOCK)
                keyboard.sendKey(0, HidKeyCodes.KEY_NUM_LOCK)
            }
            
            "SCROLLLOCK" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_SCROLL_LOCK)
                keyboard.sendKey(0, HidKeyCodes.KEY_SCROLL_LOCK)
            }
            
            "PRINTSCREEN" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_PRINT_SCREEN)
                keyboard.sendKey(0, HidKeyCodes.KEY_PRINT_SCREEN)
            }
            
            "MENU", "APP" -> {
                lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_MENU)
                keyboard.sendKey(0, HidKeyCodes.KEY_MENU)
            }
            
            "F1" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F1); keyboard.sendKey(0, HidKeyCodes.KEY_F1) }
            "F2" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F2); keyboard.sendKey(0, HidKeyCodes.KEY_F2) }
            "F3" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F3); keyboard.sendKey(0, HidKeyCodes.KEY_F3) }
            "F4" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F4); keyboard.sendKey(0, HidKeyCodes.KEY_F4) }
            "F5" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F5); keyboard.sendKey(0, HidKeyCodes.KEY_F5) }
            "F6" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F6); keyboard.sendKey(0, HidKeyCodes.KEY_F6) }
            "F7" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F7); keyboard.sendKey(0, HidKeyCodes.KEY_F7) }
            "F8" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F8); keyboard.sendKey(0, HidKeyCodes.KEY_F8) }
            "F9" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F9); keyboard.sendKey(0, HidKeyCodes.KEY_F9) }
            "F10" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F10); keyboard.sendKey(0, HidKeyCodes.KEY_F10) }
            "F11" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F11); keyboard.sendKey(0, HidKeyCodes.KEY_F11) }
            "F12" -> { lastCommand = Command.KeyPress(0, HidKeyCodes.KEY_F12); keyboard.sendKey(0, HidKeyCodes.KEY_F12) }
            
            "REM" -> {
                // Comment - log it but don't execute anything
                onLog?.invoke("REM: $args")
            }
            
            "REPEAT" -> {
                val repeatCount = args.toIntOrNull() ?: 1
                val cmdToRepeat = lastCommand
                
                if (cmdToRepeat == null || cmdToRepeat == Command.Unsupported) {
                    throw IllegalStateException("Cannot repeat: no valid previous command")
                }
                
                repeat(repeatCount) {
                    when (cmdToRepeat) {
                        is Command.StringCmd -> keyboard.typeString(cmdToRepeat.text)
                        is Command.KeyPress -> keyboard.sendKey(cmdToRepeat.modifiers, cmdToRepeat.keyCode)
                        else -> {}
                    }
                    delay(defaultDelay)
                }
            }
            
            "RAW" -> {
                // RAW <modifier> <keycode> - send raw HID values
                // Values can be decimal (e.g., 4) or hex with 0x prefix (e.g., 0x04)
                val parts = args.split(" ", limit = 2)
                if (parts.size < 2) {
                    throw IllegalArgumentException("RAW requires two arguments: modifier and keycode (e.g., RAW 0 4 or RAW 0x02 0x04)")
                }
                val modifier = parseNumber(parts[0])
                val keyCode = parseNumber(parts[1])
                lastCommand = Command.KeyPress(modifier, keyCode.toByte())
                keyboard.sendKey(modifier, keyCode.toByte())
            }
            
            else -> {
                // Unknown command - ignore or throw
                onLog?.invoke("Unknown command: $cmd")
            }
        }
    }
    
    private fun getKeyCodeForArg(arg: String): Byte {
        return when (arg.uppercase()) {
            "A" -> HidKeyCodes.KEY_A
            "B" -> HidKeyCodes.KEY_B
            "C" -> HidKeyCodes.KEY_C
            "D" -> HidKeyCodes.KEY_D
            "E" -> HidKeyCodes.KEY_E
            "F" -> HidKeyCodes.KEY_F
            "G" -> HidKeyCodes.KEY_G
            "H" -> HidKeyCodes.KEY_H
            "I" -> HidKeyCodes.KEY_I
            "J" -> HidKeyCodes.KEY_J
            "K" -> HidKeyCodes.KEY_K
            "L" -> HidKeyCodes.KEY_L
            "M" -> HidKeyCodes.KEY_M
            "N" -> HidKeyCodes.KEY_N
            "O" -> HidKeyCodes.KEY_O
            "P" -> HidKeyCodes.KEY_P
            "Q" -> HidKeyCodes.KEY_Q
            "R" -> HidKeyCodes.KEY_R
            "S" -> HidKeyCodes.KEY_S
            "T" -> HidKeyCodes.KEY_T
            "U" -> HidKeyCodes.KEY_U
            "V" -> HidKeyCodes.KEY_V
            "W" -> HidKeyCodes.KEY_W
            "X" -> HidKeyCodes.KEY_X
            "Y" -> HidKeyCodes.KEY_Y
            "Z" -> HidKeyCodes.KEY_Z
            "1" -> HidKeyCodes.KEY_1
            "2" -> HidKeyCodes.KEY_2
            "3" -> HidKeyCodes.KEY_3
            "4" -> HidKeyCodes.KEY_4
            "5" -> HidKeyCodes.KEY_5
            "6" -> HidKeyCodes.KEY_6
            "7" -> HidKeyCodes.KEY_7
            "8" -> HidKeyCodes.KEY_8
            "9" -> HidKeyCodes.KEY_9
            "0" -> HidKeyCodes.KEY_0
            "ENTER", "RETURN" -> HidKeyCodes.KEY_ENTER
            "ESCAPE", "ESC" -> HidKeyCodes.KEY_ESCAPE
            "BACKSPACE" -> HidKeyCodes.KEY_BACKSPACE
            "TAB" -> HidKeyCodes.KEY_TAB
            "SPACE" -> HidKeyCodes.KEY_SPACE
            "DELETE" -> HidKeyCodes.KEY_DELETE
            "INSERT" -> HidKeyCodes.KEY_INSERT
            "HOME" -> HidKeyCodes.KEY_HOME
            "END" -> HidKeyCodes.KEY_END
            "PAGEUP" -> HidKeyCodes.KEY_PAGE_UP
            "PAGEDOWN" -> HidKeyCodes.KEY_PAGE_DOWN
            "UP", "UPARROW" -> HidKeyCodes.KEY_UP_ARROW
            "DOWN", "DOWNARROW" -> HidKeyCodes.KEY_DOWN_ARROW
            "LEFT", "LEFTARROW" -> HidKeyCodes.KEY_LEFT_ARROW
            "RIGHT", "RIGHTARROW" -> HidKeyCodes.KEY_RIGHT_ARROW
            "PAUSE", "BREAK" -> HidKeyCodes.KEY_PAUSE
            "F1" -> HidKeyCodes.KEY_F1
            "F2" -> HidKeyCodes.KEY_F2
            "F3" -> HidKeyCodes.KEY_F3
            "F4" -> HidKeyCodes.KEY_F4
            "F5" -> HidKeyCodes.KEY_F5
            "F6" -> HidKeyCodes.KEY_F6
            "F7" -> HidKeyCodes.KEY_F7
            "F8" -> HidKeyCodes.KEY_F8
            "F9" -> HidKeyCodes.KEY_F9
            "F10" -> HidKeyCodes.KEY_F10
            "F11" -> HidKeyCodes.KEY_F11
            "F12" -> HidKeyCodes.KEY_F12
            else -> HidKeyCodes.KEY_NONE
        }
    }
    
    /**
     * Parse a number from string, supporting both decimal and hex (0x prefix)
     */
    private fun parseNumber(value: String): Int {
        val trimmed = value.trim()
        return if (trimmed.startsWith("0x", ignoreCase = true)) {
            trimmed.substring(2).toInt(16)
        } else {
            trimmed.toInt()
        }
    }
}
