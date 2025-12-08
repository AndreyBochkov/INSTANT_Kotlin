package com.instanttechnologies.instant.ui.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.instanttechnologies.instant.data.Alert
import com.instanttechnologies.instant.utils.DateTimeConverter
import com.instanttechnologies.instant.utils.INSTANTPageColumn
import com.instanttechnologies.instant.utils.LayoutText
import com.instanttechnologies.instant.utils.messageShape

@Composable
fun INSTANTAlertsPage(
    modifier: Modifier = Modifier,
    alerts: List<Alert>,
    returnToChats: () -> Unit
) {
    BackHandler {
        returnToChats()
    }
    INSTANTPageColumn(
        modifier = modifier,
        verticalPadding = 0.dp
    ) {
        item {
            LayoutText(
                stringResource(R.string.alerts_label),
                style = MaterialTheme.typography.headlineSmall
            )
            HorizontalDivider()
        }
        items(alerts) {alert ->
            Row (
                modifier = Modifier.fillParentMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
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
                            color = MaterialTheme.colorScheme.secondary,
                            shape = messageShape
                        )
                        .padding(dimensionResource(R.dimen.small_padding)),
                    horizontalAlignment = Alignment.Start
                ) {
                    LayoutText(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                    LayoutText(
                        text = alert.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Column(
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.small_padding)),
                    horizontalAlignment = Alignment.Start
                ) {
                    LayoutText(
                        text = DateTimeConverter.unixToYMDString(ts = alert.ts),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    LayoutText(
                        text = DateTimeConverter.unixToHMSString(ts = alert.ts),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    LayoutText(
                        text = stringResource(R.string.alert_label, alert.alertid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        item {
            LayoutText(stringResource(R.string.no_more_alerts_label))
        }
    }
}

@Composable
@Preview(showBackground = true, locale = "ru")
fun AlertsPreview() {
    INSTANTAlertsPage(
        alerts = listOf(
            Alert(
                alertid = 0,
                ts = 123,
                body = "A VERY IMPOTRANT MESASGE YOU'LL GET (!!!) BANNED (!!!) FROM ISNTANT IN (!!!) TWO (!!!) SECNODS!!!"
            ),
            Alert(
                alertid = 1,
                ts = 17989,
                body = "A VERY IMOPTARNT small MESSAGE"
            )
        ),
        returnToChats = {}
    )
}