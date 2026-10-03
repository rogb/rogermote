package com.rogbandroid.rogermote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.rogbandroid.rogermote.data.TvDevice
import com.rogbandroid.rogermote.tv.ConnectionState
import com.rogbandroid.rogermote.tv.displayText
import com.rogbandroid.rogermote.viewmodel.RemoteUiState

@Composable
fun SetupScreen(
    uiState: RemoteUiState,
    onIpAddressChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onForgetSavedTv: () -> Unit,
    onScanForTvs: () -> Unit,
    onSelectTv: (TvDevice) -> Unit,
    onHapticsEnabledChange: (Boolean) -> Unit,
    onOpenRemote: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onOpenRemote, enabled = uiState.hasSavedConfiguration) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Remote control")
                }
                Text(
                    text = "TV Setup",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Default.Search, contentDescription = "Scanning")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Samsung TV Remote",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("TV CONNECTION", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.ipAddress,
                onValueChange = onIpAddressChange,
                enabled = !uiState.isConnectionInProgress && !uiState.isConnected,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("TV IP") },
                placeholder = { Text("192.168.x.x") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onScanForTvs,
                enabled = !uiState.isDiscovering,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(if (uiState.isDiscovering) "Scanning..." else "Scan for TVs")
            }

            uiState.discoveryError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            if (uiState.discoveredDevices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("FOUND TVs", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                uiState.discoveredDevices.forEach { device ->
                    OutlinedButton(
                        onClick = { onSelectTv(device) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            buildString {
                                append(device.friendlyName)
                                append("\nModel: ")
                                append(device.modelName?.takeIf { it.isNotBlank() } ?: "Unavailable")
                                append("\n")
                                append(device.ipAddress)
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onConnect,
                    enabled = uiState.ipAddress.isNotBlank() &&
                        !uiState.isConnectionInProgress && !uiState.isConnected,
                    modifier = Modifier.weight(1f),
                ) { Text("Connect") }
                OutlinedButton(
                    onClick = onDisconnect,
                    enabled = uiState.connectionState != ConnectionState.Disconnected,
                    modifier = Modifier.weight(1f),
                ) { Text("Disconnect") }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Status: ${uiState.connectionState.displayText()}",
                color = if (uiState.connectionState is ConnectionState.ConnectionFailed ||
                    uiState.connectionState == ConnectionState.AuthorizationRejected
                ) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            if (uiState.hasSavedConfiguration || uiState.isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onOpenRemote, modifier = Modifier.fillMaxWidth()) {
                    Text("Open remote control")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Haptic feedback", style = MaterialTheme.typography.titleMedium)
                    Text("Vibrate briefly when a remote control is pressed.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = uiState.hapticsEnabled,
                    onCheckedChange = onHapticsEnabledChange,
                )
            }

            OutlinedButton(
                onClick = onForgetSavedTv,
                enabled = uiState.hasSavedConfiguration,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Forget saved TV") }
        }
    }
}
