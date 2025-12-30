package com.instanttechnologies.instant.ui.composable

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.data.Chat
import com.instanttechnologies.instant.data.PageType
import com.instanttechnologies.instant.utils.DateTimeConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun INSTANTApp(
    context: Context,
    modifier: Modifier = Modifier,
    viewModel: INSTANTViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    var easterEggVal by remember { mutableIntStateOf(0) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            INSTANTTopAppBar(
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                name = if (uiState.connected) uiState.name else stringResource(R.string.offline_label),
                modifier = modifier,
                onChangeIKeyRequest = {
                    viewModel.changeIKey()
                },
                onCopyReqID = {
                    viewModel.copyRequestID()
                },
                onGoToSettings = {
                    viewModel.openSettings()
                },
                onGoToAlerts = {
                    viewModel.openAlerts()
                },
                onReturnToChats = {
                    viewModel.returnToChats()
                },
                onGoToProperties = {
                    viewModel.openChatProperties()
                },
                onReturnToChat = {
                    viewModel.returnToChat()
                },

                easterEgg = { easterEggVal = (easterEggVal + 1) % 10 },
                easterEggParameter = easterEggVal >= 5,
                easterEggVal = uiState.backgroundWork?:-1,
                pageType = uiState.pageType
            )

            val pageModifier = modifier
                .weight(1f)
                .padding(horizontal = dimensionResource(R.dimen.padding))

            Row (
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                ) {
                    when (uiState.pageType) {
                        PageType.Register -> INSTANTRegisterPage(
                            modifier = pageModifier,
                            linkToPrivacyPage = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "http://instant-messenger.ru/privacy".toUri()
                                    )
                                )
                            },
                            onRegisterRequest = { login ->
                                viewModel.register(login)
                            },
                            isLoading = uiState.backgroundWork!! > 0,
                            isConnected = uiState.connected,
                            errorText = uiState.errorText
                        )

                        PageType.Chats -> INSTANTChatsPage(
                            chats = uiState.chats.map { Chat(it.chatid, it.label, it.cansend) },
                            isConnected = uiState.connected,
                            onOpenChat = {
                                viewModel.openChat(it)
                            },
                            onSearchRequest = {
                                viewModel.openSearchTab()
                            }
                        )

                        PageType.Chat -> {
                            val chatProperties = uiState.chats.first { it.chatid == uiState.currentChat }
                            INSTANTChatPage(
                                messages = uiState.messages[uiState.currentChat] ?: emptyList(),
                                onSendMessageRequest = {
                                    viewModel.sendMessage(it)
                                },
                                returnToChats = {
                                    viewModel.returnToChats()
                                },
                                isLoading = uiState.backgroundWork != 0,
                                isConnected = uiState.connected,
                                onGetMessagesRequest = { offset ->
                                    viewModel.getMessages(offset)
                                },
                                chat = chatProperties,
                                me = uiState.id
                            )
                        }

                        PageType.Search -> INSTANTSearchPage(
                            modifier = pageModifier,
                            users = uiState.users,
                            initialAdmins = emptyList(),
                            initialListeners = emptyList(),
                            onNewChatRequest = { admins, listeners, label ->
                                viewModel.newChat(admins, listeners, label)
                            },
                            onSearchRequest = {
                                viewModel.search(it)
                            },
                            returnToChats = {
                                viewModel.returnToChats()
                            },
                            isLoading = uiState.backgroundWork!! > 0,
                            isConnected = uiState.connected
                        )

                        PageType.Error -> INSTANTErrorPage(
                            modifier = pageModifier,
                            errorText = uiState.errorText
                                ?: stringResource(R.string.unlabeled_error),
                            copyReqId = {
                                viewModel.copyRequestID()
                            },
                            onOpenSite = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "http://instant-messenger.ru/index".toUri()
                                    )
                                )
                            }
                        )

                        PageType.Settings -> INSTANTSettingsPage(
                            modifier = pageModifier,
                            initialDateTime = DateTimeConverter.dateTime,
                            onChangeDateTime = {date, time ->
                                viewModel.saveDateTime(date, time)
                            },
                            returnToChats = {
                                viewModel.returnToChats()
                            },
                            onResetAddress = {
                                viewModel.resetAddress(it)
                            }
                        )

                        PageType.Alerts -> INSTANTAlertsPage(
                            modifier = pageModifier,
                            alerts = uiState.alerts,
                            returnToChats = {
                                viewModel.returnToChats()
                            }
                        )

                        PageType.ChatProperties -> {
                            INSTANTChatPropertiesPage(
                                modifier = pageModifier,
                                chatProperties = uiState.chats.first { it.chatid == uiState.currentChat },
                                returnToChat = {
                                    viewModel.returnToChat()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}