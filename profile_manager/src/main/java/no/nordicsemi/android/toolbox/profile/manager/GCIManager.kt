package no.nordicsemi.android.toolbox.profile.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.manager.repository.GCIRepository
import no.nordicsemi.android.toolbox.profile.parser.gci.GCIDataParser
import no.nordicsemi.android.toolbox.profile.parser.gci.GCIEvent
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.ble.core.util.fromShortUuid
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
private val AUTO_START_CHARACTERISTIC_UUID = UUID.fromString("3861a947-7b94-495b-ae3d-c2d669d9f168").toKotlinUuid()

@OptIn(ExperimentalUuidApi::class)
private val AUTO_EVENT_CHARACTERISTIC_UUID = UUID.fromString("c045a031-c506-4756-8bdd-63d55ef3eced").toKotlinUuid()

internal class GCIManager: ServiceManager {
    override val profile: Profile = Profile.GCI

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        withContext(scope.coroutineContext) {
            startChar = remoteService.characteristics.firstOrNull {
                it.uuid == AUTO_START_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("Automation Start characteristic not found")
            eventChar = remoteService.characteristics.firstOrNull {
                it.uuid == AUTO_EVENT_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("Automation Event characteristic not found")

            // Get initial event
            refreshGloveEvent(deviceId);

            eventChar.subscribe().mapNotNull{
                GCIDataParser.parseEvent(it)
            }.onEach {
                GCIRepository.updateGloveEvent(deviceId, it)
                if (it is GCIEvent.ActivityStarted) {
                    GCIRepository.resetRepCount(deviceId)
                } else if (it is GCIEvent.RepCompleted || it is GCIEvent.ActivityCompleted)  {
                    GCIRepository.incrementRepCount(deviceId)
                }
            }.catch { it.printStackTrace() }
            .onCompletion { GCIRepository.clear(deviceId) }
            .launchIn(scope)

        }
    }

    companion object {
        private lateinit var startChar: RemoteCharacteristic
        private lateinit var eventChar: RemoteCharacteristic

        private suspend fun readCharacteristic(deviceId: String, characteristic: RemoteCharacteristic, actions: (ByteArray) -> Unit) {
            characteristic.let { c ->
                // If the characteristic supports READ, read the initial value
                if (c.properties.contains(CharacteristicProperty.READ)) {
                    try {
                        actions(c.read())
                    } catch (e: Exception) {
                        Timber.e("Error reading OTS characteristic: ${e.message}")
                    }
                }
            }
        }

        suspend fun requestStart(deviceId: String, exerciseId: Int) {
            val data = byteArrayOf(exerciseId.toByte())

            try {
                if (::startChar.isInitialized) {
                    startChar.write(data, WriteType.WITH_RESPONSE)
                }
            } catch (e: Exception) {
                Timber.e("Error writing to OLCP characteristic: ${e.message}")
            }
        }

        suspend fun refreshGloveEvent(deviceId: String) {
            readCharacteristic(deviceId, eventChar) { data ->
                data.let { GCIDataParser.parseEvent(it) }
                    ?.let { GCIRepository.updateGloveEvent(deviceId, it) }
            }
        }
    }
}
