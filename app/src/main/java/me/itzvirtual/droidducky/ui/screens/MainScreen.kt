package me.itzvirtual.droidducky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.itzvirtual.droidducky.data.Script
import me.itzvirtual.droidducky.ui.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onRefreshDevice: () -> Unit,
    onUpdateDevicePath: (String) -> Unit,
    onAddScript: (String) -> Unit,
    onDeleteScript: (String) -> Unit,
    onPlayScript: (Script) -> Unit,
    onStopScript: () -> Unit,
    onEditScript: (Script) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDevicePathDialog by remember { mutableStateOf(false) }
    var showAddScriptDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Script?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DroidDucky") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddScriptDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Script")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Device Status Row
            DeviceStatusRow(
                devicePath = uiState.devicePath,
                deviceExists = uiState.deviceExists,
                deviceError = uiState.deviceError,
                onRefresh = onRefreshDevice,
                onClick = { showDevicePathDialog = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Scripts Section
            Text(
                text = "Scripts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (uiState.scripts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scripts yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.scripts, key = { it.id }) { script ->
                        ScriptItem(
                            script = script,
                            isExecuting = uiState.isExecuting,
                            isThisScriptExecuting = uiState.executingScriptId == script.id,
                            onPlay = { onPlayScript(script) },
                            onStop = onStopScript,
                            onDelete = { showDeleteConfirmDialog = script },
                            onClick = { onEditScript(script) }
                        )
                    }
                }
            }
        }
    }
    
    // Device Path Dialog
    if (showDevicePathDialog) {
        DevicePathDialog(
            currentPath = uiState.devicePath,
            onDismiss = { showDevicePathDialog = false },
            onConfirm = { newPath ->
                onUpdateDevicePath(newPath)
                showDevicePathDialog = false
            }
        )
    }
    
    // Add Script Dialog
    if (showAddScriptDialog) {
        AddScriptDialog(
            onDismiss = { showAddScriptDialog = false },
            onConfirm = { name ->
                onAddScript(name)
                showAddScriptDialog = false
            }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { script ->
        DeleteConfirmDialog(
            scriptName = script.name,
            onDismiss = { showDeleteConfirmDialog = null },
            onConfirm = {
                onDeleteScript(script.id)
                showDeleteConfirmDialog = null
            }
        )
    }
}

@Composable
fun DeviceStatusRow(
    devicePath: String,
    deviceExists: Boolean,
    deviceError: String?,
    onRefresh: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (deviceExists) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    
    val contentColor = if (deviceExists) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "HID Device",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = devicePath,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
                if (deviceError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deviceError,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (deviceExists) contentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error
                    )
                } else if (deviceExists) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Device ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
            
            IconButton(
                onClick = onRefresh
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = contentColor
                )
            }
        }
    }
}

@Composable
fun ScriptItem(
    script: Script,
    isExecuting: Boolean,
    isThisScriptExecuting: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isExecuting, onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = script.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            if (isThisScriptExecuting) {
                // Show stop button for the executing script
                IconButton(onClick = onStop) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                // Show play button
                IconButton(
                    onClick = onPlay,
                    enabled = !isExecuting
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isExecuting) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            IconButton(
                onClick = onDelete,
                enabled = !isExecuting
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (isExecuting) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Composable
fun DevicePathDialog(
    currentPath: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var path by remember { mutableStateOf(currentPath) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Device Path") },
        text = {
            OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                label = { Text("Path") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(path) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddScriptDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Script") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Script Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    scriptName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Script") },
        text = { 
            Text("Are you sure you want to delete \"$scriptName\"? This action cannot be undone.") 
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
