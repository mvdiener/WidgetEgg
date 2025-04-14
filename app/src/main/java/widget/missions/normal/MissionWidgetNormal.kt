package widget.normal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.MissionInfoEntry
import data.getImageFromAfxId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.bitmapResize
import tools.createCircularProgressBarBitmap
import tools.getMissionDurationRemaining
import tools.getMissionPercentComplete
import tools.getShipName
import widget.MissionWidgetDataStore
import widget.MissionWidgetDataStorePreferencesKeys
import widget.MissionWidgetUpdater

class MissionWidgetNormal : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val eid = state[MissionWidgetDataStorePreferencesKeys.EID] ?: ""
            val missionData =
                MissionWidgetDataStore().decodeMissionInfo(
                    state[MissionWidgetDataStorePreferencesKeys.MISSION_INFO] ?: ""
                )
            val useAbsoluteTime =
                state[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] ?: false
            val useAbsoluteTimePlusDay =
                state[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME_PLUS_DAY] ?: false
            val showTargetArtifact =
                state[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_NORMAL_WIDGET] ?: false
            val showFuelingShip =
                state[MissionWidgetDataStorePreferencesKeys.SHOW_FUELING_SHIP] ?: false
            val openEggInc =
                state[MissionWidgetDataStorePreferencesKeys.OPEN_EGG_INC] ?: false

            if (eid.isBlank()) {
                // If EID is blank, could either mean state is not initialized or user is not logged in
                // Attempt to load state in case it is needed, otherwise login composable will show
                LaunchedEffect(true) {
                    CoroutineScope(context = Dispatchers.IO).launch {
                        MissionWidgetUpdater().updateMissions(context)
                    }
                }
            }

            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xff181818))
                    .clickable {
                        if (openEggInc) {
                            val packageManager: PackageManager = context.packageManager
                            val launchIntent: Intent? =
                                packageManager.getLaunchIntentForPackage("com.auxbrain.egginc")
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        } else {
                            MissionWidgetUpdater().updateMissions(context)
                        }
                    }
            ) {
                val assetManager = context.assets
                if (eid.isBlank() || missionData.isEmpty()) {
                    NoMissionsContent(assetManager)
                } else {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val use24HrFormat = DateFormat.is24HourFormat(context)
                        missionData.forEach { mission ->
                            if (mission.identifier.isBlank() && !showFuelingShip) return@forEach
                            Column(
                                modifier = GlanceModifier.fillMaxHeight().defaultWeight()
                                    .padding(vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MissionProgress(
                                    assetManager,
                                    mission,
                                    useAbsoluteTime,
                                    useAbsoluteTimePlusDay,
                                    use24HrFormat,
                                    showTargetArtifact
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogoContent(assetManager: AssetManager) {
    val bitmapImage =
        BitmapFactory.decodeStream(assetManager.open("icons/logo-dark-mode.png"))

    Image(
        provider = ImageProvider(bitmapImage),
        contentDescription = "Empty Widget Logo",
        modifier = GlanceModifier.size(60.dp)
    )
}

@Composable
fun NoMissionsContent(assetManager: AssetManager) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoContent(assetManager)
        Text(
            text = "Waiting for mission data...",
            style = TextStyle(color = ColorProvider(Color.White)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun MissionProgress(
    assetManager: AssetManager,
    mission: MissionInfoEntry,
    useAbsoluteTime: Boolean,
    useAbsoluteTimePlusDay: Boolean,
    use24HrFormat: Boolean,
    showTargetArtifact: Boolean
) {
    val isFueling = mission.identifier.isBlank()

    val percentRemaining =
        if (isFueling) {
            1f
        } else {
            getMissionPercentComplete(
                mission.missionDuration,
                mission.secondsRemaining,
                mission.date
            )
        }

    Box(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 5.dp),
    ) {
        Box(
            modifier = GlanceModifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = createCircularProgressBarBitmap(
                percentRemaining,
                mission.durationType,
                115,
                isFueling
            )

            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Circular Progress",
                modifier = GlanceModifier.size(65.dp)
            )

            val shipName = getShipName(mission.shipId)
            val shipBitmap = BitmapFactory.decodeStream(assetManager.open("ships/$shipName.png"))

            Image(
                provider = ImageProvider(shipBitmap),
                contentDescription = "Ship Icon",
                modifier = GlanceModifier.size(35.dp)
            )
        }

        Box(
            modifier = GlanceModifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            val artifactName = getImageFromAfxId(mission.targetArtifact)
            if (showTargetArtifact && artifactName.isNotBlank()) {
                val artifactBitmap = bitmapResize(
                    BitmapFactory.decodeStream(assetManager.open("artifacts/$artifactName.png"))
                )
                Image(
                    provider = ImageProvider(artifactBitmap),
                    contentDescription = "Target Artifact",
                    modifier = GlanceModifier.size(20.dp)
                )
            }
        }
    }

    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text =
            if (isFueling) {
                "Fueling"
            } else {
                val (timeText, plusDay) = getMissionDurationRemaining(
                    mission.secondsRemaining,
                    mission.date,
                    useAbsoluteTime,
                    useAbsoluteTimePlusDay,
                    use24HrFormat
                )
                if (useAbsoluteTimePlusDay && plusDay) {
                    "$timeText⁺¹"
                } else {
                    timeText
                }
            },
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = TextUnit(13f, TextUnitType.Sp)
            )
        )
    }
}