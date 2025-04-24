package tools.utilities

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import java.io.InputStream

fun createCircularProgressBarBitmap(
    progress: Float,
    color: Int,
    size: Int,
    width: Float
): Bitmap {
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = width
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    paint.color = "#464646".toColorInt()
    val radius = size / 2f - paint.strokeWidth / 2
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    paint.color = color
    val sweepAngle = 360 * progress
    canvas.drawArc(
        paint.strokeWidth / 2,
        paint.strokeWidth / 2,
        size - paint.strokeWidth / 2,
        size - paint.strokeWidth / 2,
        90f,
        sweepAngle,
        false,
        paint
    )

    return bitmap
}

fun bitmapResize(image: Bitmap): Bitmap {
    val width = image.width
    val height = image.height
    val aspectRatio = width / height
    val newWidth = 100
    return if (width > newWidth) {
        val newHeight = newWidth * aspectRatio
        image.scale(newWidth, newHeight, false)
    } else {
        image
    }
}

fun getAsset(assetManager: AssetManager, path: String): InputStream {
    return try {
        assetManager.open(path)
    } catch (_: Exception) {
        assetManager.open("eggs/egg_unknown.png")
    }
}