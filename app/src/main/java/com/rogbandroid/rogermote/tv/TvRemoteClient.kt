package com.rogbandroid.rogermote.tv

import kotlinx.coroutines.flow.StateFlow
import com.rogbandroid.rogermote.data.TvApplication

interface TvRemoteClient {
    val connectionState: StateFlow<ConnectionState>
    val installedApplications: StateFlow<List<TvApplication>>

    fun connect(ipAddress: String)

    fun disconnect()

    fun sendCommand(command: RemoteCommand)

    fun refreshApplications()

    fun launchApplication(application: TvApplication)
}
