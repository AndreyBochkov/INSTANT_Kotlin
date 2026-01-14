package com.instanttechnologies.instant.ui.composable

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.data.Alert
import com.instanttechnologies.instant.data.ChatProperties
import com.instanttechnologies.instant.data.DeleteChatData
import com.instanttechnologies.instant.data.DeleteTieData
import com.instanttechnologies.instant.data.GetMessagesRequest
import com.instanttechnologies.instant.data.GetPropertiesRequest
import com.instanttechnologies.instant.data.Message
import com.instanttechnologies.instant.data.NewChatRequest
import com.instanttechnologies.instant.data.PageType
import com.instanttechnologies.instant.data.RegisterRequest
import com.instanttechnologies.instant.data.SearchRequest
import com.instanttechnologies.instant.data.SendMessageRequest
import com.instanttechnologies.instant.data.Tie
import com.instanttechnologies.instant.data.User
import com.instanttechnologies.instant.network.INSTANTWS
import com.instanttechnologies.instant.network.INSTANTWSMessage
import com.instanttechnologies.instant.security.EncryptedStorage
import com.instanttechnologies.instant.utils.DateTimeConverter
import com.instanttechnologies.instant.utils.showToast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class INSTANTUiState(
    val pageType: PageType = PageType.Register,
    val name: String = "",
    val id: Int = 0,
    val errorText: String? = null,
    val chats: List<ChatProperties> = emptyList(),
    val users: List<User> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val messages: Map<Int, List<Message>> = emptyMap(),
    val currentChat: Int? = null,
    val backgroundWork: Int? = 0,
    val connected: Boolean = false
)

class INSTANTViewModel(
    application: Application
): AndroidViewModel(application) {
    private val TAG = "INSTANT"

    private val encryptedStorage = EncryptedStorage(application)
    private lateinit var webSocket: INSTANTWS
    private val _uiState = MutableStateFlow(INSTANTUiState())
    private var requestID = ""

    val uiState: StateFlow<INSTANTUiState> = _uiState

    fun goForeground() {
        webSocket = INSTANTWS(encryptedStorage.loadAddress()?:application.getString(R.string.address))

        viewModelScope.launch {
            webSocket.incomingMessages.collect { wsMessage ->
                if (_uiState.value.backgroundWork == null) return@collect

                Log.d(TAG, "VM is proceeding ${wsMessage.javaClass.name}")

                when (wsMessage) {
                    is INSTANTWSMessage.Ready -> {
                        requestID = wsMessage.requestID
                        if (wsMessage.registered) {
                            webSocket.whoAmI()
                            webSocket.getChats()
                            if (_uiState.value.currentChat != null) {
                                webSocket.getProperties(GetPropertiesRequest(
                                    chatid = _uiState.value.currentChat!!
                                ))
                                webSocket.getMessages(GetMessagesRequest(
                                    chatid = _uiState.value.currentChat!!,
                                    offset = 0
                                ))
                            }
                            _uiState.update {
                                it.copy(
                                    backgroundWork = if (_uiState.value.currentChat != null) 4 else 2,
                                    connected = false
                                )
                            }
                        } else {
                            _uiState.value = INSTANTUiState(
                                pageType = PageType.Register,
                                backgroundWork = 0,
                                connected = true
                            )
                        }
                    }
                    is INSTANTWSMessage.Register -> {
                        application.showToast(application.getString(R.string.register_success))
                        _uiState.update {
                            it.copy(
                                pageType = PageType.Chats,
                                name = wsMessage.resp.login,
                                id = wsMessage.resp.id,
                                backgroundWork = it.backgroundWork!! - 1
                            )
                        }
                    }
                    is INSTANTWSMessage.GetChats -> {
                        _uiState.update {
                            it.copy(
                                chats = it.chats.plus(wsMessage.resp.map { ch ->
                                    ChatProperties(
                                        ch.chatid,
                                        ch.label,
                                        ch.cansend,
                                        emptyList(),
                                        emptyList()
                                    )
                                }
                            )
                            .groupBy { chPr -> chPr.chatid }
                            .map { (_, chats) ->
                                chats.first().copy(cansend = chats.last().cansend)
                            },
                            messages = it.messages.apply {
                                wsMessage.resp.forEach { chat ->
                                    if (!containsKey(chat.chatid)) {
                                        plus(chat.chatid to emptyList())
                                    }
                                }
                            },
                            backgroundWork = it.backgroundWork!! - 1,
                            connected = it.backgroundWork == 1
                        )
                    }
//                        encryptedStorage.saveChats(wsMessage.resp)
                    }
                    is INSTANTWSMessage.Search -> {
                        _uiState.update {
                            it.copy(
                                users = wsMessage.resp,
                                backgroundWork = it.backgroundWork!! - 1
                            )
                        }
                    }
                    is INSTANTWSMessage.GetProperties -> {
                        _uiState.update {
                            it.copy(
                                chats = it.chats.map { chat ->
                                    if (chat.chatid == wsMessage.resp.chatid) {
                                        chat.copy(
                                            admins = wsMessage.resp.admins,
                                            listeners = wsMessage.resp.listeners
                                        )
                                    } else {
                                        chat
                                    }
                                },
                                backgroundWork = it.backgroundWork!! - 1,
                                connected = it.connected || it.backgroundWork == 1
                            )
                        }
                    }
                    is INSTANTWSMessage.NewChat -> {
                        if (!_uiState.value.messages.containsKey(wsMessage.resp.chatid)) {
                            application.showToast(application.getString(R.string.new_chat_label) + ": " + wsMessage.resp.label)
                            _uiState.update {
                                it.copy(
                                    chats = it.chats.toMutableList()
                                        .plus(
                                            ChatProperties(
                                                chatid = wsMessage.resp.chatid,
                                                label = wsMessage.resp.label,
                                                cansend = wsMessage.resp.cansend,
                                                admins = emptyList(),
                                                listeners = emptyList()
                                            )
                                        ),
                                    messages = it.messages.plus(wsMessage.resp.chatid to emptyList())
                                )
                            }
//                            encryptedStorage.saveChats(_uiState.value.chats)
                        } else {
                            Log.d(TAG, "Unusual behaviour: newChat overwrites existing one.")
                        }
                    }
                    is INSTANTWSMessage.GetMessages -> {
                        _uiState.update { oldState ->
                            oldState.copy(
                                messages = oldState.messages.plus(
                                    wsMessage.resp.chatid to (
                                            (oldState.messages[wsMessage.resp.chatid] ?: emptyList())
                                                    + wsMessage.resp.messages
                                            )
                                        .groupBy { it.messageid }
                                        .map { (_, messages) -> messages.last() }
                                        .sortedBy { it.ts }
                                ),
                                backgroundWork = oldState.backgroundWork!! - 1,
                                connected = oldState.connected || oldState.backgroundWork == 1
                            )
                        }
                    }
                    is INSTANTWSMessage.SendMessage -> {
                        _uiState.update {
                            it.copy(
                                messages = it.messages.plus(
                                    wsMessage.resp.chatid to
                                            it.messages[wsMessage.resp.chatid]!!
                                                .plus(
                                                    Message(
                                                        messageid = wsMessage.resp.messageid,
                                                        ts = wsMessage.resp.ts,
                                                        body = wsMessage.resp.body,
                                                        sender = wsMessage.resp.sender
                                                    )
                                                )
                                )
                            )
                        }
                    }
                    is INSTANTWSMessage.AddTie -> {
                        _uiState.update {
                            it.copy(
                                chats = it.chats.map { chat ->
                                    if (chat.chatid == wsMessage.resp.chatid) {
                                        if (wsMessage.resp.cansend) {
                                            chat.copy(
                                                admins = chat.admins.plus(
                                                    User(
                                                        userid = wsMessage.resp.userid,
                                                        login = wsMessage.resp.login
                                                    )
                                                )
                                            )
                                        } else {
                                            chat.copy(
                                                listeners = chat.listeners.plus(
                                                    User(
                                                        userid = wsMessage.resp.userid,
                                                        login = wsMessage.resp.login
                                                    )
                                                )
                                            )
                                        }
                                    } else {
                                        chat
                                    }
                                }
                            )
                        }
                    }
                    is INSTANTWSMessage.DeleteTie -> {
                        _uiState.update {
                            it.copy(
                                chats = it.chats.map { chat ->
                                    if (chat.chatid == wsMessage.resp.chatid) {
                                        chat.copy(
                                            admins = chat.admins.filterNot { user -> user.userid == wsMessage.resp.userid },
                                            listeners = chat.listeners.filterNot { user -> user.userid == wsMessage.resp.userid }
                                        )
                                    } else {
                                        chat
                                    }
                                }
                            )
                        }
                    }
                    is INSTANTWSMessage.DeleteChat -> {
                        _uiState.update {
                            it.copy(
                                chats = it.chats.filterNot { chat -> chat.chatid == wsMessage.resp.chatid },
                                messages = it.messages.filterNot { (chatid, _) -> chatid == wsMessage.resp.chatid },
                                pageType = if (wsMessage.resp.chatid == it.currentChat) PageType.Chats else it.pageType,
                                currentChat = if (wsMessage.resp.chatid == it.currentChat) null else it.currentChat,
                                backgroundWork = if (wsMessage.resp.chatid == it.currentChat) 0 else it.backgroundWork
                            )
                        }
                    }
                    is INSTANTWSMessage.WhoAmI -> {
                        _uiState.update {
                            it.copy(
                                name = wsMessage.resp.login,
                                id = wsMessage.resp.id,
                                backgroundWork = it.backgroundWork!! - 1,
                                connected = it.backgroundWork == 1
                            )
                        }
                    }
                    is INSTANTWSMessage.GetAlerts -> {
                        _uiState.update {
                            it.copy(
                                alerts = wsMessage.resp,
                                backgroundWork = it.backgroundWork!! - 1
                            )
                        }
                    }
                    is INSTANTWSMessage.ChangeIKey -> {
                        application.showToast(application.getString(R.string.change_ikey_success))
                        _uiState.update {
                            it.copy(
                                backgroundWork = it.backgroundWork!! - 1
                            )
                        }
                    }
                    INSTANTWSMessage.LoginDeniedError -> {
                        _uiState.update {
                            it.copy(
                                errorText = application.getString(R.string.login_denied_123),
                                backgroundWork = it.backgroundWork!! - 1
                            )
                        }
                    }
                    INSTANTWSMessage.DuplicatedLoginError -> {
                        _uiState.update {
                            it.copy(
                                errorText = application.getString(R.string.duplicated_login_125),
                                backgroundWork = it.backgroundWork!! - 1
                            )
                        }
                    }
                    INSTANTWSMessage.EmptyCredentialsError -> {
                        _uiState.update {
                            it.copy(
                                errorText = application.getString(R.string.empty_credentials_126),
                                backgroundWork = it.backgroundWork!! - 1
                            )
                        }
                    }
                    is INSTANTWSMessage.FatalError -> {
                        _uiState.update {
                            it.copy(
                                pageType = PageType.Error,
                                errorText = wsMessage.message,
                                backgroundWork = null
                            )
                        }
                    }
                    is INSTANTWSMessage.UnspecifiedTypeError -> {
                        _uiState.update {
                            it.copy(
                                pageType = PageType.Error,
                                errorText = application.getString(R.string.unspecified_type_error) + wsMessage.type,
                                backgroundWork = null
                            )
                        }
                    }
                    INSTANTWSMessage.NotReady -> {
                        _uiState.update {
                            it.copy(
                                backgroundWork = 0,
                                connected = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun resetAddress(newAddress: String) {
        encryptedStorage.saveAddress(newAddress)
        webSocket.disconnect()
        initializeUIState()
        goForeground()
    }

    fun goBackground() {
        webSocket.disconnect()
        _uiState.update {
            it.copy(
                backgroundWork = 0,
                connected = false
            )
        }
    }

    init {
        DateTimeConverter.actualizeDateTime(encryptedStorage.loadDateTime())
        initializeUIState()
    }

    private fun initializeUIState() {
        _uiState.value = INSTANTUiState(
            pageType = PageType.Chats,
            connected = false
//            chats = encryptedStorage.loadChats()
        )
    }

    fun register(login: String) {
        if (_uiState.value.connected) {
            webSocket.register(
                RegisterRequest(
                    login = login,
                )
            )
            _uiState.update {
                it.copy(
                    errorText = null,
                    backgroundWork = it.backgroundWork!! + 1,
                )
            }
        }
    }

    fun search(query: String) {
        if (_uiState.value.connected) {
            webSocket.search(
                SearchRequest(
                    query = query
                )
            )
            _uiState.update {
                it.copy(
                    errorText = null,
                    users = emptyList(),
                    backgroundWork = it.backgroundWork!! + 1
                )
            }
        }
    }

    fun newChat(admins: List<Int>, listeners: List<Int>, label: String) {
        if (_uiState.value.connected) {
            webSocket.newChat(
                NewChatRequest(
                    admins = admins,
                    listeners = listeners,
                    label = label
                )
            )
            _uiState.update {
                it.copy(
                    errorText = null
                )
            }
        }
    }

    fun getMessages(offset: Int) {
        if (_uiState.value.connected) {
            webSocket.getMessages(
                GetMessagesRequest(
                    chatid = _uiState.value.currentChat!!,
                    offset = offset
                )
            )
            _uiState.update {
                it.copy(
                    backgroundWork = it.backgroundWork!! + 1
                )
            }
        }
    }

    fun sendMessage(body: String) {
        if (_uiState.value.connected) {
            webSocket.sendMessage(
                SendMessageRequest(
                    chatid = _uiState.value.currentChat!!,
                    body = body
                )
            )
        }
    }

    fun addTie(userid: Int, chatid: Int, cansend: Boolean) {
        if (_uiState.value.connected) {
            webSocket.addTie(
                Tie(
                    userid = userid,
                    chatid = chatid,
                    cansend = cansend
                )
            )
        }
    }

    fun deleteTie(userid: Int, chatid: Int) {
        if (_uiState.value.connected) {
            webSocket.deleteTie(
                DeleteTieData(
                    userid = userid,
                    chatid = chatid
                )
            )
        }
    }

    fun deleteChat(chatid: Int) {
        if (_uiState.value.connected) {
            webSocket.deleteChat(
                DeleteChatData(
                    chatid = chatid
                )
            )
        }
    }

    fun changeIKey() {
        if (_uiState.value.connected) {
            webSocket.changeIdentityKey()
            _uiState.update {
                it.copy(
                    backgroundWork = it.backgroundWork!! + 1
                )
            }
        }
    }

    fun saveDateTime(date: String, time: String) {
        encryptedStorage.saveDateTime(date, time)
        DateTimeConverter.actualizeDateTime(date to time)
    }

    fun copyRequestID() {
        val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", requestID)
        clipboard.setPrimaryClip(clip)
        application.showToast(application.getString(R.string.copy_success))
    }

    fun openChat(chatid: Int) {
        if (_uiState.value.connected) {
            webSocket.getProperties(GetPropertiesRequest(
                chatid = chatid
            ))
            webSocket.getMessages(GetMessagesRequest(
                chatid = chatid,
                offset = 0
            ))
            _uiState.update {
                it.copy(
                    pageType = PageType.Chat,
                    currentChat = chatid,
                    backgroundWork = it.backgroundWork!! + 2
                )
            }
        }
    }

    fun openChatProperties() {
        _uiState.update {
            it.copy(
                pageType = PageType.ChatProperties,
            )
        }
    }

    fun returnToChat() {
        _uiState.update {
            it.copy(
                pageType = PageType.Chat,
            )
        }
    }

    fun openSettings() {
        _uiState.update {
            it.copy(
                pageType = PageType.Settings
            )
        }
    }

    fun openAlerts() {
        if (_uiState.value.connected) {
            webSocket.getAlerts()
            _uiState.update {
                it.copy(
                    pageType = PageType.Alerts,
                    backgroundWork = it.backgroundWork!! + 1
                )
            }
        }
    }
    
    fun openSearchTab() {
        _uiState.update { 
            it.copy(
                pageType = PageType.Search
            )
        }
    }
    
    fun returnToChats() {
        _uiState.update {
            it.copy(
                pageType = PageType.Chats,
                currentChat = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocket.disconnect()
    }
}