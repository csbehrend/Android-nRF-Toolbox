package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.parser.gci.GCIEvent

data class GCIServiceData (
    override val profile: Profile = Profile.GCI,
    val gciEvent: GCIEvent = GCIEvent.Unknown,
    val repCount: Int = 0,
) : ProfileServiceData()
