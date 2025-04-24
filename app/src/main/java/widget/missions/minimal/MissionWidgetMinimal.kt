package widget.missions.minimal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import data.MissionInfoEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.createMissionCircularProgressBarBitmap
import tools.utilities.getAsset
import tools.utilities.getMissionPercentComplete
import tools.utilities.getShipName
import widget.missions.MissionWidgetDataStore
import widget.missions.MissionWidgetDataStorePreferencesKeys
import widget.missions.MissionWidgetUpdater

class MissionWidgetMinimal : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val eid = state[MissionWidgetDataStorePreferencesKeys.EID] ?: ""
            val missionData =
                MissionWidgetDataStore().decodeMissionInfo(
                    state[MissionWidgetDataStorePreferencesKeys.MISSION_INFO] ?: ""
                )
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
                    NoMissionsContentMinimal(assetManager)
                } else {
                    val missionsChunked = missionData.chunked(2)
                    missionsChunked.forEach { missionGroup ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            missionGroup.forEach { mission ->
                                MissionProgressMinimal(
                                    assetManager,
                                    mission
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
fun LogoContentMinimal(assetManager: AssetManager) {
    val bitmapImage =
        BitmapFactory.decodeStream(getAsset(assetManager, "icons/logo-dark-mode.png"))

    Image(
        provider = ImageProvider(bitmapImage),
        contentDescription = "Empty Widget Logo",
        modifier = GlanceModifier.size(40.dp)
    )
}

@Composable
fun NoMissionsContentMinimal(assetManager: AssetManager) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoContentMinimal(assetManager)
        Text(
            text = "Waiting for mission data...",
            style = TextStyle(color = ColorProvider(Color.White)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun MissionProgressMinimal(
    assetManager: AssetManager,
    mission: MissionInfoEntry
) {
    val isFueling = mission.identifier.isBlank()

    val percentComplete =
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
        modifier = GlanceModifier.padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = createMissionCircularProgressBarBitmap(
            percentComplete,
            mission.durationType,
            100,
            isFueling
        )

        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Circular Progress",
            modifier = GlanceModifier.size(45.dp)
        )

        val shipName = getShipName(mission.shipId)
        val shipBitmap = BitmapFactory.decodeStream(getAsset(assetManager, "ships/$shipName.png"))

        Image(
            provider = ImageProvider(shipBitmap),
            contentDescription = "Ship Icon",
            modifier = GlanceModifier.size(25.dp)
        )
    }
}