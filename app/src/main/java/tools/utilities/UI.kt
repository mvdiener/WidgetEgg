package tools.utilities

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import data.NUMBER_UNITS
import ei.Ei
import java.io.InputStream
import java.util.Locale

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

// Widgets have a maximum bitmap memory that will cause a render failure if it is exceeded
// This resize function scales down images. Image quality is worse, but not noticeable if image is already small
// Useful in places like fuel tank egg icons, target artifacts, contract rewards, or stats icons
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

fun getEggName(eggId: Int): String {
    val eggName = Ei.Egg.forNumber(eggId)?.name?.lowercase()
    return if (eggName.isNullOrBlank()) {
        "egg_unknown"
    } else {
        "egg_$eggName"
    }
}

fun getImageNameFromAfxId(afxId: Int): String {
    return when (afxId) {
        23 -> "afx_puzzle_cube_4"
        0 -> "afx_lunar_totem_4"
        6 -> "afx_demeters_necklace_4"
        7 -> "afx_vial_martian_dust_4"
        21 -> "afx_aurelian_brooch_4"
        12 -> "afx_tungsten_ankh_4"
        8 -> "afx_ornate_gusset_4"
        3 -> "afx_neo_medallion_4"
        30 -> "afx_mercurys_lens_4"
        4 -> "afx_beak_of_midas_4"
        22 -> "afx_carved_rainstick_4"
        27 -> "afx_interstellar_compass_4"
        9 -> "afx_the_chalice_4"
        11 -> "afx_phoenix_feather_4"
        24 -> "afx_quantum_metronome_4"
        28 -> "afx_dilithium_monocle_4"
        29 -> "afx_titanium_actuator_4"
        25 -> "afx_ship_in_a_bottle_4"
        26 -> "afx_tachyon_deflector_4"
        10 -> "afx_book_of_basan_4"
        5 -> "afx_light_eggendil_4"
        33 -> "afx_lunar_stone_4"
        32 -> "afx_shell_stone_4"
        1 -> "afx_tachyon_stone_4"
        37 -> "afx_terra_stone_4"
        34 -> "afx_soul_stone_4"
        31 -> "afx_dilithium_stone_4"
        36 -> "afx_quantum_stone_4"
        38 -> "afx_life_stone_4"
        40 -> "afx_clarity_stone_4"
        39 -> "afx_prophecy_stone_4"
        17 -> "afx_gold_meteorite_3"
        18 -> "afx_tau_ceti_geode_3"
        43 -> "afx_solar_titanium_3"
        else -> ""
    }
}

fun numberToString(amount: Double): String {
    var number = amount
    var unit = ""

    if (number < 1000) {
        return number.toInt().toString()
    }

    var units = NUMBER_UNITS

    while (number >= 1000) {
        number /= 1000
        if (units.isEmpty()) {
            return "Inf"
        }
        unit = units.first()
        units = units.drop(1).toTypedArray()
    }

    var formatted = String.Companion.format(Locale.ROOT, "%.3g", number)

    // If using %.3g for the string format it _sometimes_ ends up in sci. notation
    // It seems to only be cases where it's right below the threshold of the next power of ten
    // e.g. 999,999,999,999.999999 becomes 1.00+e03B
    // In this case, strip off the +e03 and bump the unit to the next tier letter

    if (formatted.contains("e+")) {
        val split = formatted.split("e+")
        val unitIndex = units.indexOf(unit)
        unit = units[unitIndex + 1]
        formatted = split[0]
    }

    return "$formatted$unit"
}