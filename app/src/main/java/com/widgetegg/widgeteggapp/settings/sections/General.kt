package com.widgetegg.widgeteggapp.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.widgetegg.widgeteggapp.Routes
import com.widgetegg.widgeteggapp.settings.ScrollBottomPadding
import com.widgetegg.widgeteggapp.settings.SettingsHeader
import com.widgetegg.widgeteggapp.settings.SettingsHeaderAndDescription
import com.widgetegg.widgeteggapp.settings.SettingsViewModel
import com.widgetegg.widgeteggapp.settings.settingsRowModifier
import com.widgetegg.widgeteggapp.settings.widgetGroupingModifier
import data.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.DEFAULT_WIDGET_TEXT_COLOR
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore

@Composable
fun General(navController: NavController) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current

    runBlocking {
        val preferences = PreferencesDatastore(context)
        settingsViewModel.updateWidgetBackgroundColor(preferences.getWidgetBackgroundColor())
        settingsViewModel.updateWidgetTextColor(preferences.getWidgetTextColor())
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsHeader(
            text = "Back to Settings Screen",
            onClick = { navController.navigate(Routes.settingsScreen) }
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "General Settings", fontSize = TextUnit(24f, TextUnitType.Sp))
            WidgetColorsGroup(settingsViewModel)
            ScrollBottomPadding()
        }
    }
}

@Composable
fun WidgetColorsGroup(settingsViewModel: SettingsViewModel) {
    Column(
        modifier = Modifier.widgetGroupingModifier(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Widgets Colors", fontSize = TextUnit(18f, TextUnitType.Sp))
        }
        BackgroundColorPicker(settingsViewModel)
        TextColorPicker(settingsViewModel)
        Button(
            modifier = Modifier
                .padding(top = 15.dp, start = 15.dp),
            onClick = {
                settingsViewModel.updateWidgetBackgroundColor(DEFAULT_WIDGET_BACKGROUND_COLOR)
                settingsViewModel.updateWidgetTextColor(DEFAULT_WIDGET_TEXT_COLOR)
            }
        ) {
            Text(text = "Reset Colors")
        }
    }
}

@Composable
fun BackgroundColorPicker(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier
            .settingsRowModifier()
            .clickable {
                settingsViewModel.updateShowBackgroundColorPickerDialog(true)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsHeaderAndDescription(
            "Widget background color",
            "Does not apply to minimal mission widget.",
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )
        if (settingsViewModel.showBackgroundColorPickerDialog) {
            ColorPickerDialog(
                "Widget Background Color",
                settingsViewModel.widgetBackgroundColor,
                onDismissDialog = { settingsViewModel.updateShowBackgroundColorPickerDialog(false) },
                onColorSelected = { newColor ->
                    settingsViewModel.updateWidgetBackgroundColor(
                        newColor
                    )
                })
        }
        AlphaTile(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color.White, RoundedCornerShape(6.dp)),
            selectedColor = settingsViewModel.widgetBackgroundColor
        )
    }
}

@Composable
fun TextColorPicker(settingsViewModel: SettingsViewModel) {
    Row(
        modifier = Modifier
            .settingsRowModifier()
            .clickable {
                settingsViewModel.updateShowTextColorPickerDialog(true)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsHeaderAndDescription(
            "Widget text color",
            "Applies where text default is white.",
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )
        if (settingsViewModel.showTextColorPickerDialog) {
            ColorPickerDialog(
                "Widget Text Color",
                settingsViewModel.widgetTextColor,
                onDismissDialog = { settingsViewModel.updateShowTextColorPickerDialog(false) },
                onColorSelected = { newColor ->
                    settingsViewModel.updateWidgetTextColor(
                        newColor
                    )
                })
        }
        AlphaTile(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color.White),
            selectedColor = settingsViewModel.widgetTextColor
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorPickerDialog(
    headerText: String,
    initialColor: Color,
    onDismissDialog: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val controller = rememberColorPickerController()
    Dialog(
        onDismissRequest = {
            onColorSelected(controller.selectedColor.value)
            onDismissDialog()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = headerText, fontSize = TextUnit(18f, TextUnitType.Sp))
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                controller = controller,
                initialColor = initialColor,
            )
            AlphaSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp),
                controller = controller,
                initialColor = initialColor,
            )
            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(35.dp),
                controller = controller,
                initialColor = initialColor,
            )
            AlphaTile(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(6.dp)),
                controller = controller
            )
        }
    }
}