package widget.virtue.normal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import data.constants.DEFAULT_BROWSER
import data.constants.PROBLEMATIC_BROWSERS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.bitmapResize
import tools.utilities.getAsset
import tools.utilities.getEggName
import tools.utilities.getNextTruthEggThreshold
import tools.utilities.getOfflineTruthEggPercentComplete
import tools.utilities.getRemainingSiloTime
import tools.utilities.getTimeRemainingToNextTruthEgg
import tools.utilities.getVirtueFunctionIconPath
import tools.utilities.numberToString
import widget.WidgetUpdater
import widget.shared.NoWidgetContent
import widget.virtue.VirtueWidgetDataStore
import widget.virtue.VirtueWidgetDataStorePreferencesKeys
import kotlin.collections.contains

class VirtueWidgetNormal : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val eid = state[VirtueWidgetDataStorePreferencesKeys.EID] ?: ""
            val virtueInfo = VirtueWidgetDataStore().decodeVirtueInfo(
                state[VirtueWidgetDataStorePreferencesKeys.VIRTUE_INFO] ?: ""
            )
            val useAbsoluteTimeVirtueSilos =
                state[VirtueWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME_VIRTUE_SILOS] ?: false
            val useAbsoluteTimeVirtueNextTruthEgg =
                state[VirtueWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME_VIRTUE_NEXT_TRUTH_EGG]
                    ?: false
            val openVirtueCompanion =
                state[VirtueWidgetDataStorePreferencesKeys.OPEN_VIRTUE_COMPANION] ?: false
            val showNextFiveGoals =
                state[VirtueWidgetDataStorePreferencesKeys.SHOW_NEXT_FIVE_GOALS] ?: false
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
                        if (openVirtueCompanion) {
                            val packageManager: PackageManager = context.packageManager
                            var browserPackage: String? = packageManager.resolveActivity(
                                Intent(Intent.ACTION_VIEW, "https://www.example.com".toUri()),
                                PackageManager.MATCH_DEFAULT_ONLY
                            )?.activityInfo?.packageName

                            if (browserPackage != null) {
                                // Not all browsers play nicely with opening a link from a widget
                                // If using any of these browsers, attempt to use chrome instead
                                if (browserPackage in PROBLEMATIC_BROWSERS) {
                                    browserPackage = DEFAULT_BROWSER
                                }
                                val launchIntent: Intent? =
                                    packageManager.getLaunchIntentForPackage(browserPackage)
                                launchIntent?.data =
                                    "https://wasmegg-carpet.netlify.app/virtue-companion?playerId=$eid".toUri()
                                context.startActivity(launchIntent)
                            }
                        } else {
                            scope.launch {
                                try {
                                    WidgetUpdater().updateWidgets(context)
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }
            ) {
                val assetManager = context.assets
                val use24HrFormat = DateFormat.is24HourFormat(context)
                if (eid.isBlank() || virtueInfo.stateId.isBlank()) {
                    NoWidgetContent(assetManager, "Waiting for virtue data...", 80.dp, textColor)
                } else {
                    HomeFarmInfo(
                        assetManager,
                        virtueInfo,
                        useAbsoluteTimeVirtueSilos,
                        use24HrFormat,
                        textColor
                    )
                    ShiftInfo(assetManager, virtueInfo, textColor)
                    FarmProgress(
                        assetManager,
                        virtueInfo,
                        useAbsoluteTimeVirtueNextTruthEgg,
                        use24HrFormat,
                        showNextFiveGoals,
                        textColor
                    )
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
}

@Composable
fun HomeFarmInfo(
    assetManager: AssetManager,
    virtueInfo: VirtueInfo,
    useAbsoluteTimeVirtueSilos: Boolean,
    use24HrFormat: Boolean,
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
                modifier = GlanceModifier.size(20.dp).padding(start = 2.dp)
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
            getRemainingSiloTime(
                virtueInfo.lastBackupDate,
                virtueInfo.maximumOfflineTime,
                useAbsoluteTimeVirtueSilos,
                use24HrFormat
            )
        Text(
            text = siloTimeRemaining,
            style = TextStyle(color = ColorProvider(textColor))
        )
    }
}

@Composable
fun FarmProgress(
    assetManager: AssetManager,
    virtueInfo: VirtueInfo,
    useAbsoluteTimeVirtueNextTruthEgg: Boolean,
    use24HrFormat: Boolean,
    showNextFiveGoals: Boolean,
    textColor: Color
) {
    val farms = if (virtueInfo.isOnVirtue) {
        val (activeFarm, otherFarms) = virtueInfo.farms.partition { it.eggId == virtueInfo.eggId }
        if (showNextFiveGoals && activeFarm.isNotEmpty()) {
            List(5) { activeFarm.first() }
        } else {
            activeFarm + otherFarms
        }
    } else {
        virtueInfo.farms
    }
    farms.forEachIndexed { index, farm ->
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 5.dp),
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
            val pendingText = if (!virtueInfo.isOnVirtue || !showNextFiveGoals || index == 0) {
                "${farm.truthEggs}(${farm.pendingTruthEggs})"
            } else {
                "+${index + 1}"
            }
            Text(
                modifier = GlanceModifier.padding(end = 2.dp),
                text = pendingText,
                style = TextStyle(color = ColorProvider(textColor))
            )
            Box(modifier = GlanceModifier.defaultWeight()) {}
            if (virtueInfo.isOnVirtue) {
                val offlinePercentComplete =
                    if (virtueInfo.eggId == farm.eggId) {
                        getOfflineTruthEggPercentComplete(
                            virtueInfo,
                            farm.eggsDelivered,
                            if (showNextFiveGoals) index else 0
                        )
                    } else {
                        0.0f
                    }
                val timeRemaining =
                    if (virtueInfo.offlineHatcheryRate == 0.0 && virtueInfo.eggLayingRate == 0.0) {
                        "Infinity"
                    } else if (offlinePercentComplete >= 1.0f) {
                        "TE ready"
                    } else {
                        getTimeRemainingToNextTruthEgg(
                            virtueInfo,
                            farm,
                            useAbsoluteTimeVirtueNextTruthEgg,
                            use24HrFormat,
                            if (showNextFiveGoals) index else 0
                        )
                    }
                Text(
                    text = timeRemaining,
                    style = TextStyle(color = ColorProvider(textColor))
                )
            } else {
                val nextTEThreshold = getNextTruthEggThreshold(farm.eggsDelivered)
                val shippingText = if (nextTEThreshold == 0.0) {
                    "Winner!"
                } else {
                    "${numberToString(farm.eggsDelivered)} shipped"
                }
                Text(
                    text = shippingText,
                    style = TextStyle(color = ColorProvider(textColor))
                )
            }
        }
    }
}