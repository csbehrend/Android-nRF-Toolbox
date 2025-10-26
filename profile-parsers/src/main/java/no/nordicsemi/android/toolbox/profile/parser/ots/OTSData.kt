package no.nordicsemi.android.toolbox.profile.parser.ots

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

@OptIn(ExperimentalUuidApi::class)
data class OTSObject (
    val name: String? = null,
    val type: Uuid? = null,
    val size: OTSObjSize? = null,
    val id: Uuid? = null,
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

enum class OACPResult(val op: Int){
    SUCCESS(0x01),
    UNSUPPORTED_OPCODE(0x02),
    INVALID_PARAMETER(0x03),
    INSUFFICIENT_RESOURCES(0x04),
    INVALID_OBJECT(0x05),
    CHANNEL_UNAVAILBLE(0x06),
    UNSUPPORTED_TYPE(0x07),
    PROCEDURE_NOT_PERMITTED(0x08),
    OBJECT_LOCKED(0x09),
    OPERATION_FAILED(0x0A)
}

enum class OLCPOpcode(val op: Int){
    FIRST(0x01),
    LAST(0x02),
    PREVIOUS(0x03),
    NEXT(0x04),
    CODE(0x70),
}

enum class OLCPResult(val op: Int) {
    SUCCESS(0x01),
    UNSUPPORTED_OPCODE(0x02),
    INVALID_PARAMETER(0x03),
    OPERATION_FAILED(0x04),
    OUT_OF_BOUNDS(0x05),
    TOO_MANY_OBJECTS(0x06),
    NO_OBJECT(0x07),
    OBJECT_ID_NOT_FOUND(0x08),
}
