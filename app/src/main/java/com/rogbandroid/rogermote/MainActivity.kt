package com.rogbandroid.rogermote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rogbandroid.rogermote.ui.RemoteScreen
import com.rogbandroid.rogermote.ui.SetupScreen
import com.rogbandroid.rogermote.ui.theme.RogermoteTheme
import com.rogbandroid.rogermote.tv.RemoteCommand
import com.rogbandroid.rogermote.viewmodel.RemoteUiState
import com.rogbandroid.rogermote.viewmodel.RemotePage
import com.rogbandroid.rogermote.viewmodel.RemoteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RogermoteTheme {
                RogermoteApp()
            }
        }
    }
}

@Composable
fun RogermoteApp(viewModel: RemoteViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onAppForeground()
                Lifecycle.Event.ON_STOP -> viewModel.onAppBackground()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (uiState.page) {
        RemotePage.Setup -> SetupScreen(
            uiState = uiState,
            onIpAddressChange = viewModel::updateIpAddress,
            onConnect = viewModel::connect,
            onDisconnect = viewModel::disconnect,
            onForgetSavedTv = viewModel::forgetSavedTv,
            onScanForTvs = viewModel::scanForTvs,
            onSelectTv = viewModel::selectTv,
            onHapticsEnabledChange = viewModel::setHapticsEnabled,
            onOpenRemote = viewModel::openRemote,
        )
        RemotePage.Remote -> RemoteScreen(
            uiState = uiState,
            onCommand = viewModel::sendCommand,
            onDisconnect = viewModel::disconnect,
            onOpenSetup = viewModel::openSetup,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RogermoteAppPreview() {
    RogermoteTheme {
        SetupScreen(
            uiState = RemoteUiState(),
            onIpAddressChange = {},
            onConnect = {},
            onDisconnect = {},
            onForgetSavedTv = {},
            onScanForTvs = {},
            onSelectTv = {},
            onHapticsEnabledChange = {},
            onOpenRemote = {},
        )
    }
}
