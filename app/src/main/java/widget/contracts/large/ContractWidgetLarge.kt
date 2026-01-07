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
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
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
import data.DEFAULT_BROWSER
import data.DEFAULT_WIDGET_BACKGROUND_COLOR
import data.DEFAULT_WIDGET_TEXT_COLOR
import data.PROBLEMATIC_BROWSERS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.utilities.bitmapResize
import tools.utilities.formatTokenTimeText
import tools.utilities.getAsset
import tools.utilities.getContractDurationRemaining
import tools.utilities.getContractGradeName
import tools.utilities.getContractTimeTextColor
import tools.utilities.getCoopEggsPerHour
import tools.utilities.getEggName
import tools.utilities.getIndividualEggsPerHour
import tools.utilities.getOfflineTimeHoursAndMinutes
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
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .contractWidgetClick(context, scope, eid, openWasmeggDashboard)
                ) {
                    for (contract in contractData) {
                        item {
                            Column(
                                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 5.dp)
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
    Artifacts(assetManager, contract, textColor)
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
            modifier = GlanceModifier.size(30.dp)
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
        modifier = GlanceModifier.fillMaxWidth().padding(start = 8.dp, end = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val coopBitmap = BitmapFactory.decodeStream(getAsset(assetManager, "other/icon_coop.png"))
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
                BitmapFactory.decodeStream(getAsset(assetManager, "other/icon_token.png"))
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
        modifier = GlanceModifier.fillMaxWidth().padding(start = 8.dp, end = 5.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val shippingBitmap =
            BitmapFactory.decodeStream(getAsset(assetManager, "other/icon_shipping.png"))
        val playerContributorInfo = contract.contributors.find { contributor -> contributor.isSelf }

        Image(
            provider = ImageProvider(shippingBitmap),
            contentDescription = "Shipping Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
        )

        if (playerContributorInfo != null) {
            val playerBitmap =
                BitmapFactory.decodeStream(getAsset(assetManager, "other/icon_player.png"))
            Image(
                provider = ImageProvider(playerBitmap),
                contentDescription = "Player Icon",
                modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
            )
            Text(
                modifier = GlanceModifier.padding(end = 5.dp),
                text = "${numberToString(playerContributorInfo.eggsDelivered)}, ${
                    getIndividualEggsPerHour(
                        playerContributorInfo
                    )
                }",
                style = TextStyle(color = ColorProvider(textColor))
            )
        }
        val coopBitmap = BitmapFactory.decodeStream(getAsset(assetManager, "other/icon_coop.png"))
        Image(
            provider = ImageProvider(coopBitmap),
            contentDescription = "Coop Icon",
            modifier = GlanceModifier.size(20.dp).padding(end = 5.dp)
        )
        Text(
            text = "${numberToString(contract.contributors.sumOf { it.eggsDelivered })}, ${
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
        modifier = GlanceModifier.fillMaxWidth().padding(start = 8.dp, end = 5.dp),
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
                BitmapFactory.decodeStream(getAsset(assetManager, "other/icon_zzz.png"))
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
    val (timeText, isOnTrack) = getContractDurationRemaining(
        contract,
        useAbsoluteTime,
        use24HrFormat,
        useOfflineTime
    )

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

    val scrollName = getScrollName(contract, timeText)
    if (scrollName.isNotEmpty()) {
        val scrollBitmap =
            BitmapFactory.decodeStream(getAsset(assetManager, "other/$scrollName.png"))

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
    textColor: Color
) {
    if (contract.contractArtifacts.isNotEmpty()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 8.dp, end = 5.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

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