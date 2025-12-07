package no.nordicsemi.android.toolbox.profile.parser.gci

import no.nordicsemi.android.toolbox.profile.parser.ots.OLCPOperation

sealed class GCIEvent(
    val opcode: Byte
) {
    data object Unknown : GCIEvent(0x00)
    data object ActivityStarted : GCIEvent(0x01)
    data object RepCompleted : GCIEvent(0x02)
    data object ActivityCompleted : GCIEvent(0x03)
    data object ActivityPaused : GCIEvent(0x04)
    data object ActivityResumed : GCIEvent(0x05)
    data object ActivityCanceled : GCIEvent(0x06)
    data object Error : GCIEvent(0x70)

    companion object {
        private val map = GCIEvent::class.sealedSubclasses.associateBy { it.objectInstance?.opcode }
        fun fromByteArray(packet: ByteArray): GCIEvent? {
            if (packet.size != 1) return null
            return fromByte(packet[0])
        }
        fun fromByte(op: Byte): GCIEvent {
            return map[op]?.objectInstance ?: throw IllegalArgumentException("Unknown GCIEvent")
        }
        fun fromInt(op: Int): GCIEvent = fromByte(op.toByte())
    }
}