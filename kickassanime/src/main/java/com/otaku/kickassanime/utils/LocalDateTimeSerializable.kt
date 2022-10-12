package com.otaku.kickassanime.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDateTime::class)
class LocalDateTimeSerializable : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("LocalDateTime") {
        element<Long>("time")
    }

    @Throws(SerializationException::class)
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return decoder.decodeStructure(descriptor) {
            val time: Long = decodeLongElement(descriptor, 0)
            LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC)
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.toEpochSecond(ZoneOffset.UTC))
        }
    }
}