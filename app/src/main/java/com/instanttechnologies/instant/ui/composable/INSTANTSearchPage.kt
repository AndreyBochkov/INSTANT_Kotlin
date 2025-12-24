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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.data.User
import com.instanttechnologies.instant.utils.INSTANTPageColumn
import com.instanttechnologies.instant.utils.INSTANTPrompt
import com.instanttechnologies.instant.utils.LayoutText

@Composable
fun INSTANTSearchPage(
    modifier: Modifier = Modifier,
    users: List<User>,
    initialAdmins: List<User>,
    initialListeners: List<User>,
    onNewChatRequest: (List<Int>, List<Int>, String) -> Unit,
    onSearchRequest: (String) -> Unit,
    returnToChats: () -> Unit,
    isLoading: Boolean,
    isConnected: Boolean,
) {
    BackHandler {
        returnToChats()
    }
    var query by rememberSaveable { mutableStateOf("") }
    var canSearch by rememberSaveable { mutableStateOf(false) }
    var admins by rememberSaveable { mutableStateOf(initialAdmins) }
    var listeners by rememberSaveable { mutableStateOf(initialListeners) }
    var label by rememberSaveable { mutableStateOf("") }
    var choice by rememberSaveable { mutableIntStateOf(0) }
    INSTANTPageColumn(
        modifier = modifier,
        verticalPadding = 0.dp
    ) {
        item {
            LayoutText(
                stringResource(R.string.search_label),
                style = MaterialTheme.typography.headlineSmall
            )
            INSTANTPrompt(
                value = label,
                placeholder = stringResource(R.string.label_placeholder),
                onValueChange = {
                    label = it
                },
                actionVisible = label.isNotBlank() && label.isNotEmpty() && isConnected,
                actionLabel = stringResource(R.string.new_chat_label),
                action = {
                    onNewChatRequest(admins.map { it.userid }, listeners.map { it.userid }, label)
                    returnToChats()
                }
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
                }
            )
            LayoutText(
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding)),
                text = stringResource(R.string.search_results_label),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        items(users) {
            if (!admins.contains(it) && !listeners.contains(it)) {
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .clickable {
                            choice = it.userid
                        }
                ) {
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
                                LayoutText(stringResource(R.string.add_as_admin_action))
                            },
                            onClick = {
                                admins = admins.plus(it)
                                choice = 0
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                LayoutText(stringResource(R.string.add_as_listener_action))
                            },
                            onClick = {
                                listeners = listeners.plus(it)
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
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding)))
            LayoutText(
                text = stringResource(R.string.admins_label),
                style = MaterialTheme.typography.headlineSmall
            )
            if (admins.isEmpty()) {
                HorizontalDivider()
                LayoutText(stringResource(R.string.add_people_label))
            }
        }
        items(admins) {
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
                                text = stringResource(R.string.remove_admin_action),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        },
                        onClick = {
                            admins = admins.minus(it)
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
                text = stringResource(R.string.listeners_label),
                style = MaterialTheme.typography.headlineSmall
            )
            if (listeners.isEmpty()) {
                HorizontalDivider()
                LayoutText(stringResource(R.string.add_people_label))
                HorizontalDivider()
            }
        }
        items(listeners) {
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
                                text = stringResource(R.string.remove_listener_action),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        },
                        onClick = {
                            listeners = listeners.minus(it)
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
}

@Composable
@Preview(showBackground = true, device = Devices.PHONE)
fun SearchPreview() {
    INSTANTSearchPage(
        users = listOf(
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
        initialAdmins = listOf(
            User(
                userid = 4,
                login = "dummy_0004"
            ),
            User(
                userid = 5,
                login = "dummy_0005"
            ),
            User(
                userid = 6,
                login = "dummy_0006"
            )
        ),
        initialListeners = listOf(
            User(
                userid = 7,
                login = "dummy_0007"
            ),
            User(
                userid = 8,
                login = "dummy_0008"
            ),
            User(
                userid = 9,
                login = "dummy_0009"
            )
        ),
        onNewChatRequest = {_, _, _ -> },
        onSearchRequest = {},
        returnToChats = {},
        isLoading = false,
        isConnected = true,
    )
}