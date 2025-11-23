package no.nordicsemi.android.toolbox.profile.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.manager.repository.BatteryRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.DFSRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.OTSRepository
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.ddf.DDFDataParser
import no.nordicsemi.android.toolbox.profile.parser.directionFinder.distance.DistanceMode
import no.nordicsemi.android.toolbox.profile.parser.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSDataParser
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.WriteType
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

private val OTS_COC_PSM: Int = 0x0025

internal class OTSManager : ServiceManager {
    override val profile: Profile = Profile.OTS

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        withContext(scope.coroutineContext) {
            peripheral = remoteService.owner
            featureCharacteristic = remoteService.characteristics.firstOrNull {
                it.uuid == FEATURES_CHARACTERISTIC_UUID.toKotlinUuid()
            } ?: throw IllegalStateException("OTS Feature characteristic not found")
            /*
            if (featureChar.properties.contains(CharacteristicProperty.READ))

            val objNameChar = remoteService.characteristics.firstOrNull {
                it.uuid == OBJECT_NAME_CHARACTERISTIC_UUID.toKotlinUuid()
            } ?: throw IllegalStateException("OTS Object Name characteristic not found")

            val objNameChar = remoteService.characteristics.firstOrNull {
                it.uuid == OBJECT_NAME_CHARACTERISTIC_UUID.toKotlinUuid()
            } ?: throw IllegalStateException("OTS Object Name characteristic not found")
             */

            featureCharacteristic.let { characteristic ->
                // If the characteristic supports READ, read the initial value
                if (characteristic.properties.contains(CharacteristicProperty.READ)) {
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

            openTransferChannel(deviceId)
        }
    }

    companion object {
        private lateinit var featureCharacteristic: RemoteCharacteristic
        private var peripheral: Peripheral<*, *>? = null

         fun openTransferChannel(deviceId: String) {
            val pair = peripheral?.openCocChannel(OTS_COC_PSM)
            pair?.second?.let {
                Timber.i("Opened OTS transfer channel")
                it.write(0xdd)
                val test = (0..<256 * 3).toByteArray()
                //val test = (0..<489).toByteArray()
                it.write(test)
            }
            pair?.first?.let {
                Timber.i("Reading OTS content")
                val msg = it.readNBytes(512 * 2)
                Timber.d(msg.toList().map { num -> num.toInt() }.joinToString(" "))
            }
        }
    }
}

fun IntRange.toByteArray(): ByteArray {
    return this.map { it.toByte() }.toByteArray()
}