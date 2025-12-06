package no.nordicsemi.android.toolbox.profile.manager.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObject
import no.nordicsemi.android.toolbox.profile.data.OTSServiceData
import no.nordicsemi.android.toolbox.profile.manager.OTSManager
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObjSize

object OTSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<OTSServiceData>>()

    fun getData(deviceId: String): Flow<OTSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(OTSServiceData()) }
    }

    fun updateFeatures(deviceId: String, features: OTSFeatures) {
        _dataMap[deviceId]?.update { it.copy(otsFeatures = features) }
    }

    fun updateObjectName(deviceId: String, name: String) {
        _dataMap[deviceId]?.update { it.copy(otsObject = it.otsObject.copy(name = name)) }
    }

    fun updateObjectSize(deviceId: String, size: OTSObjSize) {
        _dataMap[deviceId]?.update { it.copy(otsObject = it.otsObject.copy(size = size)) }
    }

    fun updateObject(deviceId: String, obj: OTSObject) {
        _dataMap[deviceId]?.update { it.copy(otsObject = obj) }
    }

    suspend fun requestOLCPOperation(deviceId: String, operation: OLCPOperation) {
        OTSManager.requestOLCPOperation(deviceId, operation)
    }

    fun onOLCPResponse(deviceId: String, response: OLCPResponse) {
        _dataMap[deviceId]?.update { it.copy(olcpResponse = response) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }
}
