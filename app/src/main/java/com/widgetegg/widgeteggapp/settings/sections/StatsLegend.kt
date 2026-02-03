package com.widgetegg.widgeteggapp.settings.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.widgetegg.widgeteggapp.R
import com.widgetegg.widgeteggapp.settings.ScrollBottomPadding
import com.widgetegg.widgeteggapp.settings.SettingsHeader
import com.widgetegg.widgeteggapp.settings.SettingsViewModel
import kotlinx.coroutines.runBlocking
import user.preferences.PreferencesDatastore

@Composable
fun StatsLegend(navController: NavController) {
    val settingsViewModel = viewModel<SettingsViewModel>()

    val context = LocalContext.current

    runBlocking {
        val preferences = PreferencesDatastore(context)
        settingsViewModel.updateShowCommunityBadges(preferences.getShowCommunityBadges())
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsHeader(
            text = "Back to Stats Screen",
            onClick = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Stats Legend", fontSize = TextUnit(24f, TextUnitType.Sp))
            StatsImage()
            StatsDescriptions()
            ScrollBottomPadding()
        }
    }
}

@Composable
fun StatsImage() {
    val painter = painterResource(id = R.drawable.stats_legend)
    Image(
        painter = painter,
        contentDescription = "Stats Legend Image",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 100.dp, vertical = 15.dp),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun StatsDescriptions() {
    Text(
        modifier = Modifier.padding(16.dp),
        text = """
            1. Name
            2. Egg, Inc. Discord role
            3. Soul eggs
            4. Prophecy eggs
            5. Truth eggs, or permit level if no truth eggs
            6. Earnings bonus w/ Egg, Inc. Discord role color
            7. Golden eggs
            8. Shell tickets
            9. Home farm egg
            10. Home farm population
            11. Seasonal contract score
            12. Total contract score
            13. Ships launched
            14. Drones taken down
            15. Crafting level
            16. Crafting XP
            17. Community badges (if enabled)
            
            Badge List
            Trophy: Enlightenment Diamond completed
            NAH: Enlightenment max population reached
            NAH (red): NAH including hab space colleggtible multipliers
            FED: All farms max population reached (not including virtue or contract colleggtibles)
            FED (red): FED including hab space colleggtible multipliers
            ZLC: No legendary artifacts owned
            ALC: All types of legendary artifacts owned
            ASC: Maximum stars on all ships reached
            Anvil: Maximum crafting level reached
        """.trimIndent()
    )
}