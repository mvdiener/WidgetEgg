package com.example.widgetegg

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import api.fetchData
import data.MissionInfoEntry
import kotlinx.coroutines.runBlocking
import tools.getMissionPercentComplete
import user.preferences.PreferencesDatastore
import java.time.Instant

class MissionWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            var missionRemainingTimes: List<Double> = emptyList()
            var preferencesMissionData: List<MissionInfoEntry>
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            ) {
                runBlocking {
                    val preferences = PreferencesDatastore(context)
                    preferencesMissionData = preferences.getMissionInfo()

                    if (preferencesMissionData.isEmpty()) {
                        val prefEid = preferences.getEid()
                        val missionInfo = fetchData(prefEid)
                        missionInfo.missions.forEach { mission ->
                            if (mission.identifier.isNotBlank()) {
                                missionRemainingTimes =
                                    missionRemainingTimes.plus(mission.secondsRemaining)
                                preferencesMissionData = preferencesMissionData.plus(
                                    MissionInfoEntry(
                                        secondsRemaining = if (mission.secondsRemaining >= 0) mission.secondsRemaining else 0.0,
                                        missionDuration = mission.durationSeconds,
                                        date = Instant.now().epochSecond
                                    )
                                )
                            }
                        }
                    }
                }

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
                    mission.date = Instant.now().epochSecond
                }

                runBlocking {
                    val preferences = PreferencesDatastore(context)
                    preferences.saveMissionInfo(preferencesMissionData)
                }

                Button(
                    text = "Refresh mission times",
                    onClick = actionRunCallback(ReloadWidgetCallback::class.java)
                )
            }
        }
    }
}

class MissionWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MissionWidget()
}

class ReloadWidgetCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        MissionWidget().update(context, glanceId)
    }
}