package com.example.model

enum class ComponentType(val title: String) {
    GPU("Видеокарта"),
    CPU("Процессор"),
    MOTHERBOARD("Материнская плата"),
    RAM("Оперативная память"),
    PSU("Блок питания"),
    COOLING("Охлаждение"),
    STORAGE("Накопитель")
}

data class PCComponent(
    val id: String,
    val name: String,
    val type: ComponentType,
    val priceUsd: Double,
    val hashRateMh: Double = 0.0, // MegaHashes/s
    val powerWatts: Int = 0,
    val coolingCapacityWatts: Int = 0, // для кулеров/СЖО
    val psuWattsCapacity: Int = 0, // для БП
    val maxGpuSlots: Int = 1, // для материнок
    val maxRamSlots: Int = 2,
    val tier: Int = 1,
    val iconName: String = "gpu",
    val description: String = "",
    val isUsed: Boolean = false, // Для барахолки Avinto
    val baseDurability: Float = 100f
)

data class InstalledRigComponent(
    val slotId: String,
    val component: PCComponent,
    var durabilityPercent: Float = 100f, // износ от перегрева
    var dustPercent: Float = 0f // пыль, снижает охлаждение
)

data class MiningRig(
    val id: String,
    val name: String,
    val location: String = "Квартира родителей",
    var isPoweredOn: Boolean = true,
    var currentCryptoId: String = "ETH",
    val installedComponents: MutableList<InstalledRigComponent> = mutableListOf(),
    var currentTemperatureC: Float = 42f,
    var roomTemperatureC: Float = 24f,
    var dustLevelPercent: Float = 2f,
    var thermalPasteCondition: Float = 100f,
    // Настройки разгона (Overclocking из Colorbit)
    var coreClockOffsetMhz: Int = 0, // от -200 до +300 МГц
    var memoryClockOffsetMhz: Int = 0, // от -500 до +1500 МГц
    var powerLimitPercent: Int = 100, // от 60% до 125%
    var fanSpeedPercent: Int = 75 // от 30% до 100%
) {
    val totalHashRateMh: Double
        get() {
            if (!isPoweredOn || isOverheated) return 0.0
            val dustPenalty = (100f - dustLevelPercent.coerceIn(0f, 100f) * 0.35f) / 100f
            val baseHash = installedComponents.sumOf { it.component.hashRateMh * (it.durabilityPercent / 100.0) }
            // Множитель разгона: разгон памяти даёт наибольший прирост к хэшрейту (ETH/DAG)
            val ocMultiplier = 1.0 + (memoryClockOffsetMhz * 0.00028) + (coreClockOffsetMhz * 0.00012)
            return (baseHash * dustPenalty * ocMultiplier).coerceAtLeast(0.0)
        }

    val totalPowerWatts: Int
        get() {
            if (!isPoweredOn) return 5
            val baseWatts = installedComponents.sumOf { it.component.powerWatts } + 25 // +25W система
            val plMultiplier = powerLimitPercent / 100.0
            return (baseWatts * plMultiplier).toInt().coerceAtLeast(20)
        }

    val psuCapacityWatts: Int
        get() = installedComponents.filter { it.component.type == ComponentType.PSU }
            .sumOf { it.component.psuWattsCapacity }

    val coolingCapacityWatts: Int
        get() {
            val base = installedComponents.filter { it.component.type == ComponentType.COOLING }
                .sumOf { it.component.coolingCapacityWatts }
            val pasteFactor = (thermalPasteCondition / 100f).coerceIn(0.2f, 1f)
            val dustFactor = (1f - (dustLevelPercent / 100f) * 0.5f).coerceIn(0.2f, 1f)
            val fanFactor = (fanSpeedPercent / 75.0f).coerceIn(0.5f, 1.4f)
            return (base * pasteFactor * dustFactor * fanFactor).toInt().coerceAtLeast(50)
        }

    val isOverheated: Boolean
        get() = currentTemperatureC >= 92f

    val isWarningTemp: Boolean
        get() = currentTemperatureC >= 78f

    val isPsuOverloaded: Boolean
        get() = psuCapacityWatts > 0 && totalPowerWatts > psuCapacityWatts
}

data class CryptoCurrency(
    val id: String,
    val name: String,
    val symbol: String,
    var priceUsd: Double,
    val difficulty: Double, // сложность сети
    val algo: String,
    val colorHex: Long,
    val priceHistory: MutableList<Double> = mutableListOf()
)

enum class RentPeriod(val title: String, val days: Int) {
    TWO_DAYS("2 дня", 2),
    SEVEN_DAYS("7 дней", 7),
    THIRTY_DAYS("30 дней", 30),
    FOREVER("Навсегда", -1)
}

data class MiningFacility(
    val id: String,
    val name: String,
    val category: String = "Жильё",
    val description: String = "",
    val electricityCostKwhUsd: Double, // тариф на электричество ($/кВт⋅ч)
    val maxRigs: Int, // макс. количество ригов
    val ambientTempC: Float, // фоновая температура
    val dustAccumulationRate: Float, // множитель запыления
    val price2DaysUsd: Double, // аренда на 2 дня
    val price7DaysUsd: Double, // аренда на 7 дней
    val price30DaysUsd: Double, // аренда на 30 дней
    val priceForeverUsd: Double, // покупка навсегда
    val isUnlocked: Boolean = false,
    val isPermanent: Boolean = false, // в собственности навсегда
    val rentExpiresTimestampMs: Long = 0L, // таймстемп окончания аренды
    val isActiveLocation: Boolean = false // текущая рабочая локация
) {
    val unlockCostUsd: Double get() = priceForeverUsd
    val rentPerDayUsd: Double get() = if (priceForeverUsd == 0.0) 0.0 else price30DaysUsd / 30.0

    fun getPrice(period: RentPeriod): Double = when (period) {
        RentPeriod.TWO_DAYS -> price2DaysUsd
        RentPeriod.SEVEN_DAYS -> price7DaysUsd
        RentPeriod.THIRTY_DAYS -> price30DaysUsd
        RentPeriod.FOREVER -> priceForeverUsd
    }

    val isRentExpired: Boolean
        get() = !isPermanent && isUnlocked && rentExpiresTimestampMs > 0 && System.currentTimeMillis() > rentExpiresTimestampMs

    fun remainingTimeFormatted(): String {
        if (isPermanent) return "В собственности навсегда"
        if (!isUnlocked || rentExpiresTimestampMs <= 0L) return "Не арендовано"
        val remainingMs = (rentExpiresTimestampMs - System.currentTimeMillis()).coerceAtLeast(0L)
        val totalSec = remainingMs / 1000
        val days = totalSec / 86400
        val hours = (totalSec % 86400) / 3600
        val minutes = (totalSec % 3600) / 60
        return when {
            days > 0 -> "$days дн. $hours ч."
            hours > 0 -> "$hours ч. $minutes мин."
            else -> "$minutes мин."
        }
    }
}

// Виртуальные кредиты из Colorbit
data class VirtualLoan(
    val id: String,
    val title: String,
    val amountUsd: Double,
    val interestRatePercent: Double, // например, 10%
    val totalToRepayUsd: Double,
    var remainingDebtUsd: Double = 0.0,
    val isActive: Boolean = false,
    val description: String = ""
)

// Сюжетная система (Colorbit Campaign)
data class StoryDialogue(
    val speakerName: String,
    val speakerRole: String,
    val text: String,
    val isPlayer: Boolean = false
)

data class StoryChapter(
    val chapterNumber: Int,
    val title: String,
    val subtitle: String,
    val requiredFacilityId: String, // ДомКлик локация, необходимая для главы
    val requiredFacilityName: String = "",
    val synopsis: String,
    val dialogues: List<StoryDialogue>,
    val objectiveText: String,
    val targetHashrateMh: Double = 0.0,
    val targetBalanceUsd: Double = 0.0,
    val rewardUsd: Double,
    val rewardXp: Int,
    var isCompleted: Boolean = false,
    var isUnlocked: Boolean = false
)

data class StoryQuest(
    val id: String,
    val title: String,
    val description: String,
    val targetType: QuestTargetType,
    val targetValue: Double,
    var currentValue: Double = 0.0,
    val rewardUsd: Double,
    val rewardXp: Int,
    var isCompleted: Boolean = false,
    var isClaimed: Boolean = false
)

enum class QuestTargetType {
    REACH_BALANCE_USD,
    MINE_CRYPTO_AMOUNT,
    BUILD_TOTAL_HASHRATE,
    BUY_COMPONENTS_COUNT,
    CLEAN_DUST_TIMES,
    SURVIVE_DAYS
}

enum class GameDifficulty(
    val id: String,
    val title: String,
    val shortName: String,
    val colorHex: Long,
    val description: String
) {
    EASY(
        id = "EASY",
        title = "Лёгкая",
        shortName = "Легко",
        colorHex = 0xFF00FF66,
        description = "Боты на Авито, автоматические выкупы. Без реальных игроков по сети."
    ),
    NORMAL(
        id = "NORMAL",
        title = "Нормальная",
        shortName = "Норм",
        colorHex = 0xFFFFB300,
        description = "Без ботов на Авито. Только реальные игроки по P2P. Продажа со спросом раз в 5 минут."
    ),
    HARD(
        id = "HARD",
        title = "Сложная",
        shortName = "Сложно",
        colorHex = 0xFFFF5252,
        description = "Хардкор P2P. Без ботов, продажа раз в 5 минут, высокая сложность добычи."
    );

    companion object {
        fun fromId(id: String?): GameDifficulty {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NORMAL
        }
    }
}

data class SeasonInfo(
    val seasonNumber: Int = 1,
    val seasonStartTimestampMs: Long = System.currentTimeMillis(),
    val durationDays: Int = 90
) {
    val durationMs: Long
        get() = durationDays.toLong() * 24 * 60 * 60 * 1000L

    val endTimestampMs: Long
        get() = seasonStartTimestampMs + durationMs

    val remainingMs: Long
        get() = (endTimestampMs - System.currentTimeMillis()).coerceAtLeast(0L)

    val remainingDays: Long
        get() = remainingMs / (24 * 60 * 60 * 1000L)

    val remainingHours: Long
        get() = (remainingMs % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)

    val remainingMinutes: Long
        get() = (remainingMs % (60 * 60 * 1000L)) / (60 * 1000L)

    val formattedRemaining: String
        get() {
            return when {
                remainingDays > 0 -> "${remainingDays}д ${remainingHours}ч"
                remainingHours > 0 -> "${remainingHours}ч ${remainingMinutes}м"
                else -> "${remainingMinutes}м"
            }
        }

    val isExpired: Boolean
        get() = System.currentTimeMillis() >= endTimestampMs
}

data class PlayerStats(
    var balanceUsd: Double = 350.0, // стартовый капитал
    var cryptoBalances: MutableMap<String, Double> = mutableMapOf(
        "ETH" to 0.0,
        "BTC" to 0.0,
        "RVN" to 0.0,
        "CFX" to 0.0
    ),
    var totalMinedUsd: Double = 0.0,
    var electricitySpentUsd: Double = 0.0,
    var level: Int = 1,
    var xp: Int = 0,
    var inGameDaysPassed: Int = 1,
    var lastOfflineTimestampMs: Long = System.currentTimeMillis(),
    var currentStoryChapter: Int = 1,
    var activeDebtUsd: Double = 0.0,
    var difficulty: GameDifficulty = GameDifficulty.NORMAL,
    var playerName: String = "Майнер-Игрок"
)

// === P2P АВИТО МОДЕЛИ (PEER-TO-PEER MARKETPLACE) ===

data class P2PListing(
    val id: String,
    val component: PCComponent,
    val sellerPeerId: String,
    val sellerName: String,
    val sellerRating: Float = 4.9f,
    val successfulDeals: Int = 24,
    val pingMs: Int = 21,
    val conditionPercent: Float = 75f, // Скрытый реальный ресурс («игра-угадайка» как на Авито)
    val sellerClaim: String = "Не бита, не крашена", // Что утверждает продавец в объявлении
    val sellerComment: String = "",
    val priceUsd: Double,
    var isSold: Boolean = false,
    val isRealPeer: Boolean = false // Лот от реального живого игрока в P2P сети
) {
    val finalPriceUsd: Double
        get() = priceUsd
}

data class P2PSwarmState(
    val isConnected: Boolean = true,
    val myPeerId: String = "peer_0x8f2a",
    val myPeerName: String = "Майнер-Игрок",
    val peersCount: Int = 24,
    val averagePingMs: Int = 21,
    val networkMode: String = "P2P Mesh / UDP & DHT Swarm",
    val localSubnetIp: String = "192.168.1.104",
    val isScanning: Boolean = false
)

data class MyP2PSaleListing(
    val id: String,
    val component: PCComponent,
    val rigId: String,
    val slotId: String,
    val durabilityPercent: Float,
    val askingPriceUsd: Double,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    var isSold: Boolean = false,
    var buyerPeerName: String? = null,
    var remainingSecondsToDemandCheck: Int = 300 // Отсчёт 5 минут до шанса продажи
)

data class P2PLeaderboardEntry(
    val peerId: String,
    val playerName: String,
    val hashRateMh: Double,
    val balanceUsd: Double,
    val rigsCount: Int,
    val chapter: Int = 1,
    val pingMs: Int = 18,
    val isLocalPlayer: Boolean = false,
    val isLanPeer: Boolean = false,
    val difficulty: String = "NORMAL",
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)
