package com.instanttechnologies.instant.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.data.Chat
import com.instanttechnologies.instant.utils.INSTANTPageColumn
import com.instanttechnologies.instant.utils.LayoutButton
import com.instanttechnologies.instant.utils.LayoutText

@Composable
fun INSTANTChatsPage(
    chats: List<Chat>,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
    onOpenChat: (Int) -> Unit,
    onSearchRequest: () -> Unit
) {
    INSTANTPageColumn(
        modifier = modifier,
        verticalPadding = 0.dp
    ) {
        item {
            LayoutText(
                stringResource(R.string.all_chats_label),
                style = MaterialTheme.typography.headlineSmall
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding))
            )
        }
        items(chats) {
            Column(
                modifier = Modifier
                    .clickable {
                        onOpenChat(it.chatid)
                    }
            ) {
                LayoutText(
                    it.label,
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding), alignment = Alignment.End)
                ) {
                    LayoutText(stringResource(R.string.chat_label, it.chatid), style = MaterialTheme.typography.bodySmall)
                    if (it.cansend) {
                        LayoutText(stringResource(R.string.you_are_admin_label), style = MaterialTheme.typography.bodySmall)
                    } else {
                        LayoutText(stringResource(R.string.you_are_listener_label), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            HorizontalDivider()
        }

        item {
            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(top = dimensionResource(R.dimen.padding)),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column (
                    horizontalAlignment = Alignment.End
                ) {
                    LayoutText(stringResource(R.string.search_for_users_label), style = MaterialTheme.typography.bodyLarge)
                    LayoutButton(
                        text = stringResource(R.string.new_chat_label),
                        event = onSearchRequest,
                        enabled = isConnected,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            LayoutText(
                stringResource(R.string.chats_hint),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
@Preview(showBackground = true, locale = "ru")
fun ChatsPreview() {
    INSTANTChatsPage(
        chats = listOf(
            Chat(
                chatid = 0,
                label = "dummy_0000",
                cansend = false
            ),
            Chat(
                chatid = 1,
                label = "dummy_0001",
                cansend = true
            ),
            Chat(
                chatid = 2,
                label = "dummy_0002",
                cansend = true
            )
        ),
        isConnected = true,
        onOpenChat = {},
        onSearchRequest = {}
    )
}