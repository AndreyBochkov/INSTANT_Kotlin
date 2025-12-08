package com.instanttechnologies.instant.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Chat(
    val chatid: Int,
    val label: String,
    val cansend: Boolean
)

data class ChatProperties(
    val chatid: Int,
    val label: String,
    val cansend: Boolean,
    val admins: List<User>,
    val listeners: List<User>
)

@Serializable
data class User(
    val userid: Int,
    val login: String
)

@Serializable
data class Message(
    val messageid: Long,
    val ts: Long,
    val body: String,
    val sender: Int
)

@Serializable
data class Alert(
    val alertid: Int,
    val ts: Long,
    val body: String
)

@Serializable
data class SyncMessage(
    val chatid: Int,
    val messageid: Long,
    val ts: Long,
    val body: String,
    val sender: Int
)

@Serializable
data class RegisterRequest( // 11
    val login: String
)

// GetChatsRequest [12]

@Serializable
data class SearchRequest( // 13
    val query: String
)

@Serializable
data class GetPropertiesRequest( // 14
    val chatid: Int
)

@Serializable
data class NewChatRequest( // 15
    val admins: List<Int>,
    val listeners: List<Int>,
    val label: String
)

@Serializable
data class GetMessagesRequest( // 16
    val chatid: Int,
    val offset: Int
)

@Serializable
data class SendMessageRequest( // 17
    val chatid: Int,
    val body: String
)

@Serializable
data class ChangeIKeyRequest( // 50
    @Serializable(with = ByteArrayAsUnsignedListSerializer::class)
    val new: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangeIKeyRequest

        return new.contentEquals(other.new)
    }

    override fun hashCode(): Int {
        return new.contentHashCode()
    }
}

//RegisterResponse [51]

//GetChatsResponse [52]List<Chat>

//SearchResponse [53]List<User>

@Serializable
data class GetPropertiesResponse( // 54
    val chatid: Int,
    val admins: List<User>,
    val listeners: List<User>
)

//NewChatResponse [55]Chat

@Serializable
data class GetMessagesResponse( // 56
    val chatid: Int,
    val messages: List<Message>
)

//SendMessageResponse [55]SyncMessage

//GotMessageAck [91]SyncMessage

//RotateKeys [92]

object ByteArrayAsUnsignedListSerializer : KSerializer<ByteArray> {
    override val descriptor = ListSerializer(Int.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: ByteArray) {
        val unsignedList = value.map { it.toInt() and 0xFF }
        encoder.encodeSerializableValue(ListSerializer(Int.serializer()), unsignedList)
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        val unsignedList = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
        return unsignedList.map { it.toByte() }.toByteArray()
    }
}

//ChangeIKeyResponse [90]