package widget.contracts.normal

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
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
import tools.createContractCircularProgressBarBitmap
import tools.getAsset
import tools.getContractGoalPercentComplete
import tools.getEggName
import widget.contracts.ContractWidgetDataStore
import widget.contracts.ContractWidgetDataStorePreferencesKeys
import widget.contracts.ContractWidgetUpdater

class ContractWidgetNormal : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<Preferences>()
            val eid = state[ContractWidgetDataStorePreferencesKeys.EID] ?: ""
            val contractData =
                ContractWidgetDataStore().decodeContractInfo(
                    state[ContractWidgetDataStorePreferencesKeys.CONTRACT_INFO] ?: ""
                )

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
                        ContractWidgetUpdater().updateContracts(context)
                    }
            ) {
                val assetManager = context.assets
                if (eid.isBlank() || contractData.isEmpty()) {
                    NoContractsContent(assetManager)
                } else {
                    when (contractData.size) {
                        1 -> {
                            ContractSingle(contractData.first())
                        }

                        2 -> {
                            contractData.forEach { contract ->
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                                        .padding(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ContractDouble(assetManager, contract)
                                }
                            }
                        }

                        else -> {
                            contractData.forEach { contract ->
                                ContractAll(contract)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContractSingle(contract: ContractInfoEntry) {
    Text(
        text = "Hello World 1 Contract",
        style = TextStyle(
            color = ColorProvider(Color.White),
            fontSize = TextUnit(13f, TextUnitType.Sp)
        )
    )
}

@Composable
fun ContractDouble(assetManager: AssetManager, contract: ContractInfoEntry) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        val eggName = if (contract.customEggId.isNullOrBlank()) {
            getEggName(contract.eggId)
        } else {
            "egg_${contract.customEggId}"
        }

        contract.goals.sortedBy { goal -> goal.amount }.forEachIndexed { index, goal ->
            val percentRemaining =
                getContractGoalPercentComplete(contract.eggsDelivered, goal.amount)
            val bitmap = createContractCircularProgressBarBitmap(
                percentRemaining,
                150,
            )

            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Circular Progress",
                modifier = GlanceModifier.size(((index + 6) * 10).dp)
            )
        }


        val eggBitmap = BitmapFactory.decodeStream(getAsset(assetManager, "eggs/$eggName.png"))
        Image(
            provider = ImageProvider(eggBitmap),
            contentDescription = "Egg Icon",
            modifier = GlanceModifier.size(45.dp)
        )
    }

    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = contract.name,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = TextUnit(13f, TextUnitType.Sp)
            )
        )
    }
}

@Composable
fun ContractAll(contract: ContractInfoEntry) {
    Text(
        text = "Hello World 4 Contracts",
        style = TextStyle(
            color = ColorProvider(Color.White),
            fontSize = TextUnit(13f, TextUnitType.Sp)
        )
    )
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
            text = "Waiting for contract data...",
            style = TextStyle(color = ColorProvider(Color.White)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}