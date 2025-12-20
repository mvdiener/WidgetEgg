package widget.contracts.large

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
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