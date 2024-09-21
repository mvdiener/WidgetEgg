package widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.MissionInfoEntry
import kotlinx.coroutines.runBlocking
import tools.getMissionPercentComplete
import user.preferences.PreferencesDatastore

class MissionWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            var preferencesMissionData: List<MissionInfoEntry>
            var prefEid: String
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            ) {
                runBlocking {
                    val preferences = PreferencesDatastore(context)
                    preferencesMissionData = preferences.getMissionInfo()
                    prefEid = preferences.getEid()
                }

                if (prefEid.isBlank() || preferencesMissionData.isEmpty()) {
                    val assetManager = context.assets
                    val bitmapImage =
                        BitmapFactory.decodeStream(assetManager.open("icons/logo-dark-mode.png"))

                    Image(
                        provider = ImageProvider(bitmapImage),
                        contentDescription = null,
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                } else {
                    preferencesMissionData.forEach { mission ->
                        val percentRemaining = getMissionPercentComplete(
                            mission.missionDuration,
                            mission.secondsRemaining,
                            mission.date
                        )
                        Text(
                            text = "Mission is $percentRemaining% complete",
                            style = TextStyle(
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                }
            }
        }
    }
}
