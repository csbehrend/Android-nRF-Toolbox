package no.nordicsemi.android.toolbox.profile.parser.ots

import no.nordicsemi.kotlin.data.IntFormat
import no.nordicsemi.kotlin.data.getInt
import java.nio.ByteOrder

object OTSDataParser {
    fun parseOACPFeatures(field: Int) = OACPFeatures(
        field and 0x01 != 0,
        field and 0x02 != 0,
        field and 0x04 != 0,
        field and 0x08 != 0,
        field and 0x10 != 0,
        field and 0x20 != 0,
        field and 0x40 != 0,
        field and 0x80 != 0,
        field and 0x100 != 0,
        field and 0x200 != 0,
    )

    fun parseOLCPFeatures(field: Int) = OLCPFeatures(
        field and 0x01 != 0,
        field and 0x02 != 0,
        field and 0x04 != 0,
        field and 0x08 != 0,
    )

    fun parseFeatures(data: ByteArray, byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): OTSFeatures? {
        val fieldLen = IntFormat.UINT32.length
        if (data.size != 2 * fieldLen) return null

        val rawOacp = data.getInt(0, IntFormat.UINT32, byteOrder)
        val oacp = parseOACPFeatures(rawOacp)

        val rawOlcp = data.getInt(fieldLen, IntFormat.UINT32, byteOrder)
        val olcp = parseOLCPFeatures(rawOlcp)

        return OTSFeatures(oacp, olcp)
    }
}