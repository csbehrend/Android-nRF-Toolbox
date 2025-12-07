package no.nordicsemi.android.toolbox.profile.manager.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.data.GCIServiceData
import no.nordicsemi.android.toolbox.profile.data.OTSServiceData
import no.nordicsemi.android.toolbox.profile.manager.GCIManager
import no.nordicsemi.android.toolbox.profile.manager.OTSManager
import no.nordicsemi.android.toolbox.profile.parser.gci.GCIEvent
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObjSize
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObject

object GCIRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<GCIServiceData>>()

    fun getData(deviceId: String): Flow<GCIServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(GCIServiceData()) }
    }
    suspend fun startExercise(deviceId: String, exerciseId: Int) {
        GCIManager.requestStart(deviceId, exerciseId)
    }

    fun updateGloveEvent(deviceId: String, event: GCIEvent) {
        _dataMap[deviceId]?.update { it.copy(gciEvent = event) }
    }

    fun resetRepCount(deviceId: String) {
        _dataMap[deviceId]?.update { it.copy(repCount = 0) }
    }

    fun incrementRepCount(deviceId: String) {
        _dataMap[deviceId]?.update { it.copy(repCount = it.repCount + 1) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }
}
