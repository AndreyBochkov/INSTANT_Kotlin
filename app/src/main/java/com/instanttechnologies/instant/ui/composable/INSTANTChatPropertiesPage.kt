package com.instanttechnologies.instant.ui.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.data.ChatProperties
import com.instanttechnologies.instant.data.User
import com.instanttechnologies.instant.utils.INSTANTPageColumn
import com.instanttechnologies.instant.utils.INSTANTPrompt
import com.instanttechnologies.instant.utils.LayoutText

@Composable
fun INSTANTChatPropertiesPage(
    modifier: Modifier = Modifier,
    chatProperties: ChatProperties,
    returnToChat: () -> Unit,
    onSearchRequest: (String) -> Unit,
    isConnected: Boolean,
    users: List<User>,
    onAddTieRequest: (Int, Int, Boolean) -> Unit,
    ondDeleteTieRequest: (Int, Int) -> Unit,
    isLoading: Boolean,
    me: Int
) {
    BackHandler {
        returnToChat()
    }
    if (chatProperties.cansend) {var query by rememberSaveable { mutableStateOf("") }
        var canSearch by rememberSaveable { mutableStateOf(false) }
        var choice by rememberSaveable { mutableIntStateOf(0) }
        INSTANTPageColumn (
            modifier = modifier,
            verticalPadding = 0.dp
        ) {
            item {
                LayoutText(
                    stringResource(
                        R.string.chatproperties_label,
                        chatProperties.chatid
                    ) + chatProperties.label,
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
                Column(
                    modifier = Modifier
                        .clickable {
                            choice = it.userid
                        }
                ) {
                    HorizontalDivider()
                    LayoutText(
                        it.login,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    DropdownMenu(
                        expanded = choice == it.userid,
                        onDismissRequest = {
                            choice = 0
                        }
                    ) {
                        if (it.userid == me) {
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.delete_self_action),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    ondDeleteTieRequest(me, chatProperties.chatid)
                                    choice = 0
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.remove_admin_action),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    ondDeleteTieRequest(it.userid, chatProperties.chatid)
                                    choice = 0
                                }
                            )
                        }
                    }
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
                }
            }

            items(chatProperties.listeners) {
                Column(
                    modifier = Modifier
                        .clickable {
                            choice = it.userid
                        }
                ) {
                    HorizontalDivider()
                    LayoutText(
                        it.login,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    DropdownMenu(
                        expanded = choice == it.userid,
                        onDismissRequest = {
                            choice = 0
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                LayoutText(
                                    stringResource(R.string.remove_listener_action),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                ondDeleteTieRequest(it.userid, chatProperties.chatid)
                                choice = 0
                            }
                        )
                    }
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
                    stringResource(R.string.add_tie_label),
                    style = MaterialTheme.typography.headlineSmall
                )
                INSTANTPrompt(
                    modifier = Modifier
                        .padding(top = dimensionResource(R.dimen.small_padding)),
                    value = query,
                    placeholder = stringResource(R.string.query_placeholder),
                    onValueChange = {
                        query = it
                        canSearch = true
                    },
                    actionVisible = query.isNotEmpty() && query.isNotBlank() && canSearch && isConnected,
                    actionLabel = stringResource(R.string.search_label),
                    action = {
                        onSearchRequest(query)
                        canSearch = false
                    },
                    capitalization = KeyboardCapitalization.Unspecified
                )
                Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding)))
                if (users.isEmpty()) {
                    if (isLoading) {
                        LayoutText(stringResource(R.string.loading_label))
                    } else {
                        LayoutText(
                            stringResource(R.string.empty_search_results_label),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            items(users) {
                if (!chatProperties.admins.contains(it) && !chatProperties.listeners.contains(it)) {
                    HorizontalDivider()
                    Column(
                        modifier = Modifier
                            .clickable {
                                choice = it.userid
                            }
                    ) {
                        HorizontalDivider()
                        LayoutText(
                            it.login,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        DropdownMenu(
                            expanded = choice == it.userid,
                            onDismissRequest = {
                                choice = 0
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.add_as_admin_action)
                                    )
                                },
                                onClick = {
                                    onAddTieRequest(it.userid, chatProperties.chatid, true)
                                    choice = 0
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.add_as_listener_action)
                                    )
                                },
                                onClick = {
                                    onAddTieRequest(it.userid, chatProperties.chatid, false)
                                    choice = 0
                                }
                            )
                        }
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
            }
            item {
                HorizontalDivider()
            }
        }
    } else { // я - слушатель
        INSTANTPageColumn (
            modifier = modifier,
            verticalPadding = 0.dp
        ) {
            item {
                LayoutText(
                    stringResource(
                        R.string.chatproperties_label,
                        chatProperties.chatid
                    ) + chatProperties.label,
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
                HorizontalDivider()
                LayoutText(stringResource(R.string.access_denied_124))
                HorizontalDivider()
            }
        }
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
        returnToChat = {},
        isConnected = true,
        onAddTieRequest = {_, _, _ -> },
        ondDeleteTieRequest = {_, _ -> },
        onSearchRequest = {},
        users = emptyList(),
        isLoading = false,
        me = 2
    )
}