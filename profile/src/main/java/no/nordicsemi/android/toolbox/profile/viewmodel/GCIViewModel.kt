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
import no.nordicsemi.android.toolbox.lib.utils.Profile.OTS
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.profile.data.GCIServiceData
import no.nordicsemi.android.toolbox.profile.data.OTSServiceData
import no.nordicsemi.android.toolbox.profile.manager.repository.GCIRepository
import no.nordicsemi.android.toolbox.profile.manager.repository.OTSRepository
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation
import no.nordicsemi.android.toolbox.profile.repository.DeviceRepository
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2

internal sealed interface GCIEvent {
    data class StartExercise(
        val id: Int
    ) : GCIEvent
    data object PauseExercise : GCIEvent
    data object StopExercise : GCIEvent
}

@HiltViewModel
internal class GCIViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {

    val address = parameterOf(ProfileDestinationId)

    private val _gciState = MutableStateFlow(GCIServiceData())
    val gciState = _gciState.asStateFlow()

    init {
        observeGCIProfile()
    }

    /**
     * Observes the [DeviceRepository.profileHandlerFlow] from the [deviceRepository] that contains [Profile.OTS].
     */
    private fun observeGCIProfile() = viewModelScope.launch {
        // update state or emit to UI
        deviceRepository.profileHandlerFlow
            .onEach { mapOfPeripheralProfiles ->
                mapOfPeripheralProfiles.forEach { (peripheral, profiles) ->
                    if (peripheral.address == address) {
                        profiles.filter { it.profile == Profile.GCI }
                            .forEach { _ ->
                                startGCIService(peripheral.address)
                            }
                    }
                }
            }.launchIn(this)
    }

    /**
     * Starts the OTS service and observes changes.
     */
    private fun startGCIService(address: String) {
        // Start the GCI service and observe location changes
        GCIRepository.getData(address).onEach {
            _gciState.value = _gciState.value.copy(
                profile = it.profile,
                gciEvent = it.gciEvent,
                repCount = it.repCount,
            )
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: GCIEvent) {
        when (event) {
            is GCIEvent.StartExercise -> {
                viewModelScope.launch {
                    GCIRepository.startExercise(address,event.id)
                }
            }
            is GCIEvent.PauseExercise -> {
                viewModelScope.launch {
                    GCIRepository.pauseExercise(address)
                }
            }
            is GCIEvent.StopExercise -> {
                viewModelScope.launch {
                    GCIRepository.stopExercise(address)
                }
            }
        }
    }
}
