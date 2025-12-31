package com.instanttechnologies.instant.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.instanttechnologies.instant.data.Alert
import com.instanttechnologies.instant.data.ChangeIKeyRequest
import com.instanttechnologies.instant.data.Chat
import com.instanttechnologies.instant.data.GetMessagesRequest
import com.instanttechnologies.instant.data.GetMessagesResponse
import com.instanttechnologies.instant.data.GetPropertiesRequest
import com.instanttechnologies.instant.data.GetPropertiesResponse
import com.instanttechnologies.instant.data.NewChatRequest
import com.instanttechnologies.instant.data.RegisterRequest
import com.instanttechnologies.instant.data.SearchRequest
import com.instanttechnologies.instant.data.SendMessageRequest
import com.instanttechnologies.instant.data.SyncMessage
import com.instanttechnologies.instant.data.User
import com.instanttechnologies.instant.data.WhoAmIResponse
import com.instanttechnologies.instant.security.HandshakeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class INSTANTWS(
    private val address: String
) {
    private val TAG = "INSTANT"

    private val scope = CoroutineScope(Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private lateinit var handshakeHelper: HandshakeHelper

    private val _incomingMessages = MutableLiveData<INSTANTWSMessage>()
    val incomingMessages: LiveData<INSTANTWSMessage> = _incomingMessages

    init {
        connectWebSocket()
    }

    private fun connectWebSocket() {
        webSocket?.close(1000, "Reconnecting")
        handshakeHelper = HandshakeHelper()

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("ws://$address/api/instant")
            .header("X-Client-Type", "instant")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WS opened, init handshake")
                webSocket.send(ByteString.of(*handshakeHelper.handshakeBytes))
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val msgType = bytes[0].toInt()
                val payload = bytes.toByteArray().sliceArray(1..<bytes.size)

                Log.d(TAG, msgType.toString())

                when(msgType) {
                    0, 1 -> {
                        handshakeHelper.finishHandshake(payload)
                        Log.d(TAG, "Handshake finished")
                        _incomingMessages.postValue(INSTANTWSMessage.Ready(msgType == 1, handshakeHelper.requestID))
                    }
                    51 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<WhoAmIResponse>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.Register(resp))
                    }
                    52 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<List<Chat>>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.GetChats(resp))
                    }
                    53 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<List<User>>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.Search(resp))
                    }
                    54 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<GetPropertiesResponse>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.GetProperties(resp))
                    }
                    55 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<Chat>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.NewChat(resp))
                    }
                    56 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<GetMessagesResponse>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.GetMessages(resp))
                    }
                    57 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<SyncMessage>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.SendMessage(resp))
                    }
                    88 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<WhoAmIResponse>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.WhoAmI(resp))
                    }
                    89 -> {
                        val dec = handshakeHelper.decrypt(payload)
                        val resp = Json.decodeFromString<List<Alert>>(dec.decodeToString())
                        _incomingMessages.postValue(INSTANTWSMessage.GetAlerts(resp))
                    }
                    90 -> {
                        _incomingMessages.postValue(INSTANTWSMessage.ChangeIKey)
                    }
                    100 -> {
                        handshakeHelper.rotateKey(payload)
                    }
                    123 -> {
                        _incomingMessages.postValue(INSTANTWSMessage.LoginDeniedError)
                    }
                    124 -> {
                        _incomingMessages.postValue(INSTANTWSMessage.AccessDeniedError)
                    }
                    125 -> {
                        _incomingMessages.postValue(INSTANTWSMessage.DuplicatedLoginError)
                    }
                    126 -> {
                        _incomingMessages.postValue(INSTANTWSMessage.EmptyCredentialsError)
                    }
                    127 -> {
                        _incomingMessages.postValue(INSTANTWSMessage.FatalError(payload.decodeToString()))
                    }
                    else -> {
                        Log.d(TAG, "Else branch reached: " + bytes[0])
                        _incomingMessages.postValue(INSTANTWSMessage.UnspecifiedTypeError(msgType))
                        webSocket.close(1000, "Error occurred")
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Connection closed. $code - \"$reason\"")
                if (code != 1000) {
                    _incomingMessages.postValue(INSTANTWSMessage.NotReady)
                    scope.launch {
                        delay(2000L)
                        connectWebSocket()
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.d(TAG, "Failure. ${t.message}")
                _incomingMessages.postValue(INSTANTWSMessage.NotReady)
                scope.launch {
                    delay(2000L)
                    connectWebSocket()
                }
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
    }

    fun register(req: RegisterRequest) {
        val enc = byteArrayOf(11)+handshakeHelper.encrypt(req, RegisterRequest.serializer())
        Log.d(TAG, enc.toHexString())
        webSocket?.send(
            ByteString.of(
                *enc
            )
        )
    }

    fun getChats() {
        webSocket?.send(ByteString.of(*byteArrayOf(12)))
    }

    fun search(req: SearchRequest) {
        webSocket?.send(
            ByteString.of(
                *byteArrayOf(13)+handshakeHelper.encrypt(req, SearchRequest.serializer())
            )
        )
    }

    fun getProperties(req: GetPropertiesRequest) {
        webSocket?.send(
            ByteString.of(
                *byteArrayOf(14)+handshakeHelper.encrypt(req, GetPropertiesRequest.serializer())
            )
        )
    }

    fun newChat(req: NewChatRequest) {
        webSocket?.send(
            ByteString.of(
                *byteArrayOf(15)+handshakeHelper.encrypt(req, NewChatRequest.serializer())
            )
        )
    }

    fun getMessages(req: GetMessagesRequest) {
        webSocket?.send(
            ByteString.of(
                *byteArrayOf(16)+handshakeHelper.encrypt(req, GetMessagesRequest.serializer())
            )
        )
    }

    fun sendMessage(req: SendMessageRequest) {
        webSocket?.send(
            ByteString.of(
                *byteArrayOf(17)+handshakeHelper.encrypt(req, SendMessageRequest.serializer())
            )
        )
    }

    fun whoAmI() {
        webSocket?.send(
            ByteString.of(*byteArrayOf(48))
        )
    }

    fun getAlerts() {
        webSocket?.send(
            ByteString.of(*byteArrayOf(49))
        )
    }

    fun changeIdentityKey() {
        val req = ChangeIKeyRequest(handshakeHelper.changeIdentityKey())
        webSocket?.send(
            ByteString.of(
                *byteArrayOf(50)+handshakeHelper.encrypt(req, ChangeIKeyRequest.serializer())
            )
        )
    }
}
