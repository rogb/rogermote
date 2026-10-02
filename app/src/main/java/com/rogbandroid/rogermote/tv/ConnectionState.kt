package com.rogbandroid.rogermote.tv

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data object WaitingForTvApproval : ConnectionState
    data object Connected : ConnectionState
    data object AuthorizationRejected : ConnectionState
    data class ConnectionFailed(val message: String) : ConnectionState
}

fun ConnectionState.displayText(): String = when (this) {
    ConnectionState.Disconnected -> "Disconnected"
    ConnectionState.Connecting -> "Connecting"
    ConnectionState.WaitingForTvApproval -> "Waiting for approval on TV"
    ConnectionState.Connected -> "Connected"
    ConnectionState.AuthorizationRejected -> "Authorization rejected"
    is ConnectionState.ConnectionFailed -> message
}
