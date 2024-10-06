package widget.large

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.BitmapFactory
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
import androidx.glance.layout.absolutePadding
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
import tools.createCircularProgressBarBitmap
import tools.getMissionDurationRemaining
import tools.getMissionPercentComplete
import tools.getShipName
import widget.MissionWidgetDataStore
import widget.MissionWidgetDataStorePreferencesKeys
import widget.MissionWidgetUpdater

class MissionWidgetLarge : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val prefEid = state[MissionWidgetDataStorePreferencesKeys.EID] ?: ""
            val preferencesMissionData =
                MissionWidgetDataStore().decodeMissionInfo(
                    state[MissionWidgetDataStorePreferencesKeys.MISSION_INFO] ?: ""
                )
            val useAbsoluteTime =
                state[MissionWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] ?: false
            val showTargetArtifact =
                state[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_LARGE_WIDGET] ?: false
            val showTankLevels =
                state[MissionWidgetDataStorePreferencesKeys.SHOW_TANK_LEVELS] ?: false
            val openEggInc =
                state[MissionWidgetDataStorePreferencesKeys.OPEN_EGG_INC] ?: false

            if (prefEid.isBlank()) {
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
                if (prefEid.isBlank() || preferencesMissionData.isEmpty()) {
                    NoMissionsContentLarge(assetManager)
                } else {
                    val missionsChunked = preferencesMissionData.chunked(2)
                    missionsChunked.forEach { missionGroup ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            missionGroup.forEach { mission ->
                                Row(
                                    modifier = GlanceModifier.defaultWeight()
                                        .padding(horizontal = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MissionProgressLarge(
                                        assetManager,
                                        mission,
                                        useAbsoluteTime,
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
}

@Composable
fun LogoContentLarge(assetManager: AssetManager) {
    val bitmapImage =
        BitmapFactory.decodeStream(assetManager.open("icons/logo-dark-mode.png"))

    Image(
        provider = ImageProvider(bitmapImage),
        contentDescription = "Empty Widget Logo",
        modifier = GlanceModifier.size(80.dp)
    )
}

@Composable
fun NoMissionsContentLarge(assetManager: AssetManager) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoContentLarge(assetManager)
        Text(
            text = "Waiting for mission data...",
            style = TextStyle(color = ColorProvider(Color.White)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun MissionProgressLarge(
    assetManager: AssetManager,
    mission: MissionInfoEntry,
    useAbsoluteTime: Boolean,
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
        modifier = GlanceModifier.padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = createCircularProgressBarBitmap(
            percentRemaining,
            mission.durationType,
            100,
            isFueling
        )

        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Circular Progress",
            modifier = GlanceModifier.size(95.dp)
        )

        val shipName = getShipName(mission.shipId)
        val shipBitmap =
            BitmapFactory.decodeStream(assetManager.open("ships/$shipName.png"))
        Image(
            provider = ImageProvider(shipBitmap),
            contentDescription = "Ship Icon",
            modifier = GlanceModifier.size(55.dp)
        )

        val artifactName = getImageFromAfxId(mission.targetArtifact)
        if (showTargetArtifact && artifactName.isNotBlank()) {
            val artifactBitmap =
                BitmapFactory.decodeStream(assetManager.open("artifacts/$artifactName.png"))
            Image(
                provider = ImageProvider(artifactBitmap),
                contentDescription = "Target Artifact",
                modifier = GlanceModifier.size(85.dp)
                    .absolutePadding(bottom = 65.dp, left = 60.dp)
            )
        }
    }

    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "time", style = TextStyle(color = ColorProvider(Color.White)))
        Text(text = "stars", style = TextStyle(color = ColorProvider(Color.White)))
        Text(text = "cap", style = TextStyle(color = ColorProvider(Color.White)))
    }

//    Column(
//        modifier = GlanceModifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalAlignment = Alignment.Bottom
//    ) {
//        Text(
//            text =
//            if (isFueling) {
//                "Fueling"
//            } else {
//                getMissionDurationRemaining(
//                    mission.secondsRemaining,
//                    mission.date,
//                    useAbsoluteTime
//                )
//            },
//            style = TextStyle(
//                color = ColorProvider(Color.White),
//                fontSize = TextUnit(12f, TextUnitType.Sp)
//            ),
//            modifier = GlanceModifier.padding(bottom = 2.dp)
//        )
//    }
}