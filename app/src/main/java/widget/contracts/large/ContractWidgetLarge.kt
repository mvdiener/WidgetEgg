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
import data.DEFAULT_BROWSER
import data.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.DEFAULT_WIDGET_TEXT_COLOR
import data.PROBLEMATIC_BROWSERS
import data.PROGRESS_BACKGROUND_COLOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.bitmapResize
import tools.utilities.createGlowBitmap
import tools.utilities.formatTokenTimeText
import tools.utilities.getAsset
import tools.utilities.getContractDurationRemaining
import tools.utilities.getContractGoalPercentComplete
import tools.utilities.getContractGradeName
import tools.utilities.getContractTimeTextColor
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
            val useAbsoluteTime =
                state[ContractWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] == true
            val useOfflineTime =
                state[ContractWidgetDataStorePreferencesKeys.USE_OFFLINE_TIME] == true
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
            if (eid.isBlank() || contractData.isEmpty() /*&& !showAvailableContracts*/) {
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
            } else if (contractData.size == 1) {
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
            } else {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.fillMaxSize().background(backgroundColor)
                ) {
                    item {
                        Spacer(modifier = GlanceModifier.height(10.dp))
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
            text = "No active contracts...",
            style = TextStyle(color = ColorProvider(textColor)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun ContractContentLarge(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
    textColor: Color
) {
    EggAndGrade(assetManager, contract, textColor)
    CoopNameAndInfo(assetManager, contract, textColor)
    Shipping(assetManager, contract, textColor)
    TimeRemainingAndOfflineTime(
        assetManager,
        context,
        contract,
        useAbsoluteTime,
        useOfflineTime,
        textColor
    )
    Artifacts(assetManager, contract)
    Goals(assetManager, contract, useOfflineTime, textColor)
}

@Composable
fun EggAndGrade(
    assetManager: AssetManager,
    contract: ContractInfoEntry,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val eggName = if (contract.customEggId.isNullOrBlank()) {
            getEggName(contract.eggId)
        } else {
            "egg_${contract.customEggId}"
        }
        val eggBitmap =
            bitmapResize(BitmapFactory.decodeStream(getAsset(assetManager, "eggs/$eggName.png")))

        val grade = getContractGradeName(contract.grade)
        val gradeBitmap = bitmapResize(
            BitmapFactory.decodeStream(
                getAsset(
                    assetManager,
                    "grades/contract_$grade.png"
                )
            )
        )

        Image(
            provider = ImageProvider(eggBitmap),
            contentDescription = "Egg Icon",
            modifier = GlanceModifier.size(30.dp).padding(end = 5.dp)
        )
        Text(
            text = contract.name,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = TextUnit(18f, TextUnitType.Sp)
            )
        )
        Box(modifier = GlanceModifier.defaultWeight()) {}
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
    contract: ContractInfoEntry,
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
        Text(
            modifier = GlanceModifier.padding(end = 5.dp),
            text = truncateString(contract.coopName, 15),
            style = TextStyle(color = ColorProvider(textColor))
        )
        if (contract.maxCoopSize != 0) {
            Text(
                modifier = GlanceModifier.padding(end = 5.dp),
                text = "(${contract.contributors.size}/${contract.maxCoopSize})",
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
        if (!contract.seasonName.isNullOrBlank()) {
            Text(
                modifier = GlanceModifier.padding(end = 5.dp),
                text = contract.seasonName!!,
                style = TextStyle(color = ColorProvider(Color(0xFF03D0A8.toInt())))
            )
        }
        if (contract.isLegacy) {
            Text(
                text = "Leggacy",
                style = TextStyle(color = ColorProvider(Color(0xFFFE9B00.toInt())))
            )
        }
        Box(modifier = GlanceModifier.defaultWeight()) {}
        if (contract.tokenTimerMinutes > 0) {
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
                text = formatTokenTimeText(contract.tokenTimerMinutes),
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
    }
}

@Composable
fun Shipping(
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
    }
}

@Composable
fun TimeRemainingAndOfflineTime(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
    textColor: Color
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val playerContributorInfo = contract.contributors.find { contributor -> contributor.isSelf }

        TimeTextAndScrollLarge(
            assetManager,
            context,
            contract,
            useAbsoluteTime,
            useOfflineTime,
            textColor
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
                modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
            )
            Text(
                text = getOfflineTimeHoursAndMinutes(playerContributorInfo),
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
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
fun Artifacts(
    assetManager: AssetManager,
    contract: ContractInfoEntry,
) {
    if (contract.contractArtifacts.isNotEmpty()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 5.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    modifier = GlanceModifier.size(40.dp).padding(start = 2.dp),
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
                        modifier = GlanceModifier.size(30.dp)
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
                                        modifier = GlanceModifier.size(10.dp)
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
            .firstOrNull { getContractGoalPercentComplete(contract.eggsDelivered, it.amount) < 1f }

        contract.goals.sortedBy { goal -> goal.amount }.forEachIndexed { index, goal ->
            val percentComplete =
                getContractGoalPercentComplete(contract.eggsDelivered, goal.amount)

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
                                contract.eggsDelivered + getOfflineEggsDelivered(contract)
                            val offlinePercentComplete =
                                getContractGoalPercentComplete(totalEggsDelivered, goal.amount)
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