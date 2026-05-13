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
import data.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.DEFAULT_WIDGET_TEXT_COLOR
import data.VirtueInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.bitmapResize
import tools.utilities.createGlowBitmap
import tools.utilities.getAsset
import tools.utilities.getEggName
import tools.utilities.getImageNameFromAfxId
import tools.utilities.getRemainingSiloTime
import widget.WidgetUpdater
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

            if (eid.isBlank()) {
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
                    NoVirtueContent(assetManager, textColor)
                } else {
                    FarmData(assetManager, context, virtueInfo, textColor)
                }
            }
        }
    }
}

@Composable
fun LogoContentVirtue(assetManager: AssetManager) {
    val bitmapImage =
        BitmapFactory.decodeStream(getAsset(assetManager, "icons/logo-dark-mode.png"))

    Image(
        provider = ImageProvider(bitmapImage),
        contentDescription = "Empty Widget Logo",
        modifier = GlanceModifier.size(80.dp)
    )
}

@Composable
fun NoVirtueContent(assetManager: AssetManager, textColor: Color) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoContentVirtue(assetManager)
        Text(
            text = "Waiting for virtue data...",
            style = TextStyle(color = ColorProvider(textColor)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun FarmData(
    assetManager: AssetManager,
    context: Context,
    virtueInfo: VirtueInfo,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
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
            modifier = GlanceModifier.size(40.dp).padding(start = 5.dp)
        )
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (true) {
//            if (virtueInfo.isOnVirtue) {
                Text(
                    text = "Shifts: ${virtueInfo.shifts}",
                    style = TextStyle(color = ColorProvider(textColor))
                )
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next shift: 1.1Q",
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
            Silos(assetManager, virtueInfo, textColor)
        }
    }
}

@Composable
fun Silos(
    assetManager: AssetManager,
    virtueInfo: VirtueInfo,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val siloBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "research/r_icon_silo_capacity.png"
                )
            )
        )
        Image(
            provider = ImageProvider(siloBitmap),
            contentDescription = "Silos",
            modifier = GlanceModifier.size(25.dp).padding(end = 2.dp)
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
fun Artifacts(
    assetManager: AssetManager,
    virtueInfo: VirtueInfo
) {
    val artifacts = if (virtueInfo.isOnVirtue) {
        virtueInfo.virtueEquippedArtifacts
    } else {
        virtueInfo.homeEquippedArtifacts
    }
    if (artifacts.isNotEmpty()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            artifacts.forEachIndexed { index, artifact ->
                val artifactName =
                    getImageNameFromAfxId(artifact.name, artifact.level)
                val artifactBitmap = bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "artifacts/$artifactName.png"
                        )
                    )
                )
                Box(
                    modifier = GlanceModifier.size(30.dp).padding(start = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (artifact.rarity > 0) {
                        Image(
                            provider = ImageProvider(createGlowBitmap(artifact.rarity)),
                            contentDescription = null,
                            modifier = GlanceModifier.fillMaxSize()
                        )
                    }
                    Image(
                        provider = ImageProvider(artifactBitmap),
                        contentDescription = "Virtue Artifact $index",
                        modifier = GlanceModifier.size(25.dp)
                    )
                    if (artifact.stones.isNotEmpty()) {
                        Box(
                            modifier = GlanceModifier.fillMaxSize().padding(end = 2.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                artifact.stones.forEachIndexed { index, stone ->
                                    val stoneName =
                                        getImageNameFromAfxId(stone.name, stone.level + 1)
                                    val stoneBitmap = bitmapResize(
                                        BitmapFactory.decodeStream(
                                            getAsset(assetManager, "artifacts/$stoneName.png")
                                        )
                                    )
                                    Image(
                                        provider = ImageProvider(stoneBitmap),
                                        contentDescription = "Stone Icon $index",
                                        modifier = GlanceModifier.size(8.dp)
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