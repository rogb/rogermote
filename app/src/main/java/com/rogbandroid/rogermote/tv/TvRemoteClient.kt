package com.rogbandroid.rogermote.tv

import kotlinx.coroutines.flow.StateFlow

interface TvRemoteClient {
    val connectionState: StateFlow<ConnectionState>

    fun connect(ipAddress: String)

    fun disconnect()

    fun sendCommand(command: RemoteCommand)
}
