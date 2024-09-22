package widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.MissionInfoEntry
import kotlinx.coroutines.runBlocking
import tools.createCircularProgressBarBitmap
import tools.getMissionDurationRemaining
import tools.getMissionPercentComplete
import tools.getShipName
import user.preferences.PreferencesDatastore

class MissionWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            var preferencesMissionData: List<MissionInfoEntry>
            var prefEid: String
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xff181818))
            ) {
                runBlocking {
                    val preferences = PreferencesDatastore(context)
                    preferencesMissionData = preferences.getMissionInfo()
                    prefEid = preferences.getEid()
                }

                val assetManager = context.assets
                if (prefEid.isBlank() || preferencesMissionData.isEmpty()) {
                    val bitmapImage =
                        BitmapFactory.decodeStream(assetManager.open("icons/logo-dark-mode.png"))

                    Image(
                        provider = ImageProvider(bitmapImage),
                        contentDescription = "Empty Widget Logo",
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        preferencesMissionData.forEach { mission ->
                            val percentRemaining = getMissionPercentComplete(
                                mission.missionDuration,
                                mission.secondsRemaining,
                                mission.date
                            )

                            Box(
                                modifier = GlanceModifier.size(100.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = GlanceModifier.fillMaxSize().padding(bottom = 15.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = createCircularProgressBarBitmap(
                                        percentRemaining,
                                        mission.durationType,
                                        100
                                    )
                                    Image(
                                        provider = ImageProvider(bitmap),
                                        contentDescription = "Circular Progress",
                                        modifier = GlanceModifier.size(70.dp)
                                    )

                                    val shipName = getShipName(mission.shipId)
                                    val shipBitmap =
                                        BitmapFactory.decodeStream(assetManager.open("ships/${shipName}.png"))
                                    Image(
                                        provider = ImageProvider(shipBitmap),
                                        contentDescription = "Ship Icon",
                                        modifier = GlanceModifier.size(50.dp)
                                    )
                                }

                                Column(
                                    modifier = GlanceModifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = getMissionDurationRemaining(
                                            mission.secondsRemaining,
                                            mission.date
                                        ),
                                        style = TextStyle(color = ColorProvider(Color.White)),
                                        modifier = GlanceModifier.padding(top = 5.dp)
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
