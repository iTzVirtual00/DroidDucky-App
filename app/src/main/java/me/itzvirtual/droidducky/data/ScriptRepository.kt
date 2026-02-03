package me.itzvirtual.droidducky.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class ScriptRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    fun getDevicePath(): String {
        return prefs.getString(KEY_DEVICE_PATH, DEFAULT_DEVICE_PATH) ?: DEFAULT_DEVICE_PATH
    }
    
    fun setDevicePath(path: String) {
        prefs.edit().putString(KEY_DEVICE_PATH, path).apply()
    }
    
    fun getScripts(): List<Script> {
        val json = prefs.getString(KEY_SCRIPTS, "[]") ?: "[]"
        val scripts = mutableListOf<Script>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                scripts.add(
                    Script(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        content = obj.optString("content", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return scripts
    }
    
    fun saveScripts(scripts: List<Script>) {
        val jsonArray = JSONArray()
        scripts.forEach { script ->
            val obj = JSONObject().apply {
                put("id", script.id)
                put("name", script.name)
                put("content", script.content)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SCRIPTS, jsonArray.toString()).apply()
    }
    
    fun addScript(script: Script) {
        val scripts = getScripts().toMutableList()
        scripts.add(script)
        saveScripts(scripts)
    }
    
    fun updateScript(script: Script) {
        val scripts = getScripts().toMutableList()
        val index = scripts.indexOfFirst { it.id == script.id }
        if (index != -1) {
            scripts[index] = script
            saveScripts(scripts)
        }
    }
    
    fun deleteScript(scriptId: String) {
        val scripts = getScripts().toMutableList()
        scripts.removeAll { it.id == scriptId }
        saveScripts(scripts)
    }
    
    fun getScript(scriptId: String): Script? {
        return getScripts().find { it.id == scriptId }
    }
    
    companion object {
        private const val PREFS_NAME = "droidducky_prefs"
        private const val KEY_DEVICE_PATH = "device_path"
        private const val KEY_SCRIPTS = "scripts"
        private const val DEFAULT_DEVICE_PATH = "/dev/hidg0"
    }
}
