package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObject

data class OTSServiceData(
    override val profile: Profile = Profile.OTS,
    val otsFeatures: OTSFeatures? = null,
    val otsObject: OTSObject? = null,
) : ProfileServiceData()
