package widget.stats.normal

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
import androidx.glance.layout.padding
import androidx.glance.layout.size
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
import tools.utilities.bitmapResize
import tools.utilities.getAsset
import tools.utilities.getContractGradeName
import tools.utilities.getEggName
import tools.utilities.getFarmerRole
import tools.utilities.getShortenedFarmerRole
import tools.utilities.truncateString
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
            val showCommunityBadges =
                state[StatsWidgetDataStorePreferencesKeys.SHOW_COMMUNITY_BADGES] == true

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
                if (eid.isBlank()) {
                    NoStatsContent(context.assets, textColor)
                } else {
                    val assetManager = context.assets
                    NameAndRole(statsInfo, eiUserName, assetManager, textColor)
                    MysticalEggs(statsInfo, assetManager, textColor)
                    Earnings(statsInfo, assetManager, textColor)
                    Spendable(statsInfo, assetManager, textColor)
                    HomeFarm(statsInfo, assetManager, textColor)
                    Contracts(statsInfo, assetManager, textColor)
                    ShipsAndDrones(statsInfo, assetManager, textColor)
                    Crafting(statsInfo, assetManager, textColor)
                    if (showCommunityBadges) {
                        Badges(statsInfo, assetManager)
                    }
                }
            }
        }
    }
}

@Composable
fun LogoContentStats(assetManager: AssetManager) {
    val bitmapImage =
        BitmapFactory.decodeStream(getAsset(assetManager, "icons/logo-dark-mode.png"))

    Image(
        provider = ImageProvider(bitmapImage),
        contentDescription = "Empty Widget Logo",
        modifier = GlanceModifier.size(80.dp)
    )
}

@Composable
fun NoStatsContent(assetManager: AssetManager, textColor: Color) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoContentStats(assetManager)
        Text(
            text = "Waiting for stats data...",
            style = TextStyle(color = ColorProvider(textColor)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun NameAndRole(
    statsInfo: StatsInfo,
    eiUserName: String,
    assetManager: AssetManager,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val profileBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_player.png"
                )
            )
        )

        val bookBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "artifacts/afx_book_of_basan_4.png"
                )
            )
        )

        Image(
            provider = ImageProvider(profileBitmap),
            contentDescription = "Player Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
        Text(
            modifier = GlanceModifier.defaultWeight(),
            text = truncateString(eiUserName, 12),
            style = TextStyle(color = ColorProvider(textColor))
        )
        val farmerRole = getFarmerRole(statsInfo.farmerRoleId)
        val shortenedRole = getShortenedFarmerRole(farmerRole.first)

        Image(
            provider = ImageProvider(bookBitmap),
            contentDescription = "Book Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
        Text(text = shortenedRole, style = TextStyle(color = ColorProvider(textColor)))


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
        val soulEggBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "eggs/egg_soul.png"
                )
            )
        )

        val prophecyEggBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "eggs/egg_of_prophecy.png"
                )
            )
        )

        Image(
            provider = ImageProvider(soulEggBitmap),
            contentDescription = "Soul Egg Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
        Text(text = statsInfo.soulEggs, style = TextStyle(color = ColorProvider(textColor)))
        Box(modifier = GlanceModifier.defaultWeight()) {}

        Image(
            provider = ImageProvider(prophecyEggBitmap),
            contentDescription = "Prophecy Egg Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val permitName = if (statsInfo.hasProPermit) {
            "pro_permit"
        } else {
            "free_permit"
        }
        val permitBitmap =
            BitmapFactory.decodeStream(getAsset(assetManager, "other/$permitName.png"))

        val truthEggBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "eggs/egg_truth.png"
                )
            )
        )

        val farmerRole = getFarmerRole(statsInfo.farmerRoleId)

        // If the player has no truth eggs, they may not be far enough in the game yet
        // Show the permit icon instead, so there aren't potential spoilers with the truth egg icon
        if ((statsInfo.truthEggs.toIntOrNull() ?: 0) == 0) {
            Image(
                provider = ImageProvider(permitBitmap),
                contentDescription = "Permit Icon",
                modifier = GlanceModifier.size(24.dp)
            )
        } else {
            Image(
                provider = ImageProvider(truthEggBitmap),
                contentDescription = "Truth Egg Icon",
                modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
            )
            Text(text = statsInfo.truthEggs, style = TextStyle(color = ColorProvider(textColor)))
        }
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Box(
            modifier = GlanceModifier
                .size(12.dp)
                .background(farmerRole.second)
                .cornerRadius(6.dp),
            contentAlignment = Alignment.Center
        ) {}
        Text(
            modifier = GlanceModifier.padding(start = 3.dp),
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
        val goldEggBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_golden_egg.png"
                )
            )
        )

        val ticketBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_shell_script.png"
                )
            )
        )

        Image(
            provider = ImageProvider(goldEggBitmap),
            contentDescription = "Gold Egg Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
        Text(text = statsInfo.goldEggs, style = TextStyle(color = ColorProvider(textColor)))
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Image(
            provider = ImageProvider(ticketBitmap),
            contentDescription = "Ticket Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
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
        val farmBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_home_farm.png"
                )
            )
        )

        val eggName = getEggName(statsInfo.homeFarmEggId)
        val eggBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "eggs/$eggName.png"
                )
            )
        )

        val chickenBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_chicken.png"
                )
            )
        )

        Image(
            provider = ImageProvider(farmBitmap),
            contentDescription = "Home Farm Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
        Image(
            provider = ImageProvider(eggBitmap),
            contentDescription = "Home Egg Icon",
            modifier = GlanceModifier.size(20.dp)
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Image(
            provider = ImageProvider(chickenBitmap),
            contentDescription = "Home Population Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
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
        val grade = getContractGradeName(statsInfo.contractGrade)
        val gradeBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "grades/contract_$grade.png"
                )
            )
        )

        val leaderboardBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_leaderboard.png"
                )
            )
        )

        Image(
            provider = ImageProvider(gradeBitmap),
            contentDescription = "Contract Grade Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
        Text(
            text = statsInfo.contractSeasonScore,
            style = TextStyle(color = ColorProvider(textColor))
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Image(
            provider = ImageProvider(leaderboardBitmap),
            contentDescription = "Leaderboard Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
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
        val shipBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "ships/afx_ship_chicken_heavy.png"
                )
            )
        )

        val droneBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_drone.png"
                )
            )
        )

        Image(
            provider = ImageProvider(shipBitmap),
            contentDescription = "Ship Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
        Text(text = statsInfo.shipsLaunched, style = TextStyle(color = ColorProvider(textColor)))
        Box(modifier = GlanceModifier.defaultWeight()) {}
        Image(
            provider = ImageProvider(droneBitmap),
            contentDescription = "Drone Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
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
        val craftingBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "other/icon_afx_craft.png"
                )
            )
        )

        Image(
            provider = ImageProvider(craftingBitmap),
            contentDescription = "Crafting Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
        )
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
fun Badges(statsInfo: StatsInfo, assetManager: AssetManager) {
    val badges = statsInfo.badges
    Row(
        modifier = GlanceModifier.statsRowModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (badges.hasEnd) {
            BadgeComposable("end", assetManager)
        }
        if (badges.hasNahPlus) {
            BadgeComposable("nah_plus", assetManager)
        } else if (badges.hasNah) {
            BadgeComposable("nah", assetManager)
        }
        if (badges.hasFedPlus) {
            BadgeComposable("fed_plus", assetManager)
        } else if (badges.hasFed) {
            BadgeComposable("fed", assetManager)
        }
        if (badges.hasZlc) {
            BadgeComposable("zlc", assetManager)
        }
        if (badges.hasAlc) {
            BadgeComposable("alc", assetManager)
        }
        if (badges.hasAsc) {
            BadgeComposable("asc", assetManager)
        }
        if (badges.hasCraftingLegend) {
            BadgeComposable("crafting", assetManager)
        }
        if (badges.hasAllShells) {
            BadgeComposable("shell", assetManager)
        }
    }
}

@Composable
fun BadgeComposable(badgeName: String, assetManager: AssetManager) {
    val bitmap = bitmapResize(
        BitmapFactory.decodeStream(
            getAsset(
                assetManager,
                "badges/badge_$badgeName.png"
            )
        )
    )

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = "$badgeName badge",
        modifier = GlanceModifier.size(20.dp).padding(end = 3.dp)
    )
}

@Composable
private fun GlanceModifier.statsRowModifier() =
    this
        .padding(start = 5.dp, end = 8.dp)
        .fillMaxWidth()