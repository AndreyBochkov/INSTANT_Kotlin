package com.instanttechnologies.instant.ui.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.data.PageType
import com.instanttechnologies.instant.utils.LayoutText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun INSTANTTopAppBar(
    modifier: Modifier = Modifier,
    name: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onChangeIKeyRequest: () -> Unit,
    onCopyReqID: () -> Unit,
    onGoToSettings: () -> Unit,
    onGoToAlerts: () -> Unit,
    onReturnToChats: () -> Unit,
    onReturnToChat: () -> Unit,
    onGoToProperties: () -> Unit,
    easterEgg: () -> Unit,
    easterEggParameter: Boolean,
    easterEggVal: Int,
    pageType: PageType
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            LayoutText(
                name.ifEmpty { stringResource(R.string.app_name) },
                style = MaterialTheme.typography.headlineSmall
            )
        },
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            IconButton(onClick = easterEgg) {
                AnimatedVisibility(easterEggParameter) {
                    LayoutText("$easterEggVal")
                }
                AnimatedVisibility(!easterEggParameter) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.instant_icon),
                        contentDescription = stringResource(R.string.app_name)
                    )
                }
            }
        },
        actions = {
            AnimatedVisibility(pageType != PageType.Register) {
                DropdownMenu(
                    expanded = actionsExpanded,
                    onDismissRequest = {
                        actionsExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            LayoutText(
                                stringResource(R.string.reset_my_identity_key_label),
                            )
                        },
                        onClick = {
                            actionsExpanded = false
                            onChangeIKeyRequest()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            LayoutText(
                                stringResource(R.string.copy_request_id_label),
                            )
                        },
                        onClick = {
                            actionsExpanded = false
                            onCopyReqID()
                        }
                    )
                    when (pageType) {
                        PageType.Error -> {
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.settings_label)
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onGoToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.return_to_chats_label)
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onReturnToChats()
                                }
                            )
                        }
                        PageType.Chats -> {
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.settings_label)
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onGoToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.go_to_alerts_label)
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onGoToAlerts()
                                }
                            )
                        }
                        PageType.Chat -> {
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.view_properties_label)
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onGoToProperties()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.return_to_chats_label)
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onReturnToChats()
                                }
                            )
                        }
                        PageType.ChatProperties -> {
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.return_to_chat_label)
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onReturnToChat()
                                }
                            )
                        }
                        else -> {
                            DropdownMenuItem(
                                text = {
                                    LayoutText(
                                        stringResource(R.string.return_to_chats_label)
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onReturnToChats()
                                }
                            )
                        }
                    }
                }
                IconButton(
                    onClick = {
                        actionsExpanded = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Options"
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TopBarPreview() {
    INSTANTTopAppBar(
        name = "INSTANT",
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        onChangeIKeyRequest = {},
        onCopyReqID = {},
        onGoToSettings = {},
        onGoToAlerts = {},
        onReturnToChats = {},
        easterEgg = {},
        onReturnToChat = {},
        onGoToProperties = {},
        easterEggParameter = false,
        easterEggVal = 0,
        pageType = PageType.Chats
    )
}