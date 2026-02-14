package widget.contracts.large

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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.CONTRACT_OFFLINE_PROGRESS_COLOR
import data.CONTRACT_PROGRESS_COLOR
import data.ContractInfoEntry
import data.ContributorInfoEntry
import data.DEFAULT_BROWSER
import data.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.DEFAULT_WIDGET_TEXT_COLOR
import data.PROBLEMATIC_BROWSERS
import data.PROGRESS_BACKGROUND_COLOR
import data.PeriodicalsContractInfoEntry
import data.SeasonGradeAndGoals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.bitmapResize
import tools.utilities.createGlowBitmap
import tools.utilities.formatTokenTimeText
import tools.utilities.getAsset
import tools.utilities.getColleggtibleBitmap
import tools.utilities.getContractDurationRemaining
import tools.utilities.getGoalPercentComplete
import tools.utilities.getContractGradeName
import tools.utilities.getContractTimeTextColor
import tools.utilities.getContractTotalTimeText
import tools.utilities.getCoopEggsPerHour
import tools.utilities.getEggName
import tools.utilities.getImageNameFromAfxId
import tools.utilities.getIndividualEggsPerHour
import tools.utilities.getOfflineEggsDelivered
import tools.utilities.getOfflineTimeHoursAndMinutes
import tools.utilities.getRewardIconPath
import tools.utilities.getScrollName
import tools.utilities.numberToString
import tools.utilities.truncateString
import widget.WidgetUpdater
import widget.contracts.ContractWidgetDataStore
import widget.contracts.ContractWidgetDataStorePreferencesKeys
import widget.contracts.active.LogoContentContracts
import kotlin.text.isBlank

class ContractWidgetLarge : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val eid = state[ContractWidgetDataStorePreferencesKeys.EID] ?: ""
            val contractData =
                ContractWidgetDataStore().decodeContractInfo(
                    state[ContractWidgetDataStorePreferencesKeys.CONTRACT_INFO] ?: ""
                )
            val periodicalsContractData =
                ContractWidgetDataStore().decodePeriodicalsContractInfo(
                    state[ContractWidgetDataStorePreferencesKeys.PERIODICALS_CONTRACT_INFO] ?: ""
                )
            val seasonData = ContractWidgetDataStore().decodeSeasonInfo(
                state[ContractWidgetDataStorePreferencesKeys.SEASON_INFO] ?: ""
            )
            val useAbsoluteTime =
                state[ContractWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] == true
            val useOfflineTime =
                state[ContractWidgetDataStorePreferencesKeys.USE_OFFLINE_TIME] == true
            val showAvailableContracts =
                state[ContractWidgetDataStorePreferencesKeys.SHOW_AVAILABLE_CONTRACTS] == true
            val showSeasonInfo =
                state[ContractWidgetDataStorePreferencesKeys.SHOW_SEASON_INFO] == true
            val openWasmeggDashboard =
                state[ContractWidgetDataStorePreferencesKeys.OPEN_WASMEGG_DASHBOARD] == true
            val backgroundColor =
                state[ContractWidgetDataStorePreferencesKeys.WIDGET_BACKGROUND_COLOR]?.let { colorInt ->
                    Color(colorInt)
                } ?: DEFAULT_WIDGET_BACKGROUND_COLOR
            val textColor =
                state[ContractWidgetDataStorePreferencesKeys.WIDGET_TEXT_COLOR]?.let { colorInt ->
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
            val assetManager = context.assets
            if (eid.isBlank() || (contractData.isEmpty() && !showAvailableContracts && !showSeasonInfo)) {
                Column(
                    modifier = GlanceModifier
                        .background(backgroundColor)
                        .fillMaxSize()
                        .contractWidgetClick(context, scope, eid, openWasmeggDashboard),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NoContractsContentLarge(assetManager, textColor)
                }
            } else if (contractData.size == 1 && !showAvailableContracts && !showSeasonInfo) {
                Column(
                    modifier = GlanceModifier
                        .background(backgroundColor)
                        .fillMaxSize()
                        .contractWidgetClick(context, scope, eid, openWasmeggDashboard),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContractContentLarge(
                        assetManager,
                        context,
                        contractData.first(),
                        useAbsoluteTime,
                        useOfflineTime,
                        textColor
                    )
                }
            } else if (showSeasonInfo && seasonData.goals.isNotEmpty() && !showAvailableContracts && contractData.isEmpty()) {
                Column(
                    modifier = GlanceModifier
                        .background(backgroundColor)
                        .fillMaxSize()
                        .contractWidgetClick(context, scope, eid, openWasmeggDashboard),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SeasonContent(assetManager, seasonData, textColor)
                }
            } else {
                val filteredPeriodicalsContracts =
                    periodicalsContractData.filter { periodical -> (periodical.identifier !in contractData.map { it.identifier }) && periodical.identifier != "first-contract" }
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.fillMaxSize().background(backgroundColor)
                ) {
                    item {
                        Spacer(modifier = GlanceModifier.height(5.dp))
                    }
                    for (contract in contractData) {
                        item {
                            Column(
                                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 5.dp)
                                    .contractWidgetClick(context, scope, eid, openWasmeggDashboard)
                            ) {
                                ContractContentLarge(
                                    assetManager,
                                    context,
                                    contract,
                                    useAbsoluteTime,
                                    useOfflineTime,
                                    textColor
                                )
                            }
                        }
                    }
                    if (showAvailableContracts) {
                        for (contract in filteredPeriodicalsContracts) {
                            item {
                                Column(
                                    modifier = GlanceModifier.fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .contractWidgetClick(
                                            context,
                                            scope,
                                            eid,
                                            openWasmeggDashboard
                                        )
                                ) {
                                    PeriodicalsContractContent(
                                        assetManager,
                                        context,
                                        contract,
                                        textColor
                                    )
                                }
                            }
                        }
                    }
                    if (showSeasonInfo && seasonData.goals.isNotEmpty()) {
                        item {
                            Column(
                                modifier = GlanceModifier.fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .contractWidgetClick(
                                        context,
                                        scope,
                                        eid,
                                        openWasmeggDashboard
                                    )
                            ) {
                                SeasonContent(assetManager, seasonData, textColor)
                            }
                        }
                    }
                    item {
                        Spacer(modifier = GlanceModifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NoContractsContentLarge(assetManager: AssetManager, textColor: Color) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoContentContracts(assetManager)
        Text(
            text = "Waiting for contract data...",
            style = TextStyle(color = ColorProvider(textColor)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

data class EggAndGrade(
    val customEggId: String?,
    val eggId: Int,
    val grade: Int,
    val name: String,
    val isUltra: Boolean
)

data class CoopInfo(
    val coopName: String?,
    val maxCoopSize: Int,
    val coopLength: Double?,
    val contributors: List<ContributorInfoEntry>,
    val seasonName: String?,
    val isLegacy: Boolean,
    val tokenTimerMinutes: Double
)

@Composable
fun ContractContentLarge(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
    textColor: Color
) {
    EggAndGrade(
        assetManager,
        context,
        EggAndGrade(
            contract.customEggId,
            contract.eggId,
            contract.grade,
            contract.name,
            contract.isUltra
        ),
        textColor
    )
    CoopNameAndInfo(
        assetManager, CoopInfo(
            contract.coopName,
            contract.maxCoopSize,
            null,
            contract.contributors,
            contract.seasonName,
            contract.isLegacy,
            contract.tokenTimerMinutes
        ), textColor
    )
    ShippingAndOfflineTime(assetManager, contract, textColor)
    ArtifactsAndTimeRemaining(
        assetManager,
        context,
        contract,
        useAbsoluteTime,
        useOfflineTime,
        textColor
    )
    Goals(assetManager, contract, useOfflineTime, textColor)
}

@Composable
fun PeriodicalsContractContent(
    assetManager: AssetManager,
    context: Context,
    contract: PeriodicalsContractInfoEntry,
    textColor: Color
) {
    EggAndGrade(
        assetManager,
        context,
        EggAndGrade(
            contract.customEggId,
            contract.eggId,
            contract.grade,
            contract.name,
            contract.isUltra
        ),
        textColor
    )
    CoopNameAndInfo(
        assetManager,
        CoopInfo(
            null,
            contract.maxCoopSize,
            contract.coopLengthSeconds,
            emptyList(),
            contract.seasonName,
            contract.isLegacy,
            contract.tokenTimerMinutes
        ),
        textColor
    )
    PeriodicalGoals(assetManager, contract, textColor)
}

@Composable
fun EggAndGrade(
    assetManager: AssetManager,
    context: Context,
    data: EggAndGrade,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val eggName = if (data.customEggId.isNullOrBlank()) {
            getEggName(data.eggId)
        } else {
            "egg_${data.customEggId}"
        }

        val eggBitmap = if (data.customEggId.isNullOrBlank()) {
            bitmapResize(BitmapFactory.decodeStream(getAsset(assetManager, "eggs/$eggName.png")))
        } else {
            getColleggtibleBitmap(assetManager, eggName, context)
        }

        Image(
            provider = ImageProvider(eggBitmap),
            contentDescription = "Egg Icon",
            modifier = GlanceModifier.size(30.dp).padding(end = 5.dp)
        )
        Text(
            text = data.name,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = TextUnit(18f, TextUnitType.Sp)
            )
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        if (data.isUltra) {
            val ultraBitmap =
                bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "other/icon_ultra.png"
                        )
                    )
                )
            Image(
                provider = ImageProvider(ultraBitmap),
                contentDescription = "Ultra Icon",
                modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
            )
        }
        val grade = getContractGradeName(data.grade)
        val gradeBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "grades/contract_$grade.png"
                )
            )
        )
        Image(
            provider = ImageProvider(gradeBitmap),
            contentDescription = "Contract Grade Icon",
            modifier = GlanceModifier.size(25.dp)
        )
    }
}

@Composable
fun CoopNameAndInfo(
    assetManager: AssetManager,
    data: CoopInfo,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val coopBitmap =
            bitmapResize(BitmapFactory.decodeStream(getAsset(assetManager, "other/icon_coop.png")))
        Image(
            provider = ImageProvider(coopBitmap),
            contentDescription = "Coop Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
        )
        if (!data.coopName.isNullOrBlank()) {
            Text(
                modifier = GlanceModifier.padding(end = 5.dp),
                text = truncateString(data.coopName, 15),
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
        if (data.maxCoopSize != 0) {
            val text =
                if (data.contributors.isEmpty()) data.maxCoopSize.toString() else "(${data.contributors.size}/${data.maxCoopSize})"
            Text(
                modifier = GlanceModifier.padding(end = 5.dp),
                text = text,
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
        if (data.coopLength != null) {
            val timerBitmap =
                bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "other/icon_timer.png"
                        )
                    )
                )
            Image(
                provider = ImageProvider(timerBitmap),
                contentDescription = "Timer Icon",
                modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
            )
            Text(
                modifier = GlanceModifier.padding(end = 5.dp),
                text = getContractTotalTimeText(data.coopLength),
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
        if (data.isLegacy) {
            Text(
                text = "Leggacy",
                style = TextStyle(color = ColorProvider(Color(0xFFFE9B00.toInt())))
            )
        } else if (!data.seasonName.isNullOrBlank()) {
            Text(
                modifier = GlanceModifier.padding(end = 5.dp),
                text = data.seasonName,
                style = TextStyle(color = ColorProvider(Color(0xFF03D0A8.toInt())))
            )
        }

        Box(modifier = GlanceModifier.defaultWeight()) {}
        if (data.tokenTimerMinutes > 0) {
            val tokenBitmap =
                bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "other/icon_token.png"
                        )
                    )
                )
            Image(
                provider = ImageProvider(tokenBitmap),
                contentDescription = "Token Icon",
                modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
            )
            Text(
                text = formatTokenTimeText(data.tokenTimerMinutes),
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
    }
}

@Composable
fun ShippingAndOfflineTime(
    assetManager: AssetManager,
    contract: ContractInfoEntry,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val shippingBitmap =
            bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "other/icon_shipping.png"
                    )
                )
            )
        val playerContributorInfo = contract.contributors.find { contributor -> contributor.isSelf }

        Image(
            provider = ImageProvider(shippingBitmap),
            contentDescription = "Shipping Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
        )

        if (playerContributorInfo != null) {
            val playerBitmap =
                bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "other/icon_player.png"
                        )
                    )
                )
            Image(
                provider = ImageProvider(playerBitmap),
                contentDescription = "Player Icon",
                modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
            )
            Text(
                modifier = GlanceModifier.padding(end = 5.dp),
                text = "${numberToString(playerContributorInfo.eggsDelivered)} ${
                    getIndividualEggsPerHour(
                        playerContributorInfo
                    )
                }",
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
        val coopBitmap =
            bitmapResize(BitmapFactory.decodeStream(getAsset(assetManager, "other/icon_coop.png")))
        Image(
            provider = ImageProvider(coopBitmap),
            contentDescription = "Coop Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
        )
        Text(
            text = "${numberToString(contract.contributors.sumOf { it.eggsDelivered })} ${
                getCoopEggsPerHour(
                    contract.contributors
                )
            }",
            style = TextStyle(color = ColorProvider(textColor))
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        if (playerContributorInfo != null) {
            val offlineBitmap =
                bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "other/icon_zzz.png"
                        )
                    )
                )
            Image(
                provider = ImageProvider(offlineBitmap),
                contentDescription = "Offline Icon",
                modifier = GlanceModifier.size(25.dp).padding(end = 5.dp)
            )
            Text(
                text = getOfflineTimeHoursAndMinutes(playerContributorInfo.offlineTimeSecondsIgnoringSilos),
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
    }
}

@Composable
fun ArtifactsAndTimeRemaining(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
    textColor: Color
) {

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (contract.contractArtifacts.isNotEmpty()) {
            contract.contractArtifacts.forEachIndexed { index, artifact ->
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
                        contentDescription = "Contract Artifact $index",
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
        Box(modifier = GlanceModifier.defaultWeight()) {}
        TimeTextAndScrollLarge(
            assetManager,
            context,
            contract,
            useAbsoluteTime,
            useOfflineTime,
            textColor
        )
    }
}

@Composable
fun TimeTextAndScrollLarge(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
    textColor: Color,
) {
    val use24HrFormat = DateFormat.is24HourFormat(context)
    var (timeText, isOnTrack) = getContractDurationRemaining(
        contract,
        useAbsoluteTime,
        use24HrFormat,
        useOfflineTime
    )

    val scrollName = getScrollName(contract, timeText)
    if (scrollName.isEmpty()) {
        timeText = "Est. $timeText"
    }

    Text(
        text = timeText,
        style = TextStyle(
            color = ColorProvider(
                Color(
                    getContractTimeTextColor(
                        contract,
                        isOnTrack,
                        textColor
                    )
                )
            )
        )
    )

    if (scrollName.isNotEmpty()) {
        val scrollBitmap =
            bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "other/$scrollName.png"
                    )
                )
            )

        Image(
            provider = ImageProvider(scrollBitmap),
            contentDescription = "Contract Scroll",
            modifier = GlanceModifier.size(20.dp).padding(start = 2.dp)
        )
    }
}

@Composable
fun Goals(
    assetManager: AssetManager,
    contract: ContractInfoEntry,
    useOfflineTime: Boolean,
    textColor: Color
) {
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val activeGoal = contract.goals
            .sortedBy { it.amount }
            .firstOrNull { getGoalPercentComplete(contract.eggsDelivered, it.amount) < 1f }

        contract.goals.sortedBy { goal -> goal.amount }.forEachIndexed { index, goal ->
            val percentComplete =
                getGoalPercentComplete(contract.eggsDelivered, goal.amount)

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val status = if (percentComplete == 1F) "complete" else "incomplete"
                val completedBitmap = bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "other/icon_goal_$status.png"
                        )
                    )
                )
                Image(
                    provider = ImageProvider(completedBitmap),
                    contentDescription = "Completed Icon $index",
                    modifier = GlanceModifier.size(25.dp).padding(end = 5.dp)
                )
                Text(
                    text = numberToString(goal.amount),
                    style = TextStyle(color = ColorProvider(textColor))
                )
                Box(modifier = GlanceModifier.defaultWeight()) {}
                val rewardBitmap = bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            getRewardIconPath(goal)
                        )
                    )
                )
                Image(
                    provider = ImageProvider(rewardBitmap),
                    contentDescription = "Reward Icon $index",
                    modifier = GlanceModifier.size(25.dp).padding(end = 5.dp)
                )
                Text(
                    text = numberToString(goal.rewardAmount),
                    style = TextStyle(color = ColorProvider(textColor))
                )
            }
            if (activeGoal != null && activeGoal.amount == goal.amount) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(start = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(7.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (useOfflineTime) {
                            val totalEggsDelivered =
                                contract.eggsDelivered + getOfflineEggsDelivered(contract.contributors)
                            val offlinePercentComplete =
                                getGoalPercentComplete(totalEggsDelivered, goal.amount)
                            LinearProgressIndicator(
                                modifier = GlanceModifier.fillMaxSize(),
                                progress = offlinePercentComplete,
                                color = ColorProvider(Color(CONTRACT_OFFLINE_PROGRESS_COLOR.toColorInt())),
                                backgroundColor = ColorProvider(Color(PROGRESS_BACKGROUND_COLOR.toColorInt()))
                            )
                        }
                        val progressBackground = if (useOfflineTime) Color.Transparent else Color(
                            PROGRESS_BACKGROUND_COLOR.toColorInt()
                        )
                        LinearProgressIndicator(
                            modifier = GlanceModifier.fillMaxSize(),
                            progress = percentComplete,
                            color = ColorProvider(Color(CONTRACT_PROGRESS_COLOR.toColorInt())),
                            backgroundColor = ColorProvider(progressBackground)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodicalGoals(
    assetManager: AssetManager,
    contract: PeriodicalsContractInfoEntry,
    textColor: Color
) {
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val pointsReplay = contract.archivedContractInfo?.pointsReplay ?: false
        val numOfGoalsAchieved = contract.archivedContractInfo?.numOfGoalsAchieved ?: 0
        val lastScore = contract.archivedContractInfo?.lastScore ?: 0.0
        if (numOfGoalsAchieved == contract.goals.size || pointsReplay || lastScore != 0.0) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (numOfGoalsAchieved == contract.goals.size || pointsReplay) {
                    Text(
                        text = "Points replay only",
                        style = TextStyle(color = ColorProvider(textColor))
                    )
                    Box(modifier = GlanceModifier.defaultWeight()) {}
                } else if (numOfGoalsAchieved < contract.goals.size) {
                    Text(
                        text = "Awaiting retry",
                        style = TextStyle(color = ColorProvider(Color.Yellow))
                    )
                    Box(modifier = GlanceModifier.defaultWeight()) {}
                }
                if (lastScore != 0.0) {
                    val lastScore = numberToString(lastScore)
                    Text(
                        text = "Last score: $lastScore",
                        style = TextStyle(color = ColorProvider(textColor))
                    )
                }
            }
        }
        contract.goals.sortedBy { goal -> goal.amount }.forEachIndexed { index, goal ->
            val isComplete = (contract.archivedContractInfo?.numOfGoalsAchieved ?: 0) >= (index + 1)

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val status = if (isComplete) "complete" else "incomplete"
                val completedBitmap = bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "other/icon_goal_$status.png"
                        )
                    )
                )
                Image(
                    provider = ImageProvider(completedBitmap),
                    contentDescription = "Completed Icon $index",
                    modifier = GlanceModifier.size(25.dp).padding(end = 5.dp)
                )
                Text(
                    text = numberToString(goal.amount),
                    style = TextStyle(color = ColorProvider(textColor))
                )
                Box(modifier = GlanceModifier.defaultWeight()) {}
                val rewardBitmap = bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            getRewardIconPath(goal)
                        )
                    )
                )
                Image(
                    provider = ImageProvider(rewardBitmap),
                    contentDescription = "Reward Icon $index",
                    modifier = GlanceModifier.size(25.dp).padding(end = 5.dp)
                )
                Text(
                    text = numberToString(goal.rewardAmount),
                    style = TextStyle(color = ColorProvider(textColor))
                )
            }
        }
    }
}

@Composable
fun SeasonContent(
    assetManager: AssetManager,
    seasonData: SeasonGradeAndGoals,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = seasonData.seasonName,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = TextUnit(18f, TextUnitType.Sp)
            )
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
        if ((seasonData.startingSeasonGrade?.number ?: 0) != 0) {
            val grade = getContractGradeName(seasonData.startingSeasonGrade?.number ?: 0)
            val gradeBitmap = bitmapResize(
                BitmapFactory.decodeStream(
                    getAsset(
                        assetManager,
                        "grades/contract_$grade.png"
                    )
                )
            )
            Image(
                provider = ImageProvider(gradeBitmap),
                contentDescription = "Season Grade Icon",
                modifier = GlanceModifier.size(25.dp)
            )
        }
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val scoreText = numberToString(seasonData.seasonScore)
        Text(
            text = "Current score: $scoreText",
            style = TextStyle(color = ColorProvider(textColor))
        )
    }

    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val activeGoal = seasonData.goals
            .sortedBy { it.amount }
            .firstOrNull { getGoalPercentComplete(seasonData.seasonScore, it.amount) < 1f }

        seasonData.goals.sortedBy { goal -> goal.amount }.forEachIndexed { index, goal ->
            val percentComplete =
                getGoalPercentComplete(seasonData.seasonScore, goal.amount)

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val status = if (percentComplete == 1F) "complete" else "incomplete"
                val completedBitmap = bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            "other/icon_goal_$status.png"
                        )
                    )
                )
                Image(
                    provider = ImageProvider(completedBitmap),
                    contentDescription = "Completed Icon $index",
                    modifier = GlanceModifier.size(25.dp).padding(end = 5.dp)
                )
                Text(
                    text = numberToString(goal.amount),
                    style = TextStyle(color = ColorProvider(textColor))
                )
                Box(modifier = GlanceModifier.defaultWeight()) {}
                val rewardBitmap = bitmapResize(
                    BitmapFactory.decodeStream(
                        getAsset(
                            assetManager,
                            getRewardIconPath(goal)
                        )
                    )
                )
                Image(
                    provider = ImageProvider(rewardBitmap),
                    contentDescription = "Reward Icon $index",
                    modifier = GlanceModifier.size(25.dp).padding(end = 5.dp)
                )
                Text(
                    text = numberToString(goal.rewardAmount),
                    style = TextStyle(color = ColorProvider(textColor))
                )
            }
            if (activeGoal != null && activeGoal.amount == goal.amount) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(start = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(7.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        LinearProgressIndicator(
                            modifier = GlanceModifier.fillMaxSize(),
                            progress = percentComplete,
                            color = ColorProvider(Color(CONTRACT_PROGRESS_COLOR.toColorInt())),
                            backgroundColor = ColorProvider(Color(PROGRESS_BACKGROUND_COLOR.toColorInt()))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlanceModifier.contractWidgetClick(
    context: Context,
    scope: CoroutineScope,
    eid: String,
    openWasmeggDashboard: Boolean
): GlanceModifier {
    return this.clickable {
        if (openWasmeggDashboard) {
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
                    "https://eicoop-carpet.netlify.app/u/$eid/".toUri()
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
}