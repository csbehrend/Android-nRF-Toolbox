package no.nordicsemi.android.toolbox.profile.parser.ots

import no.nordicsemi.kotlin.data.toByteArray
import java.nio.ByteOrder
import java.util.UUID
import java.nio.ByteBuffer
import kotlin.collections.get


/*
interface OLCPPacket {
    val opcode: int
}

sealed interface OLCPParameter {
    fun toByteArray(): ByteArray

    data class GoTo(val luid: ULong) : OLCPParameter {
        override fun toByteArray(): ByteArray {
            val buffer = ByteBuffer.allocate(ULong
            buffer.putLong()
            luid.toByte()
            uuid.toByteArray(ByteOrder.LITTLE_ENDIAN)
        }
    }
}
 */

sealed class OLCPOperation (
    val opcode: Byte,
    // val parameterSize: Int = 0,
) {
    fun genPacket(): ByteArray = opcode.toByteArray()

    /*
    fun toString(): String {
        return this::class.simpleName
    }
    */
    override fun toString(): String {
        return this::class.simpleName?: "Unknown"
    }

    data object First : OLCPOperation(0x01)
    data object Last : OLCPOperation(0x02)
    data object Previous : OLCPOperation(0x03)
    data object Next : OLCPOperation(0x04)
    data object Response : OLCPOperation(0x70)

    companion object {
        private val map = OLCPOperation::class.sealedSubclasses.associateBy { it.objectInstance?.opcode }
        fun fromByteArray(packet: ByteArray): OLCPOperation {
            assert(packet.size == 1)
            return fromByte(packet[0])
        }
        fun fromByte(op: Byte): OLCPOperation {
            return map[op]?.objectInstance ?: throw IllegalArgumentException("Unknown OLCPOperation")
        }
        fun fromInt(op: Int): OLCPOperation = fromByte(op.toByte())
    }
}

sealed class OLCPResult(
    val resultCode: Byte
) {
    object Success : OLCPResult(0x01)
    object UnsupportedOpcode : OLCPResult(0x02)
    object InvalidParameter : OLCPResult(0x03)
    object OperationFailed : OLCPResult(0x04)
    object OutOfBounds : OLCPResult(0x05)
    object TooManyObjects : OLCPResult(0x06)
    object NoObject : OLCPResult(0x07)
    object ObjectIdNotFound : OLCPResult(0x08)

    companion object {
        private val map = OLCPResult::class.sealedSubclasses.associateBy { it.objectInstance?.resultCode }
        fun fromByte(op: Byte): OLCPResult {
            return map[op]?.objectInstance ?: throw IllegalArgumentException("Unknown OLCPResult")
        }
        fun fromInt(op: Int): OLCPResult = fromByte(op.toByte())
    }
}