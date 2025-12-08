package com.instanttechnologies.instant.ui.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.instanttechnologies.instant.data.ChatProperties
import com.instanttechnologies.instant.data.User
import com.instanttechnologies.instant.utils.INSTANTPageColumn
import com.instanttechnologies.instant.utils.LayoutText

@Composable
fun INSTANTChatPropertiesPage(
    modifier: Modifier = Modifier,
    chatProperties: ChatProperties,
    returnToChat: () -> Unit
) {
    BackHandler {
        returnToChat()
    }
    INSTANTPageColumn (
        modifier = modifier,
        verticalPadding = 0.dp
    ) {
        item {
            LayoutText(
                stringResource(R.string.chatproperties_label, chatProperties.chatid) + chatProperties.label,
                style = MaterialTheme.typography.headlineSmall
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding)))
            LayoutText(
                text = stringResource(R.string.admins_label),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        items(chatProperties.admins) {
            Column {
                HorizontalDivider()
                LayoutText(
                    it.login,
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.padding),
                        alignment = Alignment.End
                    )
                ) {
                    LayoutText(
                        stringResource(R.string.user_label, it.userid),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding)))
            LayoutText(
                text = stringResource(R.string.listeners_label),
                style = MaterialTheme.typography.headlineSmall
            )
            if (chatProperties.listeners.isEmpty()) {
                HorizontalDivider()
                LayoutText(stringResource(R.string.no_people_label))
                HorizontalDivider()
            }
        }
        items(chatProperties.listeners) {
            Column {
                HorizontalDivider()
                LayoutText(
                    it.login,
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.padding),
                        alignment = Alignment.End
                    )
                ) {
                    LayoutText(
                        stringResource(R.string.user_label, it.userid),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item { HorizontalDivider() }
    }
}

@Composable
@Preview(showBackground = true, locale = "ru")
fun ChatPropertiesPreview() {
    INSTANTChatPropertiesPage(
        chatProperties = ChatProperties(
            chatid = 0,
            label = "dummy_chat_0000",
            cansend = true,
            admins = listOf(
                User(
                    userid = 1,
                    login = "dummy_0000"
                ),
                User(
                    userid = 2,
                    login = "dummy_0001"
                ),
                User(
                    userid = 3,
                    login = "dummy_0002"
                )
            ),
            listeners = listOf(
                User(
                    userid = 1,
                    login = "dummy_0000"
                ),
                User(
                    userid = 2,
                    login = "dummy_0001"
                ),
                User(
                    userid = 3,
                    login = "dummy_0002"
                )
            )
        ),
        returnToChat = {}
    )
}