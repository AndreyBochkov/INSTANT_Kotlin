package com.instanttechnologies.instant.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instanttechnologies.instant.R

val messageLeftBoxShape = RoundedCornerShape(
    topEnd = 5.dp,
    bottomEnd = 5.dp
)
val messageRightBoxShape = RoundedCornerShape(
    topStart = 5.dp,
    bottomStart = 5.dp
)
val messageShape = RoundedCornerShape(5.dp)

@Composable
fun LayoutText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style
    )
}

@Composable
fun LayoutButton(
    text: String,
    modifier: Modifier = Modifier,
    event: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = RoundedCornerShape(5.dp),
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Button(
        onClick = event,
        modifier = modifier,
        colors = ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
            disabledContentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        enabled = enabled,
        shape = shape
    ) {
        LayoutText(
            text,
            style = style,
            color = if (enabled) contentColor else MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun INSTANTPageColumn(
    modifier: Modifier = Modifier,
    verticalPadding: Dp = dimensionResource(R.dimen.padding),
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        contentPadding = PaddingValues(
            vertical = dimensionResource(R.dimen.padding)
        ),
        verticalArrangement = Arrangement.spacedBy(verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun INSTANTPrompt(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    actionVisible: Boolean,
    actionLabel: String,
    action: () -> Unit
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small_padding))
    ) {
        TextField(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                LayoutText(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            shape = messageLeftBoxShape
        )
        AnimatedVisibility(
            visible = actionVisible,
        ) {
            LayoutButton(
                modifier = Modifier.fillMaxHeight(),
                text = actionLabel,
                event = action,
                shape = messageRightBoxShape,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun INSTANTIcon(
    modifier: Modifier = Modifier
) {
    Image(
        imageVector = ImageVector.vectorResource(R.drawable.instant_icon),
        contentDescription = stringResource(R.string.app_name),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .padding(horizontal = dimensionResource(R.dimen.padding)*5),
        colorFilter = ColorFilter.lighting(multiply = Color.Unspecified, add = MaterialTheme.colorScheme.onSurface)
    )
}