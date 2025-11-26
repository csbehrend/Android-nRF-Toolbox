package no.nordicsemi.android.toolbox.profile.parser.ots

import no.nordicsemi.android.toolbox.profile.parser.ots.OACPResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import java.util.UUID

/*
sealed class OACPFeatures(
    val index: Int,
) {
    data object Create : OACPFeatures(0)
    data object Delete : OACPFeatures(1)
    data object Checksum: OACPFeatures(2)
    data object Execute: OACPFeatures(3)
    data object Read: OACPFeatures(4)
    data object Write: OACPFeatures(5)
    data object Append: OACPFeatures(6)
    data object Truncate: OACPFeatures(7)
    data object Catch: OACPFeatures(8)
    data object Abort: OACPFeatures(9)

    fun fromInt(field: Int): List<OACPFeatures> {
        val features = mutableListOf<OACPFeatures>()
        for (feature in OACPFeatures::class.sealedSubclasses) {
        }

    }
    companion object {
        private val map = entries

        val entries: List<OACPFeatures> by lazy {
    }
}
*/

data class OACPFeatures (
    val create: Boolean = false,
    val delete: Boolean = false,
    val checksum: Boolean = false,
    val execute: Boolean = false,
    val read: Boolean = false,
    val write: Boolean = false,
    val append: Boolean = false,
    val truncate: Boolean = false,
    val patch: Boolean = false,
    val abort: Boolean = false,
)

data class OLCPFeatures (
    val goto: Boolean = false,
    val order: Boolean = false,
    val reqObjCount: Boolean = false,
    val clearMarking: Boolean = false,
)

data class OTSFeatures (
    // val oacp: List<OACPFeatures> = emptyList(),
    val oacp: OACPFeatures = OACPFeatures(),
    val olcp: OLCPFeatures = OLCPFeatures(),
)

data class OTSObjProperties (
    val delete: Boolean = false,
    val execute: Boolean = false,
    val read: Boolean = false,
    val write: Boolean = false,
    val append: Boolean = false,
    val truncate: Boolean = false,
    val patch: Boolean = false,
    val mark: Boolean = false,
)
data class OTSObjSize (
    val current: Int,
    val allocated: Int,
)

data class OTSObject (
    val name: String? = null,
    val type: UUID? = null,
    val size: OTSObjSize? = null,
    val id: UUID? = null,
    val properties: OTSObjProperties? = null,
)

enum class OACPOpcode(val op: Int){
    CREATE(0x01),
    DELETE(0x02),
    CHECKSUM(0x03),
    EXECUTE(0x04),
    READ(0x05),
    WRITE(0x06),
    RESPONSE(0x60)
}

sealed class OACPResult {
    data object Success : OACPResult()
    data object UnsupportedOpcode : OACPResult()
    data object InvalidParameter : OACPResult()
    data object InsufficientResources : OACPResult()
    data object InvalidObject : OACPResult()
    data object ChannelUnavailable : OACPResult()
    data object UnsupportedType : OACPResult()
    data object ProcedureNotPermitted : OACPResult()
    data object ObjectLocked : OACPResult()
    data object OperationFailed : OACPResult()

    companion object {
        fun toInt(): Int {
            return when (this) {
                Success -> 0x01
                UnsupportedOpcode -> 0x02
                InvalidParameter -> 0x03
                InsufficientResources -> 0x04
                InvalidObject -> 0x05
                ChannelUnavailable -> 0x06
                UnsupportedType -> 0x07
                ProcedureNotPermitted -> 0x08
                ObjectLocked -> 0x09
                OperationFailed -> 0x0A
                else -> throw IllegalArgumentException("Unknown OACPResult")
            }
        }
    }
}

data class OLCPResponse (
    val request: OLCPOperation? = null,
    val result: OLCPResult? = null,
) {
    override fun toString(): String {
        return "Request = $request, Result = $result"
    }
}