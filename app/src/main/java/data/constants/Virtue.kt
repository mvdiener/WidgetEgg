package data.constants

import data.Artifact
import data.Research
import data.Stone
import ei.Ei.Egg

val IHR_RESEARCHES = arrayOf(
    Research(
        id = "internal_hatchery1",
        perLevelValue = 2.0,
        isEpic = false,
        isMultiplicative = false,
        isOfflineOnly = false
    ),
    Research(
        id = "internal_hatchery2",
        perLevelValue = 5.0,
        isEpic = false,
        isMultiplicative = false,
        isOfflineOnly = false
    ),
    Research(
        id = "internal_hatchery3",
        perLevelValue = 10.0,
        isEpic = false,
        isMultiplicative = false,
        isOfflineOnly = false
    ),
    Research(
        id = "internal_hatchery4",
        perLevelValue = 25.0,
        isEpic = false,
        isMultiplicative = false,
        isOfflineOnly = false
    ),
    Research(
        id = "internal_hatchery5",
        perLevelValue = 5.0,
        isEpic = false,
        isMultiplicative = false,
        isOfflineOnly = false
    ),
    Research(
        id = "neural_linking",
        perLevelValue = 50.0,
        isEpic = false,
        isMultiplicative = false,
        isOfflineOnly = false
    ),
    Research(
        id = "epic_internal_incubators",
        perLevelValue = 0.05,
        isEpic = true,
        isMultiplicative = true,
        isOfflineOnly = false
    ),
    Research(
        id = "int_hatch_calm",
        perLevelValue = 0.1,
        isEpic = true,
        isMultiplicative = true,
        isOfflineOnly = true
    ),
)

// Chalices
val IHR_ARTIFACTS = arrayOf(
    // T1C
    Artifact(
        name = 9,
        rarity = 0,
        level = 0,
        effectValue = 0.05,
        stones = emptyList()
    ),
    // T2C
    Artifact(
        name = 9,
        rarity = 0,
        level = 1,
        effectValue = 0.1,
        stones = emptyList()
    ),
    // T2E
    Artifact(
        name = 9,
        rarity = 2,
        level = 1,
        effectValue = 0.15,
        stones = emptyList()
    ),
    // T3C
    Artifact(
        name = 9,
        rarity = 0,
        level = 2,
        effectValue = 0.2,
        stones = emptyList()
    ),
    // T3R
    Artifact(
        name = 9,
        rarity = 1,
        level = 2,
        effectValue = 0.23,
        stones = emptyList()
    ),
    // T3E
    Artifact(
        name = 9,
        rarity = 2,
        level = 2,
        effectValue = 0.25,
        stones = emptyList()
    ),
    // T4C
    Artifact(
        name = 9,
        rarity = 0,
        level = 3,
        effectValue = 0.3,
        stones = emptyList()
    ),
    // T4E
    Artifact(
        name = 9,
        rarity = 2,
        level = 3,
        effectValue = 0.35,
        stones = emptyList()
    ),
    // T4L
    Artifact(
        name = 9,
        rarity = 3,
        level = 3,
        effectValue = 0.4,
        stones = emptyList()
    )
)

// Life stones
val IHR_STONES = arrayOf(
    Stone(
        name = 38,
        level = 0,
        effectValue = 0.02
    ),
    Stone(
        name = 38,
        level = 1,
        effectValue = 0.03
    ),
    Stone(
        name = 38,
        level = 2,
        effectValue = 0.04
    )
)

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