package com.instanttechnologies.instant.ui.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.utils.INSTANTIcon
import com.instanttechnologies.instant.utils.INSTANTPageColumn
import com.instanttechnologies.instant.utils.INSTANTPrompt
import com.instanttechnologies.instant.utils.LayoutButton
import com.instanttechnologies.instant.utils.LayoutText

@Composable
fun INSTANTRegisterPage(
    modifier: Modifier = Modifier,
    linkToPrivacyPage: () -> Unit,
    onRegisterRequest: (String) -> Unit,
    isLoading: Boolean,
    isConnected: Boolean,
    errorText: String?
) {
    var login by remember { mutableStateOf("") }
    INSTANTPageColumn(
        modifier = modifier
    ) {
        item {
            INSTANTIcon(
                modifier = Modifier.fillParentMaxWidth()
            )
        }
        item {
            LayoutText(
                stringResource(R.string.greeting_message)
            )
        }
        item {
            LayoutText(
                stringResource(R.string.chats_hint)
            )
        }
        item {
            LayoutButton(
                text = stringResource(R.string.link_to_privacy_page),
                event = linkToPrivacyPage
            )
        }
        if (errorText != null) {
            item {
                LayoutText(
                    stringResource(R.string.error_description) + errorText,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            INSTANTPrompt(
                value = login,
                placeholder = stringResource(R.string.login_placeholder),
                onValueChange = {
                    login = if (it.contains('\n')) login else it
                },
                actionVisible = !isLoading && login.isNotBlank() && login.isNotEmpty() && isConnected,
                actionLabel = stringResource(R.string.register_this_name),
                action = {
                    onRegisterRequest(login)
                }
            )
        }

        item {
            AnimatedVisibility(isLoading) {
                LayoutText(stringResource(R.string.loading_response))
            }
        }
    }
}

@Composable
@Preview(showBackground = true, locale = "ru")
fun RegisterPreview() {
    INSTANTRegisterPage(
        linkToPrivacyPage = {},
        onRegisterRequest = {},
        isLoading = false,
        isConnected = true,
        errorText = "Example error"
    )
}