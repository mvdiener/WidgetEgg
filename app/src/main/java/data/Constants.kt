package data

import androidx.compose.ui.graphics.Color

const val API_ROOT = "https://www.auxbrain.com"
const val MISSION_ENDPOINT = "${API_ROOT}/ei_afx/get_active_missions"
const val BACKUP_ENDPOINT = "${API_ROOT}/ei/bot_first_contact"
const val CONTRACT_ENDPOINT = "${API_ROOT}/ei/coop_status"

const val CURRENT_CLIENT_VERSION = 69

val DEFAULT_WIDGET_BACKGROUND_COLOR = Color(0xff181818)
val DEFAULT_WIDGET_TEXT_COLOR = Color.White

val ALL_SHIPS = arrayOf(
    "afx_ship_chicken_1",
    "afx_ship_chicken_9",
    "afx_ship_chicken_heavy",
    "afx_ship_bcr",
    "afx_ship_millenium_chicken",
    "afx_ship_corellihen_corvette",
    "afx_ship_galeggtica",
    "afx_ship_defihent",
    "afx_ship_voyegger",
    "afx_ship_henerprise",
    "afx_ship_atreggies"
)

val TANK_SIZES = arrayOf(
    2000000000,
    200000000000,
    10000000000000,
    100000000000000,
    200000000000000,
    300000000000000,
    400000000000000,
    500000000000000
)

val NUMBER_UNITS = arrayOf(
    "k",
    "M",
    "B",
    "T",
    "q",
    "Q",
    "s",
    "S",
    "o",
    "N",
    "d",
    "U",
    "D",
    "Td",
    "qd",
    "Qd",
    "sd",
    "Sd",
    "Od",
    "Nd",
    "V",
    "uV",
    "dV",
    "tV",
    "qV",
    "QV",
    "sV",
    "SV",
    "OV",
    "NV",
    "tT"
)