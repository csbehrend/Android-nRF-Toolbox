package no.nordicsemi.android.toolbox.profile.parser.gci

import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

object GCIDataParser {
    fun parseEvent(data: ByteArray, byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): GCIEvent? {
        return GCIEvent.fromByteArray(data)
    }
}