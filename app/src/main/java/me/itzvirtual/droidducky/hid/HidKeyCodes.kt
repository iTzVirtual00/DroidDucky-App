package me.itzvirtual.droidducky.hid

/**
 * USB HID Keyboard scan codes and modifiers
 * Based on USB HID Usage Tables specification
 */
object HidKeyCodes {
    // Modifier keys (bit flags for byte 0 of HID report)
    const val MOD_NONE: Byte = 0x00
    const val MOD_LEFT_CTRL: Byte = 0x01
    const val MOD_LEFT_SHIFT: Byte = 0x02
    const val MOD_LEFT_ALT: Byte = 0x04
    const val MOD_LEFT_GUI: Byte = 0x08
    const val MOD_RIGHT_CTRL: Byte = 0x10
    const val MOD_RIGHT_SHIFT: Byte = 0x20
    const val MOD_RIGHT_ALT: Byte = 0x40
    const val MOD_RIGHT_GUI: Byte = 0x80.toByte()

    // Key codes (bytes 2-7 of HID report)
    const val KEY_NONE: Byte = 0x00
    const val KEY_A: Byte = 0x04
    const val KEY_B: Byte = 0x05
    const val KEY_C: Byte = 0x06
    const val KEY_D: Byte = 0x07
    const val KEY_E: Byte = 0x08
    const val KEY_F: Byte = 0x09
    const val KEY_G: Byte = 0x0A
    const val KEY_H: Byte = 0x0B
    const val KEY_I: Byte = 0x0C
    const val KEY_J: Byte = 0x0D
    const val KEY_K: Byte = 0x0E
    const val KEY_L: Byte = 0x0F
    const val KEY_M: Byte = 0x10
    const val KEY_N: Byte = 0x11
    const val KEY_O: Byte = 0x12
    const val KEY_P: Byte = 0x13
    const val KEY_Q: Byte = 0x14
    const val KEY_R: Byte = 0x15
    const val KEY_S: Byte = 0x16
    const val KEY_T: Byte = 0x17
    const val KEY_U: Byte = 0x18
    const val KEY_V: Byte = 0x19
    const val KEY_W: Byte = 0x1A
    const val KEY_X: Byte = 0x1B
    const val KEY_Y: Byte = 0x1C
    const val KEY_Z: Byte = 0x1D

    const val KEY_1: Byte = 0x1E
    const val KEY_2: Byte = 0x1F
    const val KEY_3: Byte = 0x20
    const val KEY_4: Byte = 0x21
    const val KEY_5: Byte = 0x22
    const val KEY_6: Byte = 0x23
    const val KEY_7: Byte = 0x24
    const val KEY_8: Byte = 0x25
    const val KEY_9: Byte = 0x26
    const val KEY_0: Byte = 0x27

    const val KEY_ENTER: Byte = 0x28
    const val KEY_ESCAPE: Byte = 0x29
    const val KEY_BACKSPACE: Byte = 0x2A
    const val KEY_TAB: Byte = 0x2B
    const val KEY_SPACE: Byte = 0x2C
    const val KEY_MINUS: Byte = 0x2D
    const val KEY_EQUAL: Byte = 0x2E
    const val KEY_LEFT_BRACKET: Byte = 0x2F
    const val KEY_RIGHT_BRACKET: Byte = 0x30
    const val KEY_BACKSLASH: Byte = 0x31
    const val KEY_SEMICOLON: Byte = 0x33
    const val KEY_QUOTE: Byte = 0x34
    const val KEY_BACKTICK: Byte = 0x35
    const val KEY_COMMA: Byte = 0x36
    const val KEY_PERIOD: Byte = 0x37
    const val KEY_SLASH: Byte = 0x38
    const val KEY_CAPS_LOCK: Byte = 0x39

    const val KEY_F1: Byte = 0x3A
    const val KEY_F2: Byte = 0x3B
    const val KEY_F3: Byte = 0x3C
    const val KEY_F4: Byte = 0x3D
    const val KEY_F5: Byte = 0x3E
    const val KEY_F6: Byte = 0x3F
    const val KEY_F7: Byte = 0x40
    const val KEY_F8: Byte = 0x41
    const val KEY_F9: Byte = 0x42
    const val KEY_F10: Byte = 0x43
    const val KEY_F11: Byte = 0x44
    const val KEY_F12: Byte = 0x45

    const val KEY_PRINT_SCREEN: Byte = 0x46
    const val KEY_SCROLL_LOCK: Byte = 0x47
    const val KEY_PAUSE: Byte = 0x48
    const val KEY_INSERT: Byte = 0x49
    const val KEY_HOME: Byte = 0x4A
    const val KEY_PAGE_UP: Byte = 0x4B
    const val KEY_DELETE: Byte = 0x4C
    const val KEY_END: Byte = 0x4D
    const val KEY_PAGE_DOWN: Byte = 0x4E
    const val KEY_RIGHT_ARROW: Byte = 0x4F
    const val KEY_LEFT_ARROW: Byte = 0x50
    const val KEY_DOWN_ARROW: Byte = 0x51
    const val KEY_UP_ARROW: Byte = 0x52
    const val KEY_NUM_LOCK: Byte = 0x53
    const val KEY_MENU: Byte = 0x65

    // Keypad keys
    const val KEY_KP_SLASH: Byte = 0x54
    const val KEY_KP_ASTERISK: Byte = 0x55
    const val KEY_KP_MINUS: Byte = 0x56
    const val KEY_KP_PLUS: Byte = 0x57
    const val KEY_KP_ENTER: Byte = 0x58

    /**
     * Get key code and modifier for a character
     * Returns Pair(modifier, keyCode)
     */
    fun getKeyForChar(char: Char): Pair<Byte, Byte> {
        return when (char) {
            // Lowercase letters
            'a' -> Pair(MOD_NONE, KEY_A)
            'b' -> Pair(MOD_NONE, KEY_B)
            'c' -> Pair(MOD_NONE, KEY_C)
            'd' -> Pair(MOD_NONE, KEY_D)
            'e' -> Pair(MOD_NONE, KEY_E)
            'f' -> Pair(MOD_NONE, KEY_F)
            'g' -> Pair(MOD_NONE, KEY_G)
            'h' -> Pair(MOD_NONE, KEY_H)
            'i' -> Pair(MOD_NONE, KEY_I)
            'j' -> Pair(MOD_NONE, KEY_J)
            'k' -> Pair(MOD_NONE, KEY_K)
            'l' -> Pair(MOD_NONE, KEY_L)
            'm' -> Pair(MOD_NONE, KEY_M)
            'n' -> Pair(MOD_NONE, KEY_N)
            'o' -> Pair(MOD_NONE, KEY_O)
            'p' -> Pair(MOD_NONE, KEY_P)
            'q' -> Pair(MOD_NONE, KEY_Q)
            'r' -> Pair(MOD_NONE, KEY_R)
            's' -> Pair(MOD_NONE, KEY_S)
            't' -> Pair(MOD_NONE, KEY_T)
            'u' -> Pair(MOD_NONE, KEY_U)
            'v' -> Pair(MOD_NONE, KEY_V)
            'w' -> Pair(MOD_NONE, KEY_W)
            'x' -> Pair(MOD_NONE, KEY_X)
            'y' -> Pair(MOD_NONE, KEY_Y)
            'z' -> Pair(MOD_NONE, KEY_Z)

            // Uppercase letters (with shift)
            'A' -> Pair(MOD_LEFT_SHIFT, KEY_A)
            'B' -> Pair(MOD_LEFT_SHIFT, KEY_B)
            'C' -> Pair(MOD_LEFT_SHIFT, KEY_C)
            'D' -> Pair(MOD_LEFT_SHIFT, KEY_D)
            'E' -> Pair(MOD_LEFT_SHIFT, KEY_E)
            'F' -> Pair(MOD_LEFT_SHIFT, KEY_F)
            'G' -> Pair(MOD_LEFT_SHIFT, KEY_G)
            'H' -> Pair(MOD_LEFT_SHIFT, KEY_H)
            'I' -> Pair(MOD_LEFT_SHIFT, KEY_I)
            'J' -> Pair(MOD_LEFT_SHIFT, KEY_J)
            'K' -> Pair(MOD_LEFT_SHIFT, KEY_K)
            'L' -> Pair(MOD_LEFT_SHIFT, KEY_L)
            'M' -> Pair(MOD_LEFT_SHIFT, KEY_M)
            'N' -> Pair(MOD_LEFT_SHIFT, KEY_N)
            'O' -> Pair(MOD_LEFT_SHIFT, KEY_O)
            'P' -> Pair(MOD_LEFT_SHIFT, KEY_P)
            'Q' -> Pair(MOD_LEFT_SHIFT, KEY_Q)
            'R' -> Pair(MOD_LEFT_SHIFT, KEY_R)
            'S' -> Pair(MOD_LEFT_SHIFT, KEY_S)
            'T' -> Pair(MOD_LEFT_SHIFT, KEY_T)
            'U' -> Pair(MOD_LEFT_SHIFT, KEY_U)
            'V' -> Pair(MOD_LEFT_SHIFT, KEY_V)
            'W' -> Pair(MOD_LEFT_SHIFT, KEY_W)
            'X' -> Pair(MOD_LEFT_SHIFT, KEY_X)
            'Y' -> Pair(MOD_LEFT_SHIFT, KEY_Y)
            'Z' -> Pair(MOD_LEFT_SHIFT, KEY_Z)

            // Numbers
            '1' -> Pair(MOD_NONE, KEY_1)
            '2' -> Pair(MOD_NONE, KEY_2)
            '3' -> Pair(MOD_NONE, KEY_3)
            '4' -> Pair(MOD_NONE, KEY_4)
            '5' -> Pair(MOD_NONE, KEY_5)
            '6' -> Pair(MOD_NONE, KEY_6)
            '7' -> Pair(MOD_NONE, KEY_7)
            '8' -> Pair(MOD_NONE, KEY_8)
            '9' -> Pair(MOD_NONE, KEY_9)
            '0' -> Pair(MOD_NONE, KEY_0)

            // Symbols (shifted numbers)
            '!' -> Pair(MOD_LEFT_SHIFT, KEY_1)
            '@' -> Pair(MOD_LEFT_SHIFT, KEY_2)
            '#' -> Pair(MOD_LEFT_SHIFT, KEY_3)
            '$' -> Pair(MOD_LEFT_SHIFT, KEY_4)
            '%' -> Pair(MOD_LEFT_SHIFT, KEY_5)
            '^' -> Pair(MOD_LEFT_SHIFT, KEY_6)
            '&' -> Pair(MOD_LEFT_SHIFT, KEY_7)
            '*' -> Pair(MOD_LEFT_SHIFT, KEY_8)
            '(' -> Pair(MOD_LEFT_SHIFT, KEY_9)
            ')' -> Pair(MOD_LEFT_SHIFT, KEY_0)

            // Special characters
            ' ' -> Pair(MOD_NONE, KEY_SPACE)
            '\t' -> Pair(MOD_NONE, KEY_TAB)
            '\n' -> Pair(MOD_NONE, KEY_ENTER)

            // Punctuation
            '-' -> Pair(MOD_NONE, KEY_MINUS)
            '_' -> Pair(MOD_LEFT_SHIFT, KEY_MINUS)
            '=' -> Pair(MOD_NONE, KEY_EQUAL)
            '+' -> Pair(MOD_LEFT_SHIFT, KEY_EQUAL)
            '[' -> Pair(MOD_NONE, KEY_LEFT_BRACKET)
            '{' -> Pair(MOD_LEFT_SHIFT, KEY_LEFT_BRACKET)
            ']' -> Pair(MOD_NONE, KEY_RIGHT_BRACKET)
            '}' -> Pair(MOD_LEFT_SHIFT, KEY_RIGHT_BRACKET)
            '\\' -> Pair(MOD_NONE, KEY_BACKSLASH)
            '|' -> Pair(MOD_LEFT_SHIFT, KEY_BACKSLASH)
            ';' -> Pair(MOD_NONE, KEY_SEMICOLON)
            ':' -> Pair(MOD_LEFT_SHIFT, KEY_SEMICOLON)
            '\'' -> Pair(MOD_NONE, KEY_QUOTE)
            '"' -> Pair(MOD_LEFT_SHIFT, KEY_QUOTE)
            '`' -> Pair(MOD_NONE, KEY_BACKTICK)
            '~' -> Pair(MOD_LEFT_SHIFT, KEY_BACKTICK)
            ',' -> Pair(MOD_NONE, KEY_COMMA)
            '<' -> Pair(MOD_LEFT_SHIFT, KEY_COMMA)
            '.' -> Pair(MOD_NONE, KEY_PERIOD)
            '>' -> Pair(MOD_LEFT_SHIFT, KEY_PERIOD)
            '/' -> Pair(MOD_NONE, KEY_SLASH)
            '?' -> Pair(MOD_LEFT_SHIFT, KEY_SLASH)

            else -> Pair(MOD_NONE, KEY_NONE)
        }
    }
}
