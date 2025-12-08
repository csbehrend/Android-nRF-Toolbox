package no.nordicsemi.android.toolbox.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.profile.data.OTSServiceData
import no.nordicsemi.android.toolbox.profile.manager.repository.LBSRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.OTSRepository
import no.nordicsemi.android.toolbox.profile.parser.ots.OACPOperation
import no.nordicsemi.android.toolbox.profile.repository.DeviceRepository
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation

internal sealed interface OTSEvent {
    data class OnOLCPRequest(
        val operation: OLCPOperation
    ) : OTSEvent
    /*
    data class OnOACPRequest(
        val operation: OACPOperation
    ) : OTSEvent
     */
    data object ReadObject : OTSEvent
    data object WriteRange : OTSEvent
}

@HiltViewModel
internal class OTSViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {

    val address = parameterOf(ProfileDestinationId)

    private val _otsState = MutableStateFlow(OTSServiceData())
    val otsState = _otsState.asStateFlow()

    init {
        observeOtsProfile()
    }

    /**
     * Observes the [DeviceRepository.profileHandlerFlow] from the [deviceRepository] that contains [Profile.OTS].
     */
    private fun observeOtsProfile() = viewModelScope.launch {
        // update state or emit to UI
        deviceRepository.profileHandlerFlow
            .onEach { mapOfPeripheralProfiles ->
                mapOfPeripheralProfiles.forEach { (peripheral, profiles) ->
                    if (peripheral.address == address) {
                        profiles.filter { it.profile == Profile.OTS }
                            .forEach { _ ->
                                startOTSService(peripheral.address)
                            }
                    }
                }
            }.launchIn(this)
    }

    /**
     * Starts the OTS service and observes changes.
     */
    private fun startOTSService(address: String) {
        // Start the OTS service and observe location changes
        OTSRepository.getData(address).onEach {
            _otsState.value = _otsState.value.copy(
                profile = it.profile,
                otsFeatures = it.otsFeatures,
                otsObject = it.otsObject,
                olcpResponse = it.olcpResponse,
                oacpResponse = it.oacpResponse,
                readData = it.readData,
            )
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: OTSEvent) {
        when (event) {
            /*
            is OTSEvent.OnOACPRequest -> {
                viewModelScope.launch {
                    OTSRepository.requestOACPOperation(address, event.operation)
                }
            }
             */
            is OTSEvent.ReadObject -> {
                viewModelScope.launch {
                    OTSRepository.getObjectVal(address)
                }
            }
            is OTSEvent.WriteRange -> {
                viewModelScope.launch {
                    OTSRepository.writeRange(address)
                }
            }
            is OTSEvent.OnOLCPRequest -> {
                viewModelScope.launch {
                    OTSRepository.requestOLCPOperation(address, event.operation)
                }
            }
        }
    }
}
