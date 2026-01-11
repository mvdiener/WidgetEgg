package data

import androidx.compose.ui.graphics.Color

const val API_ROOT = "https://www.auxbrain.com"
const val MISSION_ENDPOINT = "${API_ROOT}/ei_afx/get_active_missions_v2"
const val BACKUP_ENDPOINT = "${API_ROOT}/ei/bot_first_contact"
const val CONTRACT_ENDPOINT = "${API_ROOT}/ei/coop_status"
const val PERIODICALS_ENDPOINT = "${API_ROOT}/ei/get_periodicals"
const val CONTRACTS_ARCHIVE_ENDPOINT = "${API_ROOT}/ei_ctx/get_contracts_archive"

const val CURRENT_CLIENT_VERSION = 99

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

val SHIP_MAX_LAUNCH_POINTS = arrayOf(
    0,
    14,
    45,
    85,
    125,
    125,
    185,
    185,
    255,
    435,
    1420
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

val ALL_ROLES = arrayOf(
    Pair("Farmer 1", Color(0xffd43500)),
    Pair("Farmer 1", Color(0xffd43500)),
    Pair("Farmer 1", Color(0xffd43500)),
    Pair("Farmer 2", Color(0xffd14400)),
    Pair("Farmer 3", Color(0xffcd5500)),
    Pair("Kilofarmer 1", Color(0xffca6800)),
    Pair("Kilofarmer 2", Color(0xffc77a00)),
    Pair("Kilofarmer 3", Color(0xffc58a00)),
    Pair("Megafarmer 1", Color(0xffc49400)),
    Pair("Megafarmer 2", Color(0xffc39f00)),
    Pair("Megafarmer 3", Color(0xffc3a900)),
    Pair("Gigafarmer 1", Color(0xffc2b100)),
    Pair("Gigafarmer 2", Color(0xffc2ba00)),
    Pair("Gigafarmer 3", Color(0xffc2c200)),
    Pair("Terafarmer 1", Color(0xffaec300)),
    Pair("Terafarmer 2", Color(0xff99c400)),
    Pair("Terafarmer 3", Color(0xff85c600)),
    Pair("Petafarmer 1", Color(0xff51ce00)),
    Pair("Petafarmer 2", Color(0xff16dc00)),
    Pair("Petafarmer 3", Color(0xff00ec2e)),
    Pair("Exafarmer 1", Color(0xff00fa68)),
    Pair("Exafarmer 2", Color(0xff0afc9c)),
    Pair("Exafarmer 3", Color(0xff1cf7ca)),
    Pair("Zettafarmer 1", Color(0xff2af3eb)),
    Pair("Zettafarmer 2", Color(0xff35d9f0)),
    Pair("Zettafarmer 3", Color(0xff40bced)),
    Pair("Yottafarmer 1", Color(0xff46a8eb)),
    Pair("Yottafarmer 2", Color(0xff4a9aea)),
    Pair("Yottafarmer 3", Color(0xff4e8dea)),
    Pair("Xennafarmer 1", Color(0xff527ce9)),
    Pair("Xennafarmer 2", Color(0xff5463e8)),
    Pair("Xennafarmer 3", Color(0xff6155e8)),
    Pair("Weccafarmer 1", Color(0xff7952e9)),
    Pair("Weccafarmer 2", Color(0xff8b4fe9)),
    Pair("Weccafarmer 3", Color(0xff9d4aeb)),
    Pair("Vendafarmer 1", Color(0xffb343ec)),
    Pair("Vendafarmer 2", Color(0xffd636ef)),
    Pair("Vendafarmer 3", Color(0xfff327e5)),
    Pair("Uadafarmer 1", Color(0xfff915ba)),
    Pair("Uadafarmer 2", Color(0xfffc0a9c)),
    Pair("Uadafarmer 3", Color(0xffff007d)),
    Pair("Treidafarmer 1", Color(0xfff7005d)),
    Pair("Treidafarmer 2", Color(0xfff61fd2)),
    Pair("Treidafarmer 3", Color(0xff9c4aea)),
    Pair("Quadafarmer 1", Color(0xff5559e8)),
    Pair("Quadafarmer 2", Color(0xff4a9deb)),
    Pair("Quadafarmer 3", Color(0xff2df0f2)),
    Pair("Pendafarmer 1", Color(0xff00f759)),
    Pair("Pendafarmer 2", Color(0xff7ec700)),
    Pair("Pendafarmer 3", Color(0xffc2bf00)),
    Pair("Exedafarmer 1", Color(0xffc3a000)),
    Pair("Exedafarmer 2", Color(0xffc87200)),
    Pair("Exedafarmer 3", Color(0xffd43500)),
    Pair("Infinifarmer", Color(0xff546e7a))
)

val CRAFTING_LEVELS = arrayOf(
    500.0,
    2500.0,
    5000.0,
    10000.0,
    25000.0,
    50000.0,
    100000.0,
    250000.0,
    500000.0,
    1000000.0,
    2000000.0,
    4000000.0,
    8000000.0,
    15000000.0,
    20000000.0,
    35000000.0,
    60000000.0,
    100000000.0,
    150000000.0,
    200000000.0,
    250000000.0,
    300000000.0,
    325000000.0,
    350000000.0,
    400000000.0,
    500000000.0,
    600000000.0,
    750000000.0,
    1000000000.0
)

val ALL_GRADES = arrayOf(
    "grade_unknown",
    "grade_c",
    "grade_b",
    "grade_a",
    "grade_aa",
    "grade_aaa"
)

// Pop w/ leggy gusset
const val MAX_FARM_POP = 14175000000L

// Pop w/ leggy gusset and 3 t4 clarity stones
const val MAX_ENLIGHTEN_FARM_POP = 19845000000L

val PROBLEMATIC_BROWSERS = arrayOf(
    "org.mozilla.firefox",
    "com.duckduckgo.mobile.android"
)

const val DEFAULT_BROWSER = "com.android.chrome"

const val PROGRESS_BACKGROUND_COLOR = "#464646"
const val CONTRACT_PROGRESS_COLOR = "#008531"
const val CONTRACT_OFFLINE_PROGRESS_COLOR = "#51dda8"