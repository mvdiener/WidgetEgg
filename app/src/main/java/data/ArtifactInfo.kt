package data

const val MISSION_ENDPOINT = "https://www.auxbrain.com/ei_afx/get_active_missions"
const val BACKUP_ENDPOINT = "https://www.auxbrain.com/ei/bot_first_contact"

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

val SHIP_TIMES = arrayOf(
    arrayOf(1200, 3600, 7200, 60),
    arrayOf(1800, 3600, 10800),
    arrayOf(2700, 5400, 14400),
    arrayOf(5400, 14400, 28800),
    arrayOf(10800, 21600, 43200),
    arrayOf(14400, 43200, 86400),
    arrayOf(21600, 57600, 108000),
    arrayOf(28800, 86400, 172800),
    arrayOf(43200, 129600, 259200),
    arrayOf(86400, 172800, 345600),
    arrayOf(172800, 259200, 345600),
)

fun getImageFromAfxID(afxId: Int): String {
    when (afxId) {
        23 -> return "afx_puzzle_cube_4"
        0 -> return "afx_lunar_totem_4"
        6 -> return "afx_demeters_necklace_4"
        7 -> return "afx_vial_martian_dust_4"
        21 -> return "afx_aurelian_brooch_4"
        12 -> return "afx_tungsten_ankh_4"
        8 -> return "afx_ornate_gusset_4"
        3 -> return "afx_neo_medallion_4"
        30 -> return "afx_mercurys_lens_4"
        4 -> return "afx_beak_of_midas_4"
        22 -> return "afx_carved_rainstick_4"
        27 -> return "afx_interstellar_compass_4"
        9 -> return "afx_the_chalice_4"
        11 -> return "afx_phoenix_feather_4"
        24 -> return "afx_quantum_metronome_4"
        28 -> return "afx_dilithium_monocle_4"
        29 -> return "afx_titanium_actuator_4"
        25 -> return "afx_ship_in_a_bottle_4"
        26 -> return "afx_tachyon_deflector_4"
        10 -> return "afx_book_of_basan_4"
        5 -> return "afx_light_eggendil_4"
        33 -> return "afx_lunar_stone_4"
        32 -> return "afx_shell_stone_4"
        1 -> return "afx_tachyon_stone_4"
        37 -> return "afx_terra_stone_4"
        34 -> return "afx_soul_stone_4"
        31 -> return "afx_dilithium_stone_4"
        36 -> return "afx_quantum_stone_4"
        38 -> return "afx_life_stone_4"
        40 -> return "afx_clarity_stone_4"
        39 -> return "afx_prophecy_stone_4"
        17 -> return "afx_gold_meteorite_3"
        18 -> return "afx_tau_ceti_geode_3"
        43 -> return "afx_solar_titanium_3"
        else -> return ""
    }
}

