package widget

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
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
import tools.createCircularProgressBarBitmap
import tools.getMissionDurationRemaining
import tools.getMissionPercentComplete
import tools.getShipName

class MissionWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Call the updater on widget creation to put existing data into widget state
        // This is a hacky way to do this, but it works... for now
        // The ideal way to do this is to have all widgets read from the same state file
        // using a custom glanceStateDefinition, but I have not been able to make that work.
        MissionWidgetUpdater().updateMissions(context)
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
                state[MissionWidgetDataStorePreferencesKeys.TARGET_ARTIFACT_SMALL] ?: false
            val showFuelingShip =
                state[MissionWidgetDataStorePreferencesKeys.SHOW_FUELING_SHIP] ?: false

            Column(
                verticalAlignment = Alignment.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xff181818))
                    .clickable {
                        MissionWidgetUpdater().updateMissions(context)
                    }
            ) {
                val assetManager = context.assets
                if (prefEid.isBlank()) {
                    LoggedOutContent(assetManager)
                } else if (preferencesMissionData.isEmpty()) {
                    NoMissionsContent(assetManager)
                } else {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        preferencesMissionData.forEach { mission ->
                            if (mission.identifier.isBlank() && !showFuelingShip) return@forEach
                            Box(
                                modifier = GlanceModifier.defaultWeight().padding(bottom = 3.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                MissionProgress(
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
fun LoggedOutContent(assetManager: AssetManager) {
    LogoContent(assetManager)
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Please log in!",
            style = TextStyle(color = ColorProvider(Color.White)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun NoMissionsContent(assetManager: AssetManager) {
    LogoContent(assetManager)
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Loading mission data...",
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
        modifier = GlanceModifier.fillMaxSize().padding(bottom = 5.dp),
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
            modifier = GlanceModifier.size(65.dp)
        )

        val shipName = getShipName(mission.shipId)
        val shipBitmap =
            BitmapFactory.decodeStream(assetManager.open("ships/$shipName.png"))
        Image(
            provider = ImageProvider(shipBitmap),
            contentDescription = "Ship Icon",
            modifier = GlanceModifier.size(35.dp)
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
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text =
            if (isFueling) {
                "Fueling"
            } else {
                getMissionDurationRemaining(
                    mission.secondsRemaining,
                    mission.date,
                    useAbsoluteTime
                )
            },
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = TextUnit(12f, TextUnitType.Sp)
            ),
            modifier = GlanceModifier.padding(bottom = 2.dp)
        )
    }

}