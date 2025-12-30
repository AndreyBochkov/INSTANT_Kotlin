package com.instanttechnologies.instant.ui.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.data.ChatProperties
import com.instanttechnologies.instant.data.Message
import com.instanttechnologies.instant.data.User
import com.instanttechnologies.instant.utils.DateTimeConverter
import com.instanttechnologies.instant.utils.INSTANTPrompt
import com.instanttechnologies.instant.utils.LayoutButton
import com.instanttechnologies.instant.utils.LayoutText
import com.instanttechnologies.instant.utils.messageShape

@Composable
fun INSTANTChatPage(
    modifier: Modifier = Modifier,
    onSendMessageRequest: (String) -> Unit,
    messages: List<Message>,
    returnToChats: () -> Unit,
    isLoading: Boolean,
    isConnected: Boolean,
    onGetMessagesRequest: (Int) -> Unit,
    chat: ChatProperties,
    me: Int
) {
    BackHandler {
        returnToChats()
    }
    var message by remember { mutableStateOf("") }
    var offset by remember { mutableIntStateOf(0) }
    val chatState = rememberLazyListState()
    Column (
        modifier = modifier
    ) {
        LayoutText(
            stringResource(R.string.chat_number, chat.chatid) + chat.label,
            style = MaterialTheme.typography.headlineSmall
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = chatState,
            contentPadding = PaddingValues(
                start = dimensionResource(R.dimen.padding),
                top = dimensionResource(R.dimen.padding),
                end = dimensionResource(R.dimen.padding)
            ),
            verticalArrangement = Arrangement.Bottom,
        ) {
            item {
                LayoutButton(
                    text = if (isLoading) stringResource(R.string.loading_label) else stringResource(
                        R.string.load_more_label
                    ),
                    event = {
                        offset++
                        println(offset)
                        onGetMessagesRequest(offset)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    enabled = !isLoading && isConnected
                )
            }
            items(messages) {message ->
                if (message.ts.div(86400) != (
                            messages.getOrNull(messages.indexOf(message) - 1)?.ts ?: 0 // TODO: оптимизировать этот кусок лучшего кода
                        ).div(86400)
                ) {
                    Row (
                        modifier = Modifier.fillParentMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LayoutText(
                            DateTimeConverter.unixToYMDString(ts = message.ts),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.small_padding) / 2),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                            .background(
                                color = if (message.sender == me) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.secondary,
                                shape = messageShape
                            )
                            .padding(dimensionResource(R.dimen.small_padding)),
                        horizontalAlignment = Alignment.Start
                    ) {
                        LayoutText(
                            text = "${chat.admins.firstOrNull { it.userid == message.sender }?.login?:"???"}, ${stringResource(R.string.user_label, message.sender)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.sender == me) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        )
                        LayoutText(
                            text = message.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (message.sender == me) MaterialTheme.colorScheme.onTertiary
                            else MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Column(
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.small_padding)),
                        horizontalAlignment = Alignment.Start
                    ) {
                        LayoutText(
                            text = DateTimeConverter.unixToHMSString(ts = message.ts),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        LayoutText(
                            text = stringResource(R.string.message_label, message.messageid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
        if (chat.cansend) {
            INSTANTPrompt(
                value = message,
                placeholder = stringResource(R.string.message_placeholder),
                onValueChange = {
                    message = it
                },
                actionVisible = message.isNotEmpty() && message.isNotBlank() && isConnected,
                actionLabel = stringResource(R.string.send_label),
                action = {
                    onSendMessageRequest(message)
                    message = ""
                },
            )
        } else {
            Spacer(modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding)))
        }
    }
    LaunchedEffect(messages) {
        chatState.scrollToItem(chatState.layoutInfo.totalItemsCount)
    }
}

@Composable
@Preview(showBackground = true, device = Devices.PHONE, locale = "ru")
fun ChatPreview() {
    INSTANTChatPage(
        onSendMessageRequest = {},
        messages = listOf(
                Message(
                    messageid = 0,
                    body = "Hello from user 0",
                    ts = 1753700000,
                    sender = 0
                ),
                Message(
                    messageid = 1,
                    body = "Some every long message when user 1 is describing something useful in order to break this margin system",
                    ts = 1753799999,
                    sender = 1
                ),
                Message(
                    messageid = 2,
                    body = "well now user 0 is responding",
                    ts = 1753799999,
                    sender = 0
                ),
                Message(
                    messageid = 3,
                    body = "something useful negotiated",
                    ts = 1754000000,
                    sender = 2
                ),
            ),
        returnToChats = {},
        isLoading = true,
        isConnected = true,
        onGetMessagesRequest = {},
        chat = ChatProperties(
            chatid = 0,
            label = "dummy_chat",
            cansend = true,
            admins = listOf(
                    User(
                        userid = 0,
                        login = "John Doe"
                    ),
                    User(
                        userid = 1,
                        login = "Alice Brown"
                    ),
                    User(
                        userid = 2,
                        login = "creative_name_3000"
                    ),
                ),
            listeners = emptyList()
        ),
        me = 0
    )
}