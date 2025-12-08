package com.instanttechnologies.instant.ui.composable

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instanttechnologies.instant.R
import com.instanttechnologies.instant.utils.INSTANTIcon
import com.instanttechnologies.instant.utils.INSTANTPageColumn
import com.instanttechnologies.instant.utils.LayoutButton
import com.instanttechnologies.instant.utils.LayoutText

@Composable
fun INSTANTErrorPage(
    modifier: Modifier = Modifier,
    errorText: String,
    copyReqId: () -> Unit,
    onOpenSite: () -> Unit,
) {
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
                stringResource(R.string.fatal),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            LayoutText(
                stringResource(R.string.error_description) + errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            LayoutButton(
                "1. " + stringResource(R.string.copy_request_id_label),
                event = copyReqId
            )
        }
        item {
            LayoutButton(
                "2. " + stringResource(R.string.open_official_site_label),
                event = onOpenSite
            )
        }
    }
}

@Composable
@Preview(showBackground = true, locale = "ru")
fun ErrorPreview() {
    INSTANTErrorPage(
        errorText = "This is an error message",
        copyReqId = {},
        onOpenSite = {}
    )
}