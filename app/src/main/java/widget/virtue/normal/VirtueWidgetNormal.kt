package widget.virtue.normal

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.constants.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.constants.DEFAULT_WIDGET_TEXT_COLOR
import data.VirtueInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.bitmapResize
import tools.utilities.getAsset
import tools.utilities.getEggName
import tools.utilities.getRemainingSiloTime
import widget.WidgetUpdater
import widget.shared.NoWidgetContent
import widget.virtue.VirtueWidgetDataStore
import widget.virtue.VirtueWidgetDataStorePreferencesKeys

class VirtueWidgetNormal : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val eid = state[VirtueWidgetDataStorePreferencesKeys.EID] ?: ""
            val virtueInfo = VirtueWidgetDataStore().decodeVirtueInfo(
                state[VirtueWidgetDataStorePreferencesKeys.VIRTUE_INFO] ?: ""
            )
            val backgroundColor =
                state[VirtueWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR]?.let { colorInt ->
                    Color(colorInt)
                } ?: DEFAULT_WIDGET_BACKGROUND_COLOR
            val textColor =
                state[VirtueWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR]?.let { colorInt ->
                    Color(colorInt)
                } ?: DEFAULT_WIDGET_TEXT_COLOR

            if (eid.isBlank() || virtueInfo.stateId.isBlank()) {
                // If EID is blank, could either mean state is not initialized or user is not logged in
                // Attempt to load state in case it is needed, otherwise login composable will show
                LaunchedEffect(true) {
                    CoroutineScope(context = Dispatchers.IO).launch {
                        try {
                            WidgetUpdater().updateWidgets(context)
                        } catch (_: Exception) {
                        }
                    }
                }
            }

            val scope = rememberCoroutineScope()
            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .clickable {
                        scope.launch {
                            try {
                                WidgetUpdater().updateWidgets(context)
                            } catch (_: Exception) {
                            }
                        }
                    }
            ) {
                val assetManager = context.assets
                if (eid.isBlank()) {
                    NoWidgetContent(assetManager, "Waiting for virtue data...", 80.dp, textColor)
                } else {
                    HomeFarmInfo(assetManager, virtueInfo, textColor)
                    ShiftInfo(assetManager, virtueInfo, textColor)
                    FarmProgress(assetManager, virtueInfo, textColor)
                }
            }
        }
    }
}

@Composable
fun ShiftInfo(
    assetManager: AssetManager,
    virtueInfo: VirtueInfo,
    textColor: Color
) {
    if (true) {
//            if (virtueInfo.isOnVirtue) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val truthEggBitmap = bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "eggs/egg_truth.png"
                    )
                )
            )
            Image(
                provider = ImageProvider(truthEggBitmap),
                contentDescription = "Truth Egg",
                modifier = GlanceModifier.size(20.dp).padding(end = 2.dp)
            )
            Text(
                text = "${virtueInfo.totalTruthEggs} (${virtueInfo.totalPendingTruthEggs}) ",
                style = TextStyle(color = ColorProvider(textColor))
            )
            Box(modifier = GlanceModifier.defaultWeight()) {}
            Text(
                text = "Shifts: ${virtueInfo.shifts}",
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = GlanceModifier,
                text = "Next shift: ${virtueInfo.nextShiftCost}",
                style = TextStyle(color = ColorProvider(textColor))
            )
            val soulEggBitmap = bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "eggs/egg_soul.png"
                    )
                )
            )
            Image(
                provider = ImageProvider(soulEggBitmap),
                contentDescription = "Soul Egg",
                modifier = GlanceModifier.size(20.dp).padding(start = 2.dp)
            )
        }
    } else {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Not on Virtue",
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
    }
}

@Composable
fun HomeFarmInfo(
    assetManager: AssetManager,
    virtueInfo: VirtueInfo,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val eggName = getEggName(virtueInfo.eggId)
        val eggBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "eggs/$eggName.png"
                )
            )
        )
        Image(
            provider = ImageProvider(eggBitmap),
            contentDescription = "Home Egg",
            modifier = GlanceModifier.size(20.dp)
        )
        if (virtueInfo.isOnVirtue) {
//            val virtueFunctionPath = getVirtueFunctionIconPath(virtueInfo.eggId)
            val virtueFunctionBitmap = bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "eggs/$eggName.png"
                    )
                )
            )
            Image(
                provider = ImageProvider(eggBitmap),
                contentDescription = "Home Egg",
                modifier = GlanceModifier.size(20.dp)
            )
        }

        val siloBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_silos.png"
                )
            )
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Image(
            provider = ImageProvider(siloBitmap),
            contentDescription = "Silos",
            modifier = GlanceModifier.size(20.dp).padding(end = 2.dp)
        )
        val siloTimeRemaining =
            getRemainingSiloTime(virtueInfo.lastBackupDate, virtueInfo.maximumOfflineTime)
        Text(
            text = siloTimeRemaining,
            style = TextStyle(color = ColorProvider(textColor))
        )
    }
}

//@Composable
//fun Artifacts(
//    assetManager: AssetManager,
//    virtueInfo: VirtueInfo
//) {
//    val artifacts = if (virtueInfo.isOnVirtue) {
//        virtueInfo.virtueEquippedArtifacts
//    } else {
//        virtueInfo.homeEquippedArtifacts
//    }
//    if (artifacts.isNotEmpty()) {
//        Row(
//            modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp),
//            horizontalAlignment = Alignment.Start,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            ArtifactsContent(assetManager, artifacts)
//        }
//    }
//}

@Composable
fun FarmProgress(
    assetManager: AssetManager,
    virtueInfo: VirtueInfo,
    textColor: Color
) {
    virtueInfo.farms.forEach { farm ->
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val eggName = getEggName(farm.eggId)
            val eggBitmap = bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "eggs/$eggName.png"
                    )
                )
            )
            Image(
                provider = ImageProvider(eggBitmap),
                contentDescription = eggName,
                modifier = GlanceModifier.size(20.dp).padding(end = 2.dp)
            )
            Text(
                text = "${farm.truthEggs} (${farm.pendingTruthEggs})",
                style = TextStyle(color = ColorProvider(textColor))
            )
            Box(modifier = GlanceModifier.defaultWeight()) {}
            Text(
                text = "Sample date",
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
    }
}