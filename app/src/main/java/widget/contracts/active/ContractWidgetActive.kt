package widget.contracts.active

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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.ContractInfoEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.createContractCircularProgressBarBitmap
import tools.utilities.getAsset
import tools.utilities.getContractDurationRemaining
import tools.utilities.getContractGoalPercentComplete
import tools.utilities.getContractTimeTextColor
import tools.utilities.getEggName
import tools.utilities.getScrollName
import widget.contracts.ContractWidgetDataStore
import widget.contracts.ContractWidgetDataStorePreferencesKeys
import widget.contracts.ContractWidgetUpdater
import androidx.core.net.toUri
import tools.utilities.getRewardIconPath

class ContractWidgetActive : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val eid = state[ContractWidgetDataStorePreferencesKeys.EID] ?: ""
            val contractData =
                ContractWidgetDataStore().decodeContractInfo(
                    state[ContractWidgetDataStorePreferencesKeys.CONTRACT_INFO] ?: ""
                )
            val useAbsoluteTime =
                state[ContractWidgetDataStorePreferencesKeys.USE_ABSOLUTE_TIME] ?: false
            val useOfflineTime =
                state[ContractWidgetDataStorePreferencesKeys.USE_OFFLINE_TIME] ?: false
            val openWasmeggDashboard =
                state[ContractWidgetDataStorePreferencesKeys.OPEN_WASMEGG_DASHBOARD] ?: false

            if (eid.isBlank()) {
                // If EID is blank, could either mean state is not initialized or user is not logged in
                // Attempt to load state in case it is needed, otherwise login composable will show
                LaunchedEffect(true) {
                    CoroutineScope(context = Dispatchers.IO).launch {
                        ContractWidgetUpdater().updateContracts(context)
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
                        if (openWasmeggDashboard) {
                            val problematicBrowsers =
                                listOf("org.mozilla.firefox", "com.duckduckgo.mobile.android")
                            val packageManager: PackageManager = context.packageManager
                            var browserPackage: String? = packageManager.resolveActivity(
                                Intent(Intent.ACTION_VIEW, "https://www.example.com".toUri()),
                                PackageManager.MATCH_DEFAULT_ONLY
                            )?.activityInfo?.packageName

                            if (browserPackage != null) {
                                // Not all browsers play nicely with opening a link from a widget
                                // If using any of these browsers, attempt to use chrome instead
                                if (browserPackage in problematicBrowsers) {
                                    browserPackage = "com.android.chrome"
                                }
                                val launchIntent: Intent? =
                                    packageManager.getLaunchIntentForPackage(browserPackage)
                                launchIntent?.data =
                                    "https://eicoop-carpet.netlify.app/u/$eid/".toUri()
                                context.startActivity(launchIntent)
                            }
                        } else {
                            ContractWidgetUpdater().updateContracts(context)
                        }
                    }
            ) {
                val assetManager = context.assets
                if (eid.isBlank() || contractData.isEmpty()) {
                    NoContractsContent(assetManager)
                } else {
                    when (contractData.size) {
                        1 -> {
                            ContractSingle(
                                assetManager,
                                context,
                                contractData.first(),
                                useAbsoluteTime,
                                useOfflineTime
                            )
                        }

                        2 -> {
                            contractData.forEach { contract ->
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                                        .padding(5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    ContractDouble(
                                        assetManager,
                                        context,
                                        contract,
                                        useAbsoluteTime,
                                        useOfflineTime
                                    )
                                }
                            }
                        }

                        else -> {
                            val contractsChunked = contractData.chunked(2)
                            contractsChunked.forEach { contractGroup ->
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                                        .padding(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    contractGroup.forEach { contract ->
                                        Column(
                                            modifier = GlanceModifier.defaultWeight(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ContractAll(
                                                assetManager,
                                                context,
                                                contract,
                                                useAbsoluteTime,
                                                useOfflineTime
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
    }
}

@Composable
fun ContractSingle(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        EggAndProgressBars(assetManager, contract, 75, 10)
    }

    Text(
        modifier = GlanceModifier.padding(top = 5.dp),
        text = contract.name,
        style = TextStyle(color = ColorProvider(Color.White))
    )

    TimeTextAndScroll(assetManager, context, contract, useAbsoluteTime, useOfflineTime)
    SeasonAndRewardInfo(assetManager, contract)
}

@Composable
fun ContractDouble(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
) {
    Box(
        modifier = GlanceModifier.padding(start = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        EggAndProgressBars(assetManager, contract, 45, 6)
    }

    Column(
        modifier = GlanceModifier.padding(start = 2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = contract.name,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = TextUnit(13f, TextUnitType.Sp)
            )
        )

        TimeTextAndScroll(assetManager, context, contract, useAbsoluteTime, useOfflineTime, 13f, 15)
        SeasonAndRewardInfo(assetManager, contract, 13f, 20)
    }
}

@Composable
fun ContractAll(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        EggAndProgressBars(assetManager, contract, 30, 4)
    }

    Text(
        modifier = GlanceModifier.padding(top = 5.dp),
        text = contract.name,
        style = TextStyle(
            color = ColorProvider(Color.White),
            fontSize = TextUnit(10f, TextUnitType.Sp)
        )
    )

    TimeTextAndScroll(assetManager, context, contract, useAbsoluteTime, useOfflineTime, 10f, 12)
}

@Composable
fun LogoContentContracts(assetManager: AssetManager) {
    val bitmapImage =
        BitmapFactory.decodeStream(getAsset(assetManager, "icons/logo-dark-mode.png"))

    Image(
        provider = ImageProvider(bitmapImage),
        contentDescription = "Empty Widget Logo",
        modifier = GlanceModifier.size(80.dp)
    )
}

@Composable
fun NoContractsContent(assetManager: AssetManager) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoContentContracts(assetManager)
        Text(
            text = "No active contracts...",
            style = TextStyle(color = ColorProvider(Color.White)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun EggAndProgressBars(
    assetManager: AssetManager,
    contract: ContractInfoEntry,
    eggSize: Int,
    progressSize: Int
) {
    val eggName = if (contract.customEggId.isNullOrBlank()) {
        getEggName(contract.eggId)
    } else {
        "egg_${contract.customEggId}"
    }

    contract.goals.sortedBy { goal -> goal.goalAmount }.forEachIndexed { index, goal ->
        val percentComplete =
            getContractGoalPercentComplete(contract.eggsDelivered, goal.goalAmount)
        val bitmap = createContractCircularProgressBarBitmap(
            percentComplete,
            150,
        )

        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Circular Progress",
            modifier = GlanceModifier.size(((index + progressSize) * 10).dp)
        )
    }


    val eggBitmap = BitmapFactory.decodeStream(getAsset(assetManager, "eggs/$eggName.png"))
    Image(
        provider = ImageProvider(eggBitmap),
        contentDescription = "Egg Icon",
        modifier = GlanceModifier.size(eggSize.dp)
    )
}

@Composable
fun TimeTextAndScroll(
    assetManager: AssetManager,
    context: Context,
    contract: ContractInfoEntry,
    useAbsoluteTime: Boolean,
    useOfflineTime: Boolean,
    textSize: Float = 14f,
    scrollSize: Int = 20
) {
    val use24HrFormat = DateFormat.is24HourFormat(context)
    val (timeText, isOnTrack) = getContractDurationRemaining(
        contract,
        useAbsoluteTime,
        use24HrFormat,
        useOfflineTime
    )

    Row {
        Text(
            text = timeText,
            style = TextStyle(
                color = ColorProvider(Color(getContractTimeTextColor(contract, isOnTrack))),
                fontSize = TextUnit(textSize, TextUnitType.Sp)
            )
        )

        val scrollName = getScrollName(contract, timeText)
        if (scrollName.isNotEmpty()) {
            val scrollBitmap =
                BitmapFactory.decodeStream(getAsset(assetManager, "other/$scrollName.png"))

            Image(
                provider = ImageProvider(scrollBitmap),
                contentDescription = "Contract Scroll",
                modifier = GlanceModifier.size(scrollSize.dp).padding(start = 1.dp)
            )
        }
    }
}

@Composable
fun SeasonAndRewardInfo(
    assetManager: AssetManager,
    contract: ContractInfoEntry,
    textSize: Float = 14f,
    rewardSize: Int = 22
) {
    if (!contract.seasonName.isNullOrBlank()) {
        Text(
            text = contract.seasonName!!,
            style = TextStyle(
                color = ColorProvider(Color(0xFF03D0A8.toInt())),
                fontSize = TextUnit(textSize, TextUnitType.Sp)
            )
        )
    }

    if (contract.isLegacy) {
        Text(
            text = "Leggacy",
            style = TextStyle(
                color = ColorProvider(Color(0xFFFE9B00.toInt())),
                fontSize = TextUnit(textSize, TextUnitType.Sp)
            )
        )
    }

    Row {
        contract.goals.forEachIndexed { index, goal ->
            val rewardBitmap =
                BitmapFactory.decodeStream(getAsset(assetManager, getRewardIconPath(goal)))

            Image(
                provider = ImageProvider(rewardBitmap),
                contentDescription = "Reward Icon $index",
                modifier = GlanceModifier.size(rewardSize.dp).padding(horizontal = 1.dp)
            )
        }
    }
}