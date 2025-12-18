package com.warriortech.resb.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.warriortech.resb.network.SessionManager
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

@Composable
fun BluetoothPrinterScreen(
    sessionManager: SessionManager,
    navController: NavHostController
) {
    val context = LocalContext.current
    val adapter = BluetoothAdapter.getDefaultAdapter()
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var logMessage by remember { mutableStateOf("") }

    // Permission launcher for Android 12+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { !it }) {
            logMessage = "Permission denied. Cannot access Bluetooth."
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            val devices = adapter?.bondedDevices?.toList() ?: emptyList()
            pairedDevices = devices
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Paired Bluetooth Devices", style = MaterialTheme.typography.titleLarge)

        if (pairedDevices.isEmpty()) {
            Text("No paired devices found.", color = MaterialTheme.colorScheme.error)
        } else {
            pairedDevices.forEach { device ->
                Button(
                    onClick = { selectedDevice = device },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(device.name ?: "Unknown Device")
                }
            }
        }

        selectedDevice?.let { device ->
            Text("Selected: ${device.name}", style = MaterialTheme.typography.bodyLarge)

            Button(
                onClick = {
                    sessionManager.saveBluetoothPrinter(device)
                    navController.navigate("printer_setting")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Print Test Receipt")
            }
        }

        if (logMessage.isNotEmpty()) {
            Text("Log: $logMessage", color = MaterialTheme.colorScheme.primary)
        }
    }
}