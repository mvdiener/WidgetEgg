package data

import androidx.compose.ui.graphics.Color
import ei.Ei.Egg

const val API_ROOT = "https://www.auxbrain.com"
const val MISSION_ENDPOINT = "${API_ROOT}/ei_afx/get_active_missions_v2"
const val BACKUP_ENDPOINT = "${API_ROOT}/ei/bot_first_contact"
const val CONTRACT_ENDPOINT = "${API_ROOT}/ei/coop_status_bot"
const val PERIODICALS_ENDPOINT = "${API_ROOT}/ei/get_periodicals"
const val CONTRACTS_ARCHIVE_ENDPOINT = "${API_ROOT}/ei_ctx/get_contracts_archive"

const val CURRENT_CLIENT_VERSION = 99

const val CONTRACT_NOTIFICATION_CHANNEL_ID = "widgetegg_contract_notifications"

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

//Order of virtue egg list is important, do not alter
val VIRTUE_EGGS = arrayOf(
    Egg.CURIOSITY.number,
    Egg.INTEGRITY.number,
    Egg.HUMILITY.number,
    Egg.RESILIENCE.number,
    Egg.KINDNESS.number
)
val VIRTUE_DELIVERY_GOALS = arrayOf(
    5e7, // 50M - TE #1
    1e9, // 1B
    1e10, // 10B
    7e10, // 70B
    5e11, // 500B
    2e12, // 2T
    7e12, // 7T
    2e13, // 20T
    6e13, // 60T
    1.5e14, // 150T
    5e14, // 500T
    1.5e15, // 1.5q
    4e15, // 4q
    1e16, // 10q
    2.5e16, // 25q
    5e16, // 50q
    1e17, // 100q
    1.5e17, // 150q
    2.1e17, // 210q
    2.8e17, // 280q
    3.6e17, // 360q
    4.5e17, // 450q
    5.5e17, // 550q
    6.6e17, // 660q
    7.8e17, // 780q
    9.1e17, // 910q
    1.05e18, // 1.05Q
    1.2e18, // 1.2Q
    1.36e18, // 1.36Q
    1.53e18, // 1.53Q
    1.71e18, // 1.71Q
    1.9e18, // 1.9Q
    2.1e18, // 2.1Q
    2.31e18, // 2.31Q
    2.53e18, // 2.53Q
    2.76e18, // 2.76Q
    3.0e18, // 3.0Q
    3.25e18, // 3.25Q
    3.51e18, // 3.51Q
    3.78e18, // 3.78Q
    4.06e18, // 4.06Q
    4.35e18, // 4.35Q
    4.65e18, // 4.65Q
    4.96e18, // 4.96Q
    5.28e18, // 5.28Q
    5.61e18, // 5.61Q
    5.95e18, // 5.95Q
    6.3e18, // 6.30Q
    6.66e18, // 6.66Q
    7.03e18, // 7.03Q
    7.41e18, // 7.41Q
    7.8e18, // 7.8Q
    8.2e18, // 8.2Q
    8.61e18, // 8.61Q
    9.03e18, // 9.03Q
    9.46e18, // 9.46Q
    9.9e18, // 9.9Q
    1.035e19, // 10.35Q
    1.081e19, // 10.81Q
    1.128e19, // 11.28Q
    1.176e19, // 11.76Q
    1.225e19, // 12.25Q
    1.275e19, // 12.75Q
    1.326e19, // 13.26Q
    1.378e19, // 13.78Q
    1.431e19, // 14.31Q
    1.485e19, // 14.85Q
    1.54e19, // 15.4Q
    1.596e19, // 15.96Q
    1.653e19, // 16.53Q
    1.711e19, // 17.11Q
    1.77e19, // 17.7Q
    1.83e19, // 18.3Q
    1.891e19, // 18.91Q
    1.953e19, // 19.53Q
    2.016e19, // 20.16Q
    2.08e19, // 20.8Q
    2.145e19, // 21.45Q
    2.211e19, // 22.11Q
    2.278e19, // 22.78Q
    2.346e19, // 23.46Q
    2.415e19, // 24.15Q
    2.485e19, // 24.85Q
    2.556e19, // 25.56Q
    2.628e19, // 26.28Q
    2.701e19, // 27.01Q
    2.775e19, // 27.75Q
    2.85e19, // 28.5Q
    2.926e19, // 29.26Q
    3.003e19, // 30.03Q
    3.081e19, // 30.81Q
    3.16e19, // 31.6Q
    3.24e19, // 32.4Q
    3.321e19, // 33.21Q
    3.403e19, // 34.03Q
    3.486e19, // 34.86Q
    3.57e19, // 35.7Q
    3.655e19, // 36.55Q - TE #98 (max)
)