package me.itzvirtual.droidducky.ui.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object ScriptEditor : Screen("script_editor/{scriptId}") {
        fun createRoute(scriptId: String) = "script_editor/$scriptId"
    }
}
