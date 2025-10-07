package com.widgetegg.widgeteggapp.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@Composable
fun SettingsHeader(
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 25.dp, top = 50.dp)
            .semantics { contentDescription = text },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = text,
            modifier = Modifier.clickable { onClick() }
        )
    }
}

@Composable
fun SettingsSectionItem(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color = MaterialTheme.colorScheme.surfaceBright)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp)
    )
}

@Composable
fun SettingsHeaderAndDescription(
    headerText: String,
    descriptionText: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(text = headerText)
        if (!descriptionText.isNullOrBlank()) {
            Text(
                text = descriptionText,
                fontSize = TextUnit(13f, TextUnitType.Sp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ScrollBottomPadding() {
    Row(
        modifier = Modifier.padding(vertical = 50.dp)
    ) {}
}

fun Modifier.settingsRowModifier() =
    this
        .padding(top = 15.dp, start = 15.dp, end = 25.dp)
        .fillMaxWidth()

fun Modifier.widgetGroupingModifier() =
    this
        .padding(top = 50.dp, start = 20.dp)
        .fillMaxWidth()