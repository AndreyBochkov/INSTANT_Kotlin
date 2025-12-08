package com.instanttechnologies.instant.ui.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.utils.DateTimeConverter
import com.instanttechnologies.instant.utils.INSTANTPageColumn
import com.instanttechnologies.instant.utils.INSTANTPrompt
import com.instanttechnologies.instant.utils.LayoutButton
import com.instanttechnologies.instant.utils.LayoutText

@Composable
fun INSTANTSettingsPage(
    modifier: Modifier = Modifier,
    initialDateTime: Pair<String, String>,
    onChangeDateTime: (String, String) -> Unit,
    returnToChats: () -> Unit,
    onResetAddress: (String) -> Unit
) {
    BackHandler {
        returnToChats()
    }
    var date by rememberSaveable { mutableStateOf(initialDateTime.first) }
    val defaultDate = stringResource(R.string.dateDefault)
    var time by rememberSaveable { mutableStateOf(initialDateTime.second) }
    val defaultTime = stringResource(R.string.timeDefault)
    var canSetDateTime by rememberSaveable { mutableStateOf(false) }

    var address by rememberSaveable { mutableStateOf("") }
    val defaultAddress = stringResource(R.string.address)
    var canSetAddress by rememberSaveable { mutableStateOf(false) }

    INSTANTPageColumn(
        modifier = modifier
    ) {
        item {
            LayoutText(
                stringResource(R.string.settings_label),
                style = MaterialTheme.typography.headlineSmall
            )
            HorizontalDivider()
        }
        item {
            LayoutText(stringResource(R.string.datetime_preferences_label), style = MaterialTheme.typography.headlineSmall)
            INSTANTPrompt(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.small_padding)),
                value = date,
                placeholder = stringResource(R.string.date_placeholder),
                onValueChange = {
                    date = if (it.contains("\n")) date else it
                    canSetDateTime = true
                },
                actionVisible = canSetDateTime,
                actionLabel = stringResource(R.string.set_datetime_action),
                action = {
                    onChangeDateTime(date,  time)
                    canSetDateTime = false
                }
            )
            INSTANTPrompt(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.small_padding)),
                value = time,
                placeholder = stringResource(R.string.time_placeholder),
                onValueChange = {
                    time = if (it.contains("\n")) time else it
                    canSetDateTime = true
                },
                actionVisible = canSetDateTime,
                actionLabel = stringResource(R.string.set_datetime_action),
                action = {
                    onChangeDateTime(date, time)
                    canSetDateTime = false
                }
            )
            LayoutButton(
                text = stringResource(R.string.datetime_iso_8601),
                event = {
                    date = defaultDate
                    time = defaultTime
                    canSetDateTime = true
                },
                style = MaterialTheme.typography.bodyMedium
            )
            LayoutButton(
                text = stringResource(R.string.datetime_american),
                event = {
                    date = "MM-dd-yyyy"
                    time = "HH:mm:ss"
                    canSetDateTime = true
                },
                style = MaterialTheme.typography.bodyMedium
            )
            LayoutButton(
                text = stringResource(R.string.datetime_rfc_5322),
                event = {
                    date = "dd.MM.yyyy"
                    time = "HH:mm:ss"
                    canSetDateTime = true
                },
                style = MaterialTheme.typography.bodyMedium
            )
            LayoutText(
                stringResource(
                    R.string.datetime_greeting_text,
                    DateTimeConverter.unixToYMDString(pattern = date, ts = 1756581615),
                    DateTimeConverter.unixToHMSString(pattern = time, ts = 1756581615)
                )
            )
            HorizontalDivider()
        }
        item {
            LayoutText(stringResource(R.string.server_address_label), style = MaterialTheme.typography.headlineSmall)
            INSTANTPrompt(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.small_padding)),
                value = address,
                placeholder = stringResource(R.string.new_address_placeholder),
                onValueChange = { 
                    address = if (it.contains("\n")) address else it
                    canSetAddress = true
                },
                actionVisible = address.isNotEmpty() && address.isNotBlank() && canSetAddress,
                actionLabel = stringResource(R.string.new_address_action),
                action = {
                    onResetAddress(address)
                }
            )
            LayoutButton(
                text = stringResource(R.string.official_instance_label),
                event = {
                    address = defaultAddress
                    canSetAddress = true
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
@Preview(showBackground = true, locale = "ru")
fun SettingsPreview() {
    INSTANTSettingsPage(
        initialDateTime = "" to "",
        onChangeDateTime = {_, _ ->},
        returnToChats = {},
        onResetAddress = {}
    )
}