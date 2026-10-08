package com.rogbandroid.rogermote.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.rogbandroid.rogermote.viewmodel.RemoteUiState

class SavedTvDevicesTest {
    private val first = TvDevice("192.168.1.10", "Living room \"TV\"", "Q80R", "uuid:first", "Family Room TV")
    private val second = TvDevice("192.168.1.11", "Bedroom TV")

    @Test
    fun roundTripPreservesDisplayDetailsAndOptionalFields() {
        val devices = listOf(first, second)
        assertEquals(devices, SavedTvDevices.decode(SavedTvDevices.encode(devices)))
    }

    @Test
    fun invalidStorageDoesNotCrashOrDiscardValidEntries() {
        assertTrue(SavedTvDevices.decode("broken json").isEmpty())
        assertTrue(SavedTvDevices.decode(null).isEmpty())
        assertEquals(listOf(second), SavedTvDevices.decode(
            "[null,{},${SavedTvDevices.encode(listOf(second)).removePrefix("[").removeSuffix("]")}]",
        ))
    }

    @Test
    fun rescanUpdatesAddressByIdentityAndKeepsOfflineDevices() {
        val updated = first.copy(ipAddress = "192.168.1.20", friendlyName = "Renamed TV", alias = "")
        assertEquals(listOf(updated.copy(alias = first.alias), second), SavedTvDevices.merge(listOf(first, second), listOf(updated)))
        assertEquals(listOf(first, second), SavedTvDevices.merge(listOf(first, second), emptyList()))
    }

    @Test
    fun duplicateResultsAndMissingMetadataPreserveSavedModel() {
        val found = first.copy(modelName = null, uniqueId = null)
        assertEquals(listOf(first, second), SavedTvDevices.merge(listOf(first), listOf(found, second, second)))
    }

    @Test
    fun deletionSurvivesSerialization() {
        val remaining = listOf(first, second).filterNot { it == first }
        assertEquals(listOf(second), SavedTvDevices.decode(SavedTvDevices.encode(remaining)))
        assertTrue(SavedTvDevices.decode(SavedTvDevices.encode(emptyList())).isEmpty())
    }

    @Test
    fun olderEntriesWithoutAnAliasStillLoad() {
        val json = """[{"ipAddress":"192.168.1.11","friendlyName":"Bedroom TV"}]"""
        assertEquals(listOf(second), SavedTvDevices.decode(json))
    }

    @Test
    fun remoteAliasFollowsSelectedTvAndCanBeCleared() {
        val state = RemoteUiState(ipAddress = first.ipAddress, discoveredDevices = listOf(first, second))
        assertEquals("Family Room TV", state.selectedTvAlias)
        assertEquals("", state.copy(ipAddress = second.ipAddress).selectedTvAlias)
        assertEquals("", state.copy(discoveredDevices = listOf(first.copy(alias = ""))).selectedTvAlias)
    }
}
