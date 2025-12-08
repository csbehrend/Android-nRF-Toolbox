package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.parser.ots.OACPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResponse
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSFeatures
import no.nordicsemi.android.toolbox.profile.parser.ots.OTSObject
import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPResult

data class OTSServiceData(
    override val profile: Profile = Profile.OTS,
    val otsFeatures: OTSFeatures = OTSFeatures(),
    val otsObject: OTSObject = OTSObject(),
    val oacpResponse: OACPResponse = OACPResponse(),
    val olcpResponse: OLCPResponse = OLCPResponse(),
    val readData: String = ""
) : ProfileServiceData()
