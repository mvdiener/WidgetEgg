package data.constants

import data.Artifact
import data.EventType
import data.Hab
import data.Research
import data.Stone
import data.Vehicle
import ei.Ei.Egg

val IHR_RESEARCHES = arrayOf(
    Research(
        id = "internal_hatchery1",
        perLevelValue = 2.0
    ),
    Research(
        id = "internal_hatchery2",
        perLevelValue = 5.0
    ),
    Research(
        id = "internal_hatchery3",
        perLevelValue = 10.0
    ),
    Research(
        id = "internal_hatchery4",
        perLevelValue = 25.0
    ),
    Research(
        id = "internal_hatchery5",
        perLevelValue = 5.0
    ),
    Research(
        id = "neural_linking",
        perLevelValue = 50.0
    ),
    Research(
        id = "epic_internal_incubators",
        perLevelValue = 0.05,
        isEpic = true,
        isMultiplicative = true
    ),
    Research(
        id = "int_hatch_calm",
        perLevelValue = 0.1,
        isEpic = true,
        isMultiplicative = true,
        isOfflineOnly = true
    )
)

val LAY_RATE_RESEARCHES = arrayOf(
    Research(
        id = "comfy_nests",
        perLevelValue = 0.1
    ),
    Research(
        id = "hen_house_ac",
        perLevelValue = 0.05
    ),
    Research(
        id = "improved_genetics",
        perLevelValue = 0.15
    ),
    Research(
        id = "time_compress",
        perLevelValue = 0.1
    ),
    Research(
        id = "timeline_diversion",
        perLevelValue = 0.02
    ),
    Research(
        id = "relativity_optimization",
        perLevelValue = 0.1
    ),
    Research(
        id = "epic_egg_laying",
        perLevelValue = 0.05,
        isEpic = true
    )
)

val SHIPPING_CAPACITY_RESEARCHES = arrayOf(
    Research(
        id = "leafsprings",
        perLevelValue = 0.05
    ),
    Research(
        id = "lightweight_boxes",
        perLevelValue = 0.1
    ),
    Research(
        id = "driver_training",
        perLevelValue = 0.05
    ),
    Research(
        id = "super_alloy",
        perLevelValue = 0.05
    ),
    Research(
        id = "quantum_storage",
        perLevelValue = 0.05
    ),
    Research(
        id = "hover_upgrades",
        perLevelValue = 0.05,
        isHoverOnly = true
    ),
    Research(
        id = "dark_containment",
        perLevelValue = 0.05
    ),
    Research(
        id = "neural_net_refine",
        perLevelValue = 0.05
    ),
    Research(
        id = "hyper_portalling",
        perLevelValue = 0.05,
        isHyperloopOnly = true
    ),
    Research(
        id = "transportation_lobbyist",
        perLevelValue = 0.05,
        isEpic = true
    )
)

val HAB_SPACE_RESEARCHES = arrayOf(
    Research(
        id = "hab_capacity1",
        perLevelValue = 0.05
    ),
    Research(
        id = "microlux",
        perLevelValue = 0.05
    ),
    Research(
        id = "grav_plating",
        perLevelValue = 0.02
    ),
    Research(
        id = "wormhole_dampening",
        perLevelValue = 0.02,
        isPortalHabsOnly = true
    )
)

// Chalice
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

// Life stone
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

// Metronome
val LAY_RATE_ARTIFACTS = arrayOf(
    // T1C
    Artifact(
        name = 24,
        rarity = 0,
        level = 0,
        effectValue = 0.05,
        stones = emptyList()
    ),
    // T2C
    Artifact(
        name = 24,
        rarity = 0,
        level = 1,
        effectValue = 0.1,
        stones = emptyList()
    ),
    // T2R
    Artifact(
        name = 24,
        rarity = 1,
        level = 1,
        effectValue = 0.12,
        stones = emptyList()
    ),
    // T3C
    Artifact(
        name = 24,
        rarity = 0,
        level = 2,
        effectValue = 0.15,
        stones = emptyList()
    ),
    // T3R
    Artifact(
        name = 24,
        rarity = 1,
        level = 2,
        effectValue = 0.17,
        stones = emptyList()
    ),
    // T3E
    Artifact(
        name = 24,
        rarity = 2,
        level = 2,
        effectValue = 0.2,
        stones = emptyList()
    ),
    // T4C
    Artifact(
        name = 24,
        rarity = 0,
        level = 3,
        effectValue = 0.25,
        stones = emptyList()
    ),
    // T4R
    Artifact(
        name = 24,
        rarity = 1,
        level = 3,
        effectValue = 0.27,
        stones = emptyList()
    ),
    // T4E
    Artifact(
        name = 24,
        rarity = 2,
        level = 3,
        effectValue = 0.3,
        stones = emptyList()
    ),
    // T4L
    Artifact(
        name = 24,
        rarity = 3,
        level = 3,
        effectValue = 0.35,
        stones = emptyList()
    )
)

// Tachyon stone
val LAY_RATE_STONES = arrayOf(
    Stone(
        name = 1,
        level = 0,
        effectValue = 0.02
    ),
    Stone(
        name = 1,
        level = 1,
        effectValue = 0.04
    ),
    Stone(
        name = 1,
        level = 2,
        effectValue = 0.05
    )
)

// Compass
val SHIPPING_CAPACITY_ARTIFACTS = arrayOf(
    // T1C
    Artifact(
        name = 27,
        rarity = 0,
        level = 0,
        effectValue = 0.05,
        stones = emptyList()
    ),
    // T2C
    Artifact(
        name = 27,
        rarity = 0,
        level = 1,
        effectValue = 0.1,
        stones = emptyList()
    ),
    // T3C
    Artifact(
        name = 27,
        rarity = 0,
        level = 2,
        effectValue = 0.2,
        stones = emptyList()
    ),
    // T3R
    Artifact(
        name = 27,
        rarity = 1,
        level = 2,
        effectValue = 0.22,
        stones = emptyList()
    ),
    // T4C
    Artifact(
        name = 27,
        rarity = 0,
        level = 3,
        effectValue = 0.3,
        stones = emptyList()
    ),
    // T4R
    Artifact(
        name = 27,
        rarity = 1,
        level = 3,
        effectValue = 0.35,
        stones = emptyList()
    ),
    // T4E
    Artifact(
        name = 27,
        rarity = 2,
        level = 3,
        effectValue = 0.4,
        stones = emptyList()
    ),
    // T4L
    Artifact(
        name = 27,
        rarity = 3,
        level = 3,
        effectValue = 0.5,
        stones = emptyList()
    )
)

// Gusset
val HAB_SPACE_ARTIFACTS = arrayOf(
    // T1C
    Artifact(
        name = 8,
        rarity = 0,
        level = 0,
        effectValue = 0.05,
        stones = emptyList()
    ),
    // T2C
    Artifact(
        name = 8,
        rarity = 0,
        level = 1,
        effectValue = 0.1,
        stones = emptyList()
    ),
    // T2E
    Artifact(
        name = 8,
        rarity = 2,
        level = 1,
        effectValue = 0.12,
        stones = emptyList()
    ),
    // T3C
    Artifact(
        name = 8,
        rarity = 0,
        level = 2,
        effectValue = 0.15,
        stones = emptyList()
    ),
    // T3R
    Artifact(
        name = 8,
        rarity = 1,
        level = 2,
        effectValue = 0.16,
        stones = emptyList()
    ),
    // T4C
    Artifact(
        name = 8,
        rarity = 0,
        level = 3,
        effectValue = 0.2,
        stones = emptyList()
    ),
    // T4E
    Artifact(
        name = 8,
        rarity = 2,
        level = 3,
        effectValue = 0.22,
        stones = emptyList()
    ),
    // T4L
    Artifact(
        name = 8,
        rarity = 3,
        level = 3,
        effectValue = 0.25,
        stones = emptyList()
    )
)

// Quantum stone
val SHIPPING_CAPACITY_STONES = arrayOf(
    Stone(
        name = 36,
        level = 0,
        effectValue = 0.02
    ),
    Stone(
        name = 36,
        level = 1,
        effectValue = 0.04
    ),
    Stone(
        name = 36,
        level = 2,
        effectValue = 0.05
    )
)

val VEHICLES = arrayOf(
    Vehicle(
        id = 0,
        baseCapacity = 5e3 / 60
    ),
    Vehicle(
        id = 1,
        baseCapacity = 15e3 / 60
    ),
    Vehicle(
        id = 2,
        baseCapacity = 50e3 / 60
    ),
    Vehicle(
        id = 3,
        baseCapacity = 100e3 / 60
    ),
    Vehicle(
        id = 4,
        baseCapacity = 250e3 / 60
    ),
    Vehicle(
        id = 5,
        baseCapacity = 500e3 / 60
    ),
    Vehicle(
        id = 6,
        baseCapacity = 1e6 / 60
    ),
    Vehicle(
        id = 7,
        baseCapacity = 5e6 / 60
    ),
    Vehicle(
        id = 8,
        baseCapacity = 15e6 / 60
    ),
    Vehicle(
        id = 9,
        baseCapacity = 30e6 / 60
    ),
    Vehicle(
        id = 10,
        baseCapacity = 50e6 / 60
    ),
    Vehicle(
        id = 11,
        baseCapacity = 50e6 / 60
    )
)

val HABS = arrayOf(
    Hab(
        id = 0,
        baseHabSpace = 250.0
    ),
    Hab(
        id = 1,
        baseHabSpace = 500.0
    ),
    Hab(
        id = 2,
        baseHabSpace = 1000.0
    ),
    Hab(
        id = 3,
        baseHabSpace = 2000.0
    ),
    Hab(
        id = 4,
        baseHabSpace = 5000.0
    ),
    Hab(
        id = 5,
        baseHabSpace = 10000.0
    ),
    Hab(
        id = 6,
        baseHabSpace = 20000.0
    ),
    Hab(
        id = 7,
        baseHabSpace = 50000.0
    ),
    Hab(
        id = 8,
        baseHabSpace = 100000.0
    ),
    Hab(
        id = 9,
        baseHabSpace = 200000.0
    ),
    Hab(
        id = 10,
        baseHabSpace = 500000.0
    ),
    Hab(
        id = 11,
        baseHabSpace = 1e6
    ),
    Hab(
        id = 12,
        baseHabSpace = 2e6
    ),
    Hab(
        id = 13,
        baseHabSpace = 5e6
    ),
    Hab(
        id = 14,
        baseHabSpace = 1e7
    ),
    Hab(
        id = 15,
        baseHabSpace = 2.5e7
    ),
    Hab(
        id = 16,
        baseHabSpace = 5e7
    ),
    Hab(
        id = 17,
        baseHabSpace = 1e8
    ),
    Hab(
        id = 18,
        baseHabSpace = 6e8
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

val ALL_EVENT_TYPES = listOf(
    EventType("epic-research-sale", "Epic Research Sale"),
    EventType("piggy-boost", "Piggy Growth"),
    EventType("piggy-cap-boost", "Unlimited Piggy"),
    EventType("prestige-boost", "Prestige Boost"),
    EventType("earnings-boost", "Cash Boost"),
    EventType("gift-boost", "Generous Gifts"),
    EventType("drone-boost", "Generous Drones"),
    EventType("research-sale", "Research Sale"),
    EventType("hab-sale", "Hen House Sale"),
    EventType("vehicle-sale", "Vehicle Sale"),
    EventType("boost-sale", "Boost Sale"),
    EventType("boost-duration", "Boost Time+"),
    EventType("crafting-sale", "Crafting Sale"),
    EventType("mission-fuel", "Mission Fuel Boost"),
    EventType("mission-capacity", "Mission Capacity Boost"),
    EventType("mission-duration", "Mission Duration Cut"),
    EventType("shell-sale", "Shell Sale")
)