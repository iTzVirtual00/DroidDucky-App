package me.itzvirtual.droidducky.hid

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * HID Keyboard device handler
 * Writes keyboard HID reports to /dev/hidgX
 */
class HidKeyboard(private val devicePath: String) {
    
    companion object {
        private const val REPORT_SIZE = 8
        private const val KEY_PRESS_DELAY_MS = 10L
    }
    
    /**
     * Check if the HID device exists and is accessible
     */
    fun isDeviceAvailable(): Boolean {
        val file = File(devicePath)
        return file.exists() && file.canWrite()
    }
    
    /**
     * Send a single key press with optional modifiers
     */
    suspend fun sendKey(modifier: Byte, keyCode: Byte) {
        withContext(Dispatchers.IO) {
            // Press key
            writeReport(modifier, keyCode)
            delay(KEY_PRESS_DELAY_MS)
            // Release key
            writeReport(0, 0)
            delay(KEY_PRESS_DELAY_MS)
        }
    }
    
    /**
     * Send multiple modifiers with a key
     */
    suspend fun sendKey(modifiers: Int, keyCode: Byte) {
        withContext(Dispatchers.IO) {
            writeReport(modifiers.toByte(), keyCode)
            delay(KEY_PRESS_DELAY_MS)
            writeReport(0, 0)
            delay(KEY_PRESS_DELAY_MS)
        }
    }
    
    /**
     * Type a string character by character
     */
    suspend fun typeString(text: String) {
        for (char in text) {
            val (modifier, keyCode) = HidKeyCodes.getKeyForChar(char)
            if (keyCode != HidKeyCodes.KEY_NONE) {
                sendKey(modifier, keyCode)
            }
        }
    }
    
    /**
     * Write raw HID report to device
     * Report format: [modifier, reserved, key1, key2, key3, key4, key5, key6]
     */
    private fun writeReport(modifier: Byte, keyCode: Byte) {
        val report = ByteArray(REPORT_SIZE).apply {
            this[0] = modifier
            this[1] = 0 // Reserved
            this[2] = keyCode
            // Bytes 3-7 are additional keys (set to 0)
        }
        
        try {
            FileOutputStream(devicePath).use { fos ->
                fos.write(report)
                fos.flush()
            }
        } catch (e: Exception) {
            throw HidException("Failed to write to HID device: ${e.message}", e)
        }
    }
    
    /**
     * Send key release (all keys up)
     */
    suspend fun releaseAll() {
        withContext(Dispatchers.IO) {
            writeReport(0, 0)
        }
    }
}

class HidException(message: String, cause: Throwable? = null) : Exception(message, cause)
