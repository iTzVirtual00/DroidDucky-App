package me.itzvirtual.droidducky

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import me.itzvirtual.droidducky.ui.MainViewModel
import me.itzvirtual.droidducky.ui.ScriptEditorViewModel
import me.itzvirtual.droidducky.ui.navigation.Screen
import me.itzvirtual.droidducky.ui.screens.MainScreen
import me.itzvirtual.droidducky.ui.screens.ScriptEditorScreen
import me.itzvirtual.droidducky.ui.theme.DroidDuckyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidDuckyTheme {
                DroidDuckyApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun DroidDuckyApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            // Reload scripts every time this screen is displayed
            LaunchedEffect(Unit) {
                viewModel.loadScripts()
            }
            
            MainScreen(
                uiState = uiState,
                onRefreshDevice = { viewModel.refreshDeviceStatus() },
                onUpdateDevicePath = { viewModel.updateDevicePath(it) },
                onAddScript = { viewModel.addScript(it) },
                onDeleteScript = { viewModel.deleteScript(it) },
                onPlayScript = { script ->
                    Toast.makeText(context, "Executing: ${script.name}", Toast.LENGTH_SHORT).show()
                    viewModel.playScript(script) { success, error ->
                        if (success) {
                            Toast.makeText(context, "Completed: ${script.name}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, error ?: "Execution failed", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onStopScript = {
                    viewModel.stopExecution()
                    Toast.makeText(context, "Execution stopped", Toast.LENGTH_SHORT).show()
                },
                onEditScript = { script ->
                    navController.navigate(Screen.ScriptEditor.createRoute(script.id))
                }
            )
        }
        
        composable(
            route = Screen.ScriptEditor.route,
            arguments = listOf(navArgument("scriptId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scriptId = backStackEntry.arguments?.getString("scriptId") ?: return@composable
            val viewModel: ScriptEditorViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            LaunchedEffect(scriptId) {
                viewModel.loadScript(scriptId)
            }
            
            ScriptEditorScreen(
                uiState = uiState,
                onContentChange = { viewModel.updateContent(it) },
                onSaveAndExit = { 
                    viewModel.saveScript()
                    Toast.makeText(context, "Script saved", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}