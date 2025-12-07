package no.nordicsemi.android.toolbox.profile.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.manager.repository.BatteryRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.CSCRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.DFSRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.LBSRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.OTSRepository
import no.nordicsemi.android.toolbox.profile.parser.csc.CSCDataParser
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
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResult
import no.nordicsemi.kotlin.ble.core.util.fromShortUuid

@OptIn(ExperimentalUuidApi::class)
private val FEATURES_CHARACTERISTIC_UUID = Uuid.fromShortUuid(0x2abd)
@OptIn(ExperimentalUuidApi::class)
private val OBJECT_NAME_CHARACTERISTIC_UUID = Uuid.fromShortUuid(0x2abe)
@OptIn(ExperimentalUuidApi::class)
private val OBJECT_TYPE_CHARACTERISTIC_UUID = Uuid.fromShortUuid(0x2abf)
@OptIn(ExperimentalUuidApi::class)
private val OBJECT_SIZE_CHARACTERISTIC_UUID = Uuid.fromShortUuid(0x2ac0)
@OptIn(ExperimentalUuidApi::class)
private val OBJECT_ID_CHARACTERISTIC_UUID = Uuid.fromShortUuid(0x2ac3)
@OptIn(ExperimentalUuidApi::class)
private val OBJECT_PROPERTIES_CHARACTERISTIC_UUID = Uuid.fromShortUuid(0x2ac4)

@OptIn(ExperimentalUuidApi::class)
private val OACP_CHARACTERISTIC_UUID = Uuid.fromShortUuid(0x2AC5)
@OptIn(ExperimentalUuidApi::class)
private val OLCP_CHARACTERISTIC_UUID = Uuid.fromShortUuid(0x2AC6)

private const val OTS_COC_PSM: Int = 0x0025

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
            featureChar = remoteService.characteristics.firstOrNull {
                it.uuid == FEATURES_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("OTS Feature characteristic not found")
            objectNameChar = remoteService.characteristics.firstOrNull {
                it.uuid == OBJECT_NAME_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("OTS Object Name characteristic not found")
            objectTypeChar = remoteService.characteristics.firstOrNull {
                it.uuid == OBJECT_TYPE_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("OTS Object Type characteristic not found")
            objectSizeChar = remoteService.characteristics.firstOrNull {
                it.uuid == OBJECT_SIZE_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("OTS Object Size characteristic not found")
            oacpChar = remoteService.characteristics.firstOrNull {
                it.uuid == OACP_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("OTS OACP characteristic not found")
            olcpChar = remoteService.characteristics.firstOrNull {
                it.uuid == OLCP_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("OTS OLCP characteristic not found")

            refreshOtsFeatures(deviceId)
            refreshObjectName(deviceId)
            refreshObjectSize(deviceId)

            // olcpChar.subscribe().mapNotNull {
                //  OTSDataParser.parseOlcpResponse(it)
            // OTSRepository.onOLCPResponse(deviceId, it)
            olcpChar.subscribe().mapNotNull{
                    OTSDataParser.parseOlcpResponse(it)
                }.onEach { OTSRepository.onOLCPResponse(deviceId, it) }
                .filter { it.result is OLCPResult.Success }
                .onEach {
                    refreshObjectName(deviceId)
                    refreshObjectSize(deviceId)
                }.catch { it.printStackTrace() }
                .onCompletion { OTSRepository.clear(deviceId) }
                .launchIn(scope)

            // openTransferChannel(deviceId)
        }
    }

    companion object {
        private lateinit var featureChar: RemoteCharacteristic
        private lateinit var objectNameChar: RemoteCharacteristic
        private lateinit var objectTypeChar: RemoteCharacteristic
        private lateinit var objectSizeChar: RemoteCharacteristic
        private lateinit var oacpChar: RemoteCharacteristic
        private lateinit var olcpChar: RemoteCharacteristic
        private var peripheral: Peripheral<*, *>? = null

        // private val _otsOp

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
                Timber.d("Message size from ESP32: " + msg.size.toString())
                Timber.d(msg.toList().map { num -> num.toInt() }.joinToString(" "))
            }
            Timber.d(OTSDataParser.parseOlcpResponse(byteArrayOf(0x01, 0x01)).toString())
        }

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

        suspend fun refreshOtsFeatures(deviceId: String) {
            readCharacteristic(deviceId, featureChar) { data ->
                data.let { OTSDataParser.parseFeatures(it) }
                    ?.let { OTSRepository.updateFeatures(deviceId, it) }
            }
        }

        suspend fun refreshObjectName(deviceId: String) {
            readCharacteristic(deviceId, objectNameChar) { data ->
                data.let { OTSDataParser.parseObjectName(it) }
                .let { OTSRepository.updateObjectName(deviceId, it) }
            }
        }

        suspend fun refreshObjectSize(deviceId: String) {
            readCharacteristic(deviceId, objectSizeChar) { data ->
                data.let { OTSDataParser.parseObjectSize(it) }
                ?.let { OTSRepository.updateObjectSize(deviceId, it) }
            }
        }

        suspend fun requestOLCPOperation(deviceId: String, operation: OLCPOperation) {
            val data = operation.genPacket()

            try {
                if (::olcpChar.isInitialized) {
                    olcpChar.write(data, WriteType.WITH_RESPONSE)
                }
            } catch (e: Exception) {
                Timber.e("Error writing to OLCP characteristic: ${e.message}")
            }
        }
    }
}

fun IntRange.toByteArray(): ByteArray {
    return this.map { it.toByte() }.toByteArray()
}