package widget.virtue.large

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.constants.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.constants.DEFAULT_WIDGET_TEXT_COLOR
import data.VirtueInfo
import data.constants.PROGRESS_BACKGROUND_COLOR
import data.constants.PROGRESS_COLOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.bitmapResize
import tools.utilities.createLinearGradientBitmap
import tools.utilities.getAsset
import tools.utilities.getEggName
import tools.utilities.getEventImage
import tools.utilities.getNextTruthEggThreshold
import tools.utilities.getRemainingSiloTime
import tools.utilities.getTimeRemainingToNextTruthEgg
import tools.utilities.getTruthEggPercentComplete
import tools.utilities.getVirtueFunctionIconPath
import tools.utilities.numberToString
import widget.WidgetUpdater
import widget.shared.ArtifactsContent
import widget.shared.NoWidgetContent
import widget.virtue.VirtueWidgetDataStore
import widget.virtue.VirtueWidgetDataStorePreferencesKeys

class VirtueWidgetLarge : GlanceAppWidget() {
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
                if (eid.isBlank() || virtueInfo.stateId.isBlank()) {
                    NoWidgetContent(assetManager, "Waiting for virtue data...", 80.dp, textColor)
                } else {
                    HomeFarmInfo(assetManager, virtueInfo, textColor)
                    ShiftInfo(assetManager, virtueInfo, textColor)
                    ArtifactsAndEvents(assetManager, virtueInfo)
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
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 8.dp, end = 5.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Shifts: ${virtueInfo.shifts}",
            style = TextStyle(color = ColorProvider(textColor))
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
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
            modifier = GlanceModifier.size(25.dp)
        )
        if (virtueInfo.isOnVirtue) {
            val virtueFunctionPath = getVirtueFunctionIconPath(virtueInfo.eggId)
            val virtueFunctionBitmap = bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "other/$virtueFunctionPath.png"
                    )
                )
            )
            Image(
                provider = ImageProvider(virtueFunctionBitmap),
                contentDescription = "Virtue Egg Function",
                modifier = GlanceModifier.size(25.dp).padding(start = 2.dp)
            )
        }

        Box(modifier = GlanceModifier.defaultWeight()) {}
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

@Composable
fun ArtifactsAndEvents(
    assetManager: AssetManager,
    virtueInfo: VirtueInfo
) {
    val artifacts = if (virtueInfo.isOnVirtue) {
        virtueInfo.virtueEquippedArtifacts
    } else {
        virtueInfo.homeEquippedArtifacts
    }
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (artifacts.isNotEmpty()) {
            ArtifactsContent(assetManager, artifacts)
        }
        Box(modifier = GlanceModifier.defaultWeight()) {}
        virtueInfo.dailyEvents.forEach { event ->
            val eventImage = getEventImage(event.type)
            val eventBitmap = bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "events/$eventImage.png"
                    )
                )
            )
            Box(
                modifier = GlanceModifier.padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier.height(15.dp).width(25.dp).cornerRadius(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (event.isUltra) {
                        val gradientBitmap = createLinearGradientBitmap(
                            startColor = 0xfffca606.toInt(),
                            endColor = 0xff890faf.toInt(),
                            heightPx = 30,
                            widthPx = 50
                        )
                        Image(
                            provider = ImageProvider(gradientBitmap),
                            contentDescription = null,
                            modifier = GlanceModifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .background(Color(0xfffe0482))
                        ) {}
                    }

                    Image(
                        provider = ImageProvider(eventBitmap),
                        contentDescription = event.type,
                        modifier = GlanceModifier.size(20.dp)
                    )
                }
            }
        }
    }
}

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

            Box(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Row(
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
                }
            }
            Box(
                modifier = GlanceModifier.defaultWeight().padding(end = 5.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                val nextTEThreshold = getNextTruthEggThreshold(farm.eggsDelivered)
                var shippingText = if (nextTEThreshold == 0.0) {
                    "Winner!"
                } else {
                    "${numberToString(farm.eggsDelivered)}/${numberToString(nextTEThreshold)}"
                }
                if (!virtueInfo.isOnVirtue) {
                    shippingText += " shipped"
                }
                Text(
                    text = shippingText,
                    style = TextStyle(color = ColorProvider(textColor))
                )
            }
            if (virtueInfo.isOnVirtue) {
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(7.dp)
                        .padding(start = 5.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val percentComplete = getTruthEggPercentComplete(farm.eggsDelivered)
                    LinearProgressIndicator(
                        modifier = GlanceModifier.fillMaxSize(),
                        progress = percentComplete,
                        color = ColorProvider(Color(PROGRESS_COLOR.toColorInt())),
                        backgroundColor = ColorProvider(Color(PROGRESS_BACKGROUND_COLOR.toColorInt()))
                    )
                }
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (virtueInfo.isOnVirtue) {
                        val timeRemaining =
                            if (virtueInfo.offlineHatcheryRate == 0.0 && virtueInfo.eggLayingRate == 0.0) {
                                "Infinity"
                            } else {
                                getTimeRemainingToNextTruthEgg(virtueInfo, farm)
                            }
                        Text(
                            text = timeRemaining,
                            style = TextStyle(color = ColorProvider(textColor))
                        )
                    } else {
                        val delivered = numberToString(farm.eggsDelivered)
                        Text(
                            text = "$delivered shipped",
                            style = TextStyle(color = ColorProvider(textColor))
                        )
                    }
                }
            }
        }
    }
}