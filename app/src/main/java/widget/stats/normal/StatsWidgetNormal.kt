package widget.stats.normal

import android.content.Context
import android.content.res.AssetManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.DEFAULT_WIDGET_TEXT_COLOR
import data.StatsInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.getFarmerRole
import tools.utilities.getShortenedFarmerName
import widget.WidgetUpdater
import widget.stats.StatsWidgetDataStore
import widget.stats.StatsWidgetDataStorePreferencesKeys

class StatsWidgetNormal : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val eid = state[StatsWidgetDataStorePreferencesKeys.EID] ?: ""
            val eiUserName = state[StatsWidgetDataStorePreferencesKeys.EI_USER_NAME] ?: ""
            val statsInfo = StatsWidgetDataStore().decodeStatsInfo(
                state[StatsWidgetDataStorePreferencesKeys.STATS_INFO] ?: ""
            )
            val backgroundColor =
                state[StatsWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR]?.let { colorInt ->
                    Color(colorInt)
                } ?: DEFAULT_WIDGET_BACKGROUND_COLOR
            val textColor =
                state[StatsWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR]?.let { colorInt ->
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
                NameAndPermit(statsInfo, eiUserName, assetManager, textColor)
                MysticalEggs(statsInfo, assetManager, textColor)
                Earnings(statsInfo, assetManager, textColor)
                Spendable(statsInfo, assetManager, textColor)
                HomeFarm(statsInfo, assetManager, textColor)
                Contracts(statsInfo, assetManager, textColor)
                ShipsAndDrones(statsInfo, assetManager, textColor)
                Crafting(statsInfo, assetManager, textColor)
            }
        }
    }
}

@Composable
fun NameAndPermit(
    statsInfo: StatsInfo,
    eiUserName: String,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = eiUserName, style = TextStyle(color = ColorProvider(textColor)))
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Text(
            text = statsInfo.hasProPermit.toString(),
            style = TextStyle(color = ColorProvider(textColor))
        )
    }
}

@Composable
fun MysticalEggs(
    statsInfo: StatsInfo,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = statsInfo.soulEggs, style = TextStyle(color = ColorProvider(textColor)))
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Text(text = statsInfo.prophecyEggs, style = TextStyle(color = ColorProvider(textColor)))
    }
}

@Composable
fun Earnings(
    statsInfo: StatsInfo,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val farmerRole = getFarmerRole(statsInfo.farmerRoleId)
        val farmerName = getShortenedFarmerName(farmerRole.first)
        Text(text = farmerName, style = TextStyle(color = ColorProvider(textColor)))
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Text(
            text = "${statsInfo.earningsBonus} %",
            style = TextStyle(color = ColorProvider(textColor))
        )
    }
}

@Composable
fun Spendable(
    statsInfo: StatsInfo,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = statsInfo.goldEggs, style = TextStyle(color = ColorProvider(textColor)))
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Text(text = statsInfo.tickets, style = TextStyle(color = ColorProvider(textColor)))
    }
}

@Composable
fun HomeFarm(
    statsInfo: StatsInfo,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statsInfo.homeFarmEggId.toString(),
            style = TextStyle(color = ColorProvider(textColor))
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Text(
            text = statsInfo.homeFarmPopulation,
            style = TextStyle(color = ColorProvider(textColor))
        )
    }
}

@Composable
fun Contracts(
    statsInfo: StatsInfo,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statsInfo.contractSeasonScore,
            style = TextStyle(color = ColorProvider(textColor))
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Text(
            text = statsInfo.contractTotalScore, style = TextStyle(color = ColorProvider(textColor))
        )
    }
}

@Composable
fun ShipsAndDrones(
    statsInfo: StatsInfo,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = statsInfo.shipsLaunched, style = TextStyle(color = ColorProvider(textColor)))
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Text(text = statsInfo.droneTakedowns, style = TextStyle(color = ColorProvider(textColor)))
    }
}

@Composable
fun Crafting(
    statsInfo: StatsInfo,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statsInfo.craftingLevel.toString(),
            style = TextStyle(color = ColorProvider(textColor))
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Text(
            text = "${statsInfo.craftingXP} XP",
            style = TextStyle(color = ColorProvider(textColor))
        )
    }
}

@Composable

private fun GlanceModifier.statsRowModifier() =
    this
        .padding(horizontal = 15.dp)
        .fillMaxWidth()