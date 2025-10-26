package no.nordicsemi.android.toolbox.profile.manager.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObject
import no.nordicsemi.android.toolbox.profile.data.OTSServiceData

object OTSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<OTSServiceData>>()

    fun getData(deviceId: String): Flow<OTSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(OTSServiceData()) }
    }

    fun updateFeatures(deviceId: String, features: OTSFeatures) {
        _dataMap[deviceId]?.update { it.copy(otsFeatures = features) }
    }

    fun updateObject(deviceId: String, obj: OTSObject) {
        _dataMap[deviceId]?.update { it.copy(otsObject = obj) }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }
}
