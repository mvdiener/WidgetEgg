package data

const val API_ROOT = "https://www.auxbrain.com"
const val MISSION_ENDPOINT = "${API_ROOT}/ei_afx/get_active_missions"
const val BACKUP_ENDPOINT = "${API_ROOT}/ei/bot_first_contact"
const val CONTRACT_ENDPOINT = "${API_ROOT}/ei/coop_status"

const val CURRENT_CLIENT_VERSION = 69

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

fun getImageFromAfxId(afxId: Int): String {
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
