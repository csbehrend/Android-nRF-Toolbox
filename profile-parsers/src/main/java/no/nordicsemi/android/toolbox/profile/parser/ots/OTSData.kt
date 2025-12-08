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

enum class OACPOpcode(val op: Byte){
    CREATE(0x01),
    DELETE(0x02),
    CHECKSUM(0x03),
    EXECUTE(0x04),
    READ(0x05),
    WRITE(0x06),
    RESPONSE(0x60);

    companion object {
        private val map = entries.associateBy(OACPOpcode::op)
        fun fromByte(opcode: Byte) = map[opcode]
        fun toByte(opcode: OACPOpcode) = opcode.op
    }
}

enum class OACPResult(val code: Byte) {
    SUCCESS(0x01),
    UNSUPPORTED_OPCODE(0x02),
    INVALID_PARAMETER(0x03),
    INSUFFICIENT_RESOURCES(0x04),
    INVALID_OBJECT(0x05),
    CHANNEL_UNAVAILABLE(0x06),
    UNSUPPORTED_TYPE(0x07),
    PROCEDURE_NOT_PERMITTED(0x08),
    OBJECT_LOCKED(0x09),
    OPERATION_FAILED(0x0A);

    companion object {
        private val map = OACPResult.entries.associateBy(OACPResult::code)
        fun fromByte(code: Byte) = map[code]
        fun toByte(result: OACPResult): Byte = result.code
    }
}

data class OACPResponse (
    val request: OACPOpcode? = null,
    val result: OACPResult? = null,
) {
    override fun toString(): String {
        return "Request = $request, Result = $result"
    }

    fun isSuccess(): Boolean {
        return result == OACPResult.SUCCESS
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