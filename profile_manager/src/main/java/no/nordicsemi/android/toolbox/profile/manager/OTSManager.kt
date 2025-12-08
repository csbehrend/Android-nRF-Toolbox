package no.nordicsemi.android.toolbox.profile.manager

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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
import no.nordicsemi.android.toolbox.profile.parser.ots.OACPOperation
import no.nordicsemi.android.toolbox.profile.parser.ots.OACPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OACPResult
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
import no.nordicsemi.kotlin.ble.core.exception.CocException
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

            oacpChar.subscribe().mapNotNull {
                    OTSDataParser.parseOacpResponse(it)
                }.zip(oacpOperations) { response, operation ->
                    assert(response.request == operation.opcode)
                    OTSRepository.onOACPResponse(deviceId, response)
                    _oacpResponse.emit(Pair(operation, response))
                }.catch { it.printStackTrace() }
                .onCompletion { OTSRepository.clear(deviceId) }
                .launchIn(scope)

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

        private var _oacpOperations = MutableSharedFlow<OACPOperation>()
        val oacpOperations: SharedFlow<OACPOperation> = _oacpOperations

        private var _oacpResponse = MutableSharedFlow<Pair<OACPOperation, OACPResponse>?>()
        val oacpResponse: SharedFlow<Pair<OACPOperation, OACPResponse>?> = _oacpResponse

        val oacpMutex = Mutex()


        // private val _otsOp
        suspend fun readCurrentObject(deviceId: String) {
            if (oacpMutex.isLocked) return
            val peripheral = peripheral ?: return
            val currentObject = OTSRepository.getData(deviceId).firstOrNull()?.otsObject ?: return
            val size = currentObject.size?.current ?: return
            val op = OACPOperation.Read(0, size)
            oacpMutex.withLock {
                try {
                    peripheral.openCocChannel(OTS_COC_PSM)
                    requestOACPOperation(deviceId, op)
                    val data = awaitOACPResponse(op)?.takeIf { it.isSuccess() }
                        ?.let { oacpReadWorker(op) }
                        ?.also{
                            OTSRepository.onObjectRead(deviceId, it)
                            Timber.i("DATA READ COMPLETE: $it")
                        }
                } catch (e: Exception) {
                    Timber.e("Error reading current object: ${e.message}")
                } finally {
                    Timber.i("CLOSING OTS CHANNEL")
                    peripheral.closeCocChannel(OTS_COC_PSM)
                }
            }
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

        private suspend fun awaitOACPResponse(operation: OACPOperation): OACPResponse? {
            val resp = withTimeoutOrNull(2000) { oacpResponse.first{ it?.first == operation } }
            return resp?.second
        }

        private suspend fun requestOACPOperation(deviceId: String, operation: OACPOperation) {
            val data = operation.genPacket()
            try {
                if (::oacpChar.isInitialized) {
                    oacpChar.write(data, WriteType.WITH_RESPONSE)
                    _oacpOperations.emit(operation)
                }
            } catch (e: Exception) {
                Timber.e("Error writing to OACP characteristic: ${e.message}")
            }
        }

        private fun oacpReadWorker(readOperation: OACPOperation.Read): ByteArray? {
            val peripheral = peripheral ?: return null
            var data: ByteArray? = null
            try {
                data = peripheral.readFromCocChannel(OTS_COC_PSM, readOperation.length)
            } catch (e: CocException) {
                Timber.e("Error reading from OTS channel: ${e.message}")
            }
            return data
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