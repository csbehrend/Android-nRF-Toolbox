package no.nordicsemi.android.toolbox.profile.manager.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.data.OTSServiceData
import no.nordicsemi.android.toolbox.profile.manager.GCIManager
import no.nordicsemi.android.toolbox.profile.manager.OTSManager
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObjSize
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObject

object GCIRepository {
    suspend fun startExercise(deviceId: String, exerciseId: Int) {
        GCIManager.requestStart(deviceId, exerciseId)
    }
}
