package no.nordicsemi.android.toolbox.profile.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.manager.repository.BatteryRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.OTSRepository
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSDataParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

private val FEATURES_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002abd-0000-1000-8000-00805f9b34fb")
private val OBJECT_NAME_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002abe-0000-1000-8000-00805f9b34fb")
private val OBJECT_TYPE_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002abf-0000-1000-8000-00805f9b34fb")
private val OBJECT_SIZE_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002ac0-0000-1000-8000-00805f9b34fb")
private val OBJECT_ID_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002ac3-0000-1000-8000-00805f9b34fb")
private val OBJECT_PROPERTIES_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00002ac4-0000-1000-8000-00805f9b34fb")



internal class OTSManager : ServiceManager {
    override val profile: Profile = Profile.OTS

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {

        val featureChar = remoteService.characteristics.firstOrNull {
            it.uuid == FEATURES_CHARACTERISTIC_UUID.toKotlinUuid()
        } ?: throw IllegalStateException("OTS Feature characteristic not found")

        featureChar.let { characteristic ->
            // If the characteristic supports READ, read the initial value
            if (CharacteristicProperty.READ in characteristic.properties) {
                try {
                    characteristic.read()
                        .let {
                            OTSDataParser.parseFeatures(it)
                        }
                        ?.let { otsFeatures ->
                            OTSRepository.updateFeatures(deviceId, otsFeatures)
                        }

                } catch (e: Exception) {
                    Timber.e("Error reading OTS Features: ${e.message}")
                }
            }
        }
    }
}
