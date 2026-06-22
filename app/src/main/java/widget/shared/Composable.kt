package widget.shared

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import data.Artifact
import tools.utilities.bitmapResize
import tools.utilities.createGlowBitmap
import tools.utilities.getAsset
import tools.utilities.getImageNameFromAfxId

@Composable
fun LogoContent(assetManager: AssetManager, logoSize: Dp) {
    val bitmapImage =
        BitmapFactory.decodeStream(getAsset(assetManager, "icons/logo-dark-mode.png"))

    Image(
        provider = ImageProvider(bitmapImage),
        contentDescription = "Empty Widget Logo",
        modifier = GlanceModifier.size(logoSize)
    )
}

@Composable
fun NoWidgetContent(
    assetManager: AssetManager,
    contentMessage: String,
    logoSize: Dp,
    textColor: Color
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoContent(assetManager, logoSize)
        Text(
            text = contentMessage,
            style = TextStyle(color = ColorProvider(textColor)),
            modifier = GlanceModifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun ArtifactsContent(assetManager: AssetManager, artifacts: List<Artifact>) {
    if (artifacts.isNotEmpty()) {
        artifacts.forEachIndexed { index, artifact ->
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
}