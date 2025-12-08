package com.instanttechnologies.instant.network

import com.instanttechnologies.instant.data.Alert
import com.instanttechnologies.instant.data.Chat
import com.instanttechnologies.instant.data.GetMessagesResponse
import com.instanttechnologies.instant.data.GetPropertiesResponse
import com.instanttechnologies.instant.data.SyncMessage
import com.instanttechnologies.instant.data.User

sealed class INSTANTWSMessage {
    data class Ready(val registered: Boolean, val requestID: String) : INSTANTWSMessage()
    data object NotReady : INSTANTWSMessage()
    data object Register : INSTANTWSMessage()
    data class GetChats(val resp: List<Chat>) : INSTANTWSMessage()
    data class Search(val resp: List<User>) : INSTANTWSMessage()
    data class GetProperties(val resp: GetPropertiesResponse) : INSTANTWSMessage()
    data class NewChat(val resp: Chat) : INSTANTWSMessage()
    data class GetMessages(val resp: GetMessagesResponse) : INSTANTWSMessage()
    data class SendMessage(val resp: SyncMessage) : INSTANTWSMessage()

    data class GetAlerts(val resp: List<Alert>) : INSTANTWSMessage()
    data object ChangeIKey : INSTANTWSMessage()

    data class FatalError(val message: String) : INSTANTWSMessage()
    data object EmptyCredentialsError : INSTANTWSMessage()
    data object DuplicatedLoginError : INSTANTWSMessage()
    data object AccessDeniedError : INSTANTWSMessage()

    data class UnspecifiedTypeError(val type: Int) : INSTANTWSMessage()
}