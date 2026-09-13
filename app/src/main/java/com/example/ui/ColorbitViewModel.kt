package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ColorbitRepository
import com.example.data.ComponentCatalog
import com.example.model.ComponentType
import com.example.model.CryptoCurrency
import com.example.model.InstalledRigComponent
import com.example.model.MiningFacility
import com.example.model.MiningRig
import com.example.model.PCComponent
import com.example.model.PlayerStats
import com.example.model.QuestTargetType
import com.example.model.RentPeriod
import com.example.model.StoryQuest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class GameTab {
    RIGS, // Мои фермы и обслуживание
    SHOP, // Магазин комплектующих
    EXCHANGE, // Биржа криптовалют и курсы
    FACILITIES, // Локации (гараж, склад, ЦОД)
    QUESTS // Задания и прокачка
}

data class OfflineEarningsResult(
    val offlineSeconds: Long,
    val minedAmounts: Map<String, Double>,
    val electricityCostUsd: Double
)

class ColorbitViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ColorbitRepository(application)

    private val _selectedTab = MutableStateFlow(GameTab.RIGS)
    val selectedTab: StateFlow<GameTab> = _selectedTab.asStateFlow()

    private val _playerStats = MutableStateFlow(repo.loadPlayerStats())
    val playerStats: StateFlow<PlayerStats> = _playerStats.asStateFlow()

    private val _rigs = MutableStateFlow(repo.loadRigs())
    val rigs: StateFlow<List<MiningRig>> = _rigs.asStateFlow()

    private val _cryptos = MutableStateFlow(repo.getInitialCryptos())
    val cryptos: StateFlow<List<CryptoCurrency>> = _cryptos.asStateFlow()

    private val _facilities = MutableStateFlow(repo.getFacilities())
    val facilities: StateFlow<List<MiningFacility>> = _facilities.asStateFlow()

    private val _quests = MutableStateFlow(repo.getStoryQuests())
    val quests: StateFlow<List<StoryQuest>> = _quests.asStateFlow()

    private val _storyChapters = MutableStateFlow(repo.loadStoryChapters())
    val storyChapters: StateFlow<List<com.example.model.StoryChapter>> = _storyChapters.asStateFlow()

    private val _activeStoryDialogue = MutableStateFlow<com.example.model.StoryChapter?>(null)
    val activeStoryDialogue: StateFlow<com.example.model.StoryChapter?> = _activeStoryDialogue.asStateFlow()

    private val _loans = MutableStateFlow(repo.loadLoans())
    val loans: StateFlow<List<com.example.model.VirtualLoan>> = _loans.asStateFlow()

    private val p2pService = com.example.data.P2PAvitoService(viewModelScope)
    val p2pSwarmState = p2pService.swarmState
    val p2pListings = p2pService.listings
    val myP2PSales = p2pService.mySales
    val p2pLeaderboard = p2pService.leaderboardEntries
    val leaderboardEntries = p2pLeaderboard

    private val _statusNotification = MutableStateFlow<String?>(null)
    val statusNotification: StateFlow<String?> = _statusNotification.asStateFlow()

    private val _offlineDialogInfo = MutableStateFlow<OfflineEarningsResult?>(null)
    val offlineDialogInfo: StateFlow<OfflineEarningsResult?> = _offlineDialogInfo.asStateFlow()

    private val _needsInitialDifficultySelection = MutableStateFlow(!repo.hasChosenDifficulty())
    val needsInitialDifficultySelection: StateFlow<Boolean> = _needsInitialDifficultySelection.asStateFlow()

    private val _seasonInfo = MutableStateFlow(repo.loadSeasonInfo())
    val seasonInfo: StateFlow<com.example.model.SeasonInfo> = _seasonInfo.asStateFlow()

    private val _showSeasonalWipeDialog = MutableStateFlow(false)
    val showSeasonalWipeDialog: StateFlow<Boolean> = _showSeasonalWipeDialog.asStateFlow()

    private var tickCount = 0

    init {
        p2pService.setDifficulty(_playerStats.value.difficulty)
        updateP2PPlayerStats()
        checkOfflineProgress()
        startSimulationEngine()
        // Проверка на необходимость сезонного вайпа (каждые 3 месяца)
        if (_seasonInfo.value.isExpired) {
            _showSeasonalWipeDialog.value = true
        }
        // Если это первый запуск, показываем диалог первой главы
        val ch1 = _storyChapters.value.firstOrNull { it.chapterNumber == 1 && !it.isCompleted }
        if (ch1 != null && _playerStats.value.currentStoryChapter == 1 && _playerStats.value.inGameDaysPassed == 1) {
            _activeStoryDialogue.value = ch1
        }
    }

    fun selectTab(tab: GameTab) {
        _selectedTab.value = tab
    }

    fun dismissOfflineDialog() {
        _offlineDialogInfo.value = null
    }

    fun showStoryDialogue(chapter: com.example.model.StoryChapter) {
        _activeStoryDialogue.value = chapter
    }

    fun dismissStoryDialogue() {
        _activeStoryDialogue.value = null
    }

    private fun checkOfflineProgress() {
        checkRentExpiration()
        val now = System.currentTimeMillis()
        val lastTs = _playerStats.value.lastOfflineTimestampMs
        val diffSeconds = ((now - lastTs) / 1000).coerceAtLeast(0)

        // Если отсутствовали больше 20 секунд, рассчитываем пассивный офлайн доход
        if (diffSeconds > 20) {
            val cappedSeconds = diffSeconds.coerceAtMost(86400 * 2) // максимум 2 дня
            val currentRigs = _rigs.value
            val minedMap = mutableMapOf<String, Double>()
            var totalElectricityCost = 0.0

            val currentFacility = _facilities.value.firstOrNull { it.isActiveLocation && it.isUnlocked }
                ?: _facilities.value.firstOrNull { it.isUnlocked }
                ?: _facilities.value[0]
            val kwhCost = currentFacility.electricityCostKwhUsd

            for (rig in currentRigs) {
                if (!rig.isPoweredOn || rig.isOverheated) continue

                val crypto = _cryptos.value.find { it.id == rig.currentCryptoId } ?: _cryptos.value[0]
                val hashRate = rig.totalHashRateMh
                // 10 MH/s = ~0.00002 монеты в час
                val coinsPerSec = (hashRate / 10.0) * (0.00002 / 3600.0) / crypto.difficulty
                val earned = coinsPerSec * cappedSeconds
                minedMap[crypto.id] = (minedMap[crypto.id] ?: 0.0) + earned

                val hours = cappedSeconds / 3600.0
                val kw = rig.totalPowerWatts / 1000.0
                totalElectricityCost += kw * hours * kwhCost
            }

            // Начисляем на балансы
            val stats = _playerStats.value.copy()
            minedMap.forEach { (coinId, amount) ->
                val current = stats.cryptoBalances[coinId] ?: 0.0
                stats.cryptoBalances[coinId] = current + amount
            }
            stats.electricitySpentUsd += totalElectricityCost
            stats.balanceUsd = (stats.balanceUsd - totalElectricityCost).coerceAtLeast(-50.0)
            stats.lastOfflineTimestampMs = now
            _playerStats.value = stats
            repo.savePlayerStats(stats)

            if (minedMap.isNotEmpty() && minedMap.values.sum() > 0.000001) {
                _offlineDialogInfo.value = OfflineEarningsResult(
                    offlineSeconds = diffSeconds,
                    minedAmounts = minedMap,
                    electricityCostUsd = totalElectricityCost
                )
            }
        }
    }

    private fun startSimulationEngine() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000) // тик 1 секунда
                tickCount++
                if (tickCount % 5 == 0) {
                    checkRentExpiration()
                    checkStoryProgress()
                }
                processMiningTick()

                // Каждые 5 секунд обновляем крипто-биржу (динамический курс) и P2P статы
                if (tickCount % 5 == 0) {
                    processMarketFluctuations()
                    updateP2PPlayerStats()
                }

                // Проверка 3-месячного сезонного вайпа (каждые 10 секунд)
                if (tickCount % 10 == 0) {
                    val currentSeason = repo.loadSeasonInfo()
                    _seasonInfo.value = currentSeason
                    if (currentSeason.isExpired && !_showSeasonalWipeDialog.value) {
                        _showSeasonalWipeDialog.value = true
                    }
                }

                // Каждые 15 секунд сохраняем состояние
                if (tickCount % 15 == 0) {
                    saveGameState()
                }
            }
        }
    }

    private fun processMiningTick() {
        val currentRigs = _rigs.value.toMutableList()
        val stats = _playerStats.value.copy()
        var updated = false

        val activeFacility = _facilities.value.firstOrNull { it.isActiveLocation && it.isUnlocked }
            ?: _facilities.value.firstOrNull { it.isUnlocked }
            ?: _facilities.value[0]
        val ambientTemp = activeFacility.ambientTempC

        for (i in currentRigs.indices) {
            val rig = currentRigs[i]
            if (!rig.isPoweredOn) {
                // Остывает в выключенном состоянии
                if (rig.currentTemperatureC > ambientTemp) {
                    rig.currentTemperatureC = (rig.currentTemperatureC - 0.5f).coerceAtLeast(ambientTemp)
                }
                continue
            }

            // Расчёт тепловыделения и температуры
            val heatPower = rig.totalPowerWatts
            val coolingPower = rig.coolingCapacityWatts
            val targetTemp = ambientTemp + (heatPower.toFloat() / (coolingPower.toFloat().coerceAtLeast(1f)) * 50f)
            
            if (rig.currentTemperatureC < targetTemp) {
                rig.currentTemperatureC = (rig.currentTemperatureC + 0.4f).coerceAtMost(targetTemp)
            } else {
                rig.currentTemperatureC = (rig.currentTemperatureC - 0.3f).coerceAtLeast(targetTemp)
            }

            // Накопление пыли (0.01% в тик с модификатором локации)
            rig.dustLevelPercent = (rig.dustLevelPercent + 0.005f * activeFacility.dustAccumulationRate).coerceAtMost(100f)
            // Деградация термопасты при высокой температуре (>75C)
            if (rig.currentTemperatureC > 75f) {
                rig.thermalPasteCondition = (rig.thermalPasteCondition - 0.01f).coerceAtLeast(10f)
            }

            // Износ компонентов при критическом перегреве (>85C)
            if (rig.currentTemperatureC > 85f) {
                rig.installedComponents.forEach { comp ->
                    comp.durabilityPercent = (comp.durabilityPercent - 0.02f).coerceAtLeast(5f)
                }
            }

            // Майнинг монет в тик
            if (!rig.isOverheated && !rig.isPsuOverloaded) {
                val crypto = _cryptos.value.find { it.id == rig.currentCryptoId } ?: _cryptos.value[0]
                val hashRate = rig.totalHashRateMh
                // Добыча монеты за 1 секунду
                val coinReward = (hashRate / 10.0) * (0.00002 / 3600.0) / crypto.difficulty

                val currentBalance = stats.cryptoBalances[crypto.id] ?: 0.0
                stats.cryptoBalances[crypto.id] = currentBalance + coinReward

                val usdGained = coinReward * crypto.priceUsd
                stats.totalMinedUsd += usdGained

                // Электричество (за секунду)
                val kwh = (rig.totalPowerWatts / 1000.0) / 3600.0
                val cost = kwh * activeFacility.electricityCostKwhUsd
                stats.balanceUsd -= cost
                stats.electricitySpentUsd += cost
                stats.xp += 1

                // Проверка квестов
                checkQuests(stats, earnedCoin = crypto.id, earnedAmount = coinReward)
                updated = true
            }
        }

        if (updated) {
            _rigs.value = currentRigs
            _playerStats.value = stats
        }
    }

    private fun processMarketFluctuations() {
        val updatedCryptos = _cryptos.value.map { coin ->
            // Случайные колебания цен в пределах +/- 1.8%
            val deltaPercent = (Random.nextDouble(-1.8, 2.0)) / 100.0
            val newPrice = (coin.priceUsd * (1.0 + deltaPercent)).coerceAtLeast(0.001)

            val history = coin.priceHistory.toMutableList()
            if (history.size >= 12) {
                history.removeAt(0)
            }
            history.add(newPrice)

            coin.copy(priceUsd = newPrice, priceHistory = history)
        }
        _cryptos.value = updatedCryptos
    }

    private fun checkQuests(stats: PlayerStats, earnedCoin: String, earnedAmount: Double) {
        val currentQuests = _quests.value.map { quest ->
            if (quest.isCompleted) return@map quest

            when (quest.targetType) {
                QuestTargetType.MINE_CRYPTO_AMOUNT -> {
                    val currentAmount = stats.cryptoBalances[earnedCoin] ?: 0.0
                    val isDone = currentAmount >= quest.targetValue
                    quest.copy(currentValue = currentAmount, isCompleted = isDone)
                }
                QuestTargetType.REACH_BALANCE_USD -> {
                    val isDone = stats.balanceUsd >= quest.targetValue
                    quest.copy(currentValue = stats.balanceUsd, isCompleted = isDone)
                }
                QuestTargetType.BUILD_TOTAL_HASHRATE -> {
                    val totalHash = _rigs.value.sumOf { it.totalHashRateMh }
                    val isDone = totalHash >= quest.targetValue
                    quest.copy(currentValue = totalHash, isCompleted = isDone)
                }
                QuestTargetType.BUY_COMPONENTS_COUNT -> {
                    val totalComps = _rigs.value.sumOf { it.installedComponents.size }.toDouble()
                    val isDone = totalComps >= quest.targetValue
                    quest.copy(currentValue = totalComps, isCompleted = isDone)
                }
                else -> quest
            }
        }
        _quests.value = currentQuests
    }

    fun claimQuestReward(questId: String) {
        val quest = _quests.value.find { it.id == questId } ?: return
        if (!quest.isCompleted || quest.isClaimed) return

        val stats = _playerStats.value.copy(
            balanceUsd = _playerStats.value.balanceUsd + quest.rewardUsd,
            xp = _playerStats.value.xp + quest.rewardXp
        )
        _playerStats.value = stats

        _quests.value = _quests.value.map {
            if (it.id == questId) it.copy(isClaimed = true) else it
        }
        showNotification("Награда получена: +$${quest.rewardUsd.toInt()} и +${quest.rewardXp} XP!")
        saveGameState()
    }

    fun toggleRigPower(rigId: String) {
        val updated = _rigs.value.map {
            if (it.id == rigId) {
                it.copy(isPoweredOn = !it.isPoweredOn)
            } else it
        }
        _rigs.value = updated
        saveGameState()
    }

    fun switchRigCoin(rigId: String, cryptoId: String) {
        val updated = _rigs.value.map {
            if (it.id == rigId) {
                it.copy(currentCryptoId = cryptoId)
            } else it
        }
        _rigs.value = updated
        showNotification("Монета для рига изменена на $cryptoId")
        saveGameState()
    }

    fun cleanDust(rigId: String) {
        val stats = _playerStats.value
        val cost = 5.0 // баллон со сжатым воздухом
        if (stats.balanceUsd < cost) {
            showNotification("Недостаточно денег ($5) на баллон со сжатым воздухом!")
            return
        }

        val updated = _rigs.value.map {
            if (it.id == rigId) {
                it.copy(dustLevelPercent = 0f)
            } else it
        }
        _rigs.value = updated
        _playerStats.value = stats.copy(balanceUsd = stats.balanceUsd - cost)
        showNotification("Риг очищен от пыли! Охлаждение улучшено.")
        saveGameState()
    }

    fun replaceThermalPaste(rigId: String) {
        val stats = _playerStats.value
        val cost = 12.0 // премиум термопаста
        if (stats.balanceUsd < cost) {
            showNotification("Недостаточно денег ($12) на качественную термопасту!")
            return
        }

        val updated = _rigs.value.map {
            if (it.id == rigId) {
                it.copy(thermalPasteCondition = 100f)
            } else it
        }
        _rigs.value = updated
        _playerStats.value = stats.copy(balanceUsd = stats.balanceUsd - cost)
        showNotification("Термопаста заменена! Температура стабилизирована.")
        saveGameState()
    }

    fun buyAndInstallComponent(rigId: String, component: PCComponent) {
        val stats = _playerStats.value
        if (stats.balanceUsd < component.priceUsd) {
            showNotification("Не хватает денег для покупки ${component.name} ($${component.priceUsd})")
            return
        }

        val rig = _rigs.value.find { it.id == rigId } ?: return

        // Проверка слотов
        if (component.type == ComponentType.GPU) {
            val mb = rig.installedComponents.find { it.component.type == ComponentType.MOTHERBOARD }?.component
            val currentGpuCount = rig.installedComponents.count { it.component.type == ComponentType.GPU }
            val maxSlots = mb?.maxGpuSlots ?: 2
            if (currentGpuCount >= maxSlots) {
                showNotification("В материнской плате нет свободных слотов для GPU (максимум $maxSlots)!")
                return
            }
        }

        val newInstalled = InstalledRigComponent(
            slotId = "slot_${System.currentTimeMillis()}",
            component = component,
            durabilityPercent = if (component.isUsed) component.baseDurability else 100f
        )

        val updatedRigs = _rigs.value.map {
            if (it.id == rigId) {
                val list = it.installedComponents.toMutableList()
                // Если покупаем замену (например кулер, мать, проц или БП), заменяем старый
                if (component.type == ComponentType.PSU || component.type == ComponentType.MOTHERBOARD ||
                    component.type == ComponentType.COOLING || component.type == ComponentType.CPU
                ) {
                    list.removeAll { comp -> comp.component.type == component.type }
                }
                list.add(newInstalled)
                it.copy(installedComponents = list)
            } else it
        }

        _rigs.value = updatedRigs
        _playerStats.value = stats.copy(balanceUsd = stats.balanceUsd - component.priceUsd)
        showNotification("Успешно куплено и установлено: ${component.name}!")
        saveGameState()
    }

    fun sellCrypto(cryptoId: String, amountToSell: Double) {
        val stats = _playerStats.value
        val currentBalance = stats.cryptoBalances[cryptoId] ?: 0.0
        val actualAmount = amountToSell.coerceAtMost(currentBalance)
        if (actualAmount <= 0.0) {
            showNotification("Нечего продавать!")
            return
        }

        val crypto = _cryptos.value.find { it.id == cryptoId } ?: return
        val usdEarned = actualAmount * crypto.priceUsd

        val newCryptoMap = stats.cryptoBalances.toMutableMap()
        newCryptoMap[cryptoId] = (currentBalance - actualAmount).coerceAtLeast(0.0)

        _playerStats.value = stats.copy(
            balanceUsd = stats.balanceUsd + usdEarned,
            cryptoBalances = newCryptoMap
        )
        showNotification("Продано %.4f %s за +$%.2f USD!".format(actualAmount, crypto.symbol, usdEarned))
        saveGameState()
    }

    fun addNewRig(name: String) {
        val stats = _playerStats.value
        val activeFacility = _facilities.value.firstOrNull { it.isActiveLocation && it.isUnlocked }
            ?: _facilities.value.firstOrNull { it.isUnlocked }
            ?: _facilities.value[0]
        if (_rigs.value.size >= activeFacility.maxRigs) {
            showNotification("Лимит ригов в текущей локации (${activeFacility.maxRigs}) исчерпан!")
            return
        }

        val frameCost = 45.0 // каркас под риг
        if (stats.balanceUsd < frameCost) {
            showNotification("Не хватает $45 на алюминиевый каркас!")
            return
        }

        val mb = ComponentCatalog.allComponents.find { it.id == "mb_basic" }!!
        val cpu = ComponentCatalog.allComponents.find { it.id == "cpu_celeron" }!!
        val ram = ComponentCatalog.allComponents.find { it.id == "ram_4gb" }!!
        val psu = ComponentCatalog.allComponents.find { it.id == "psu_500w" }!!
        val cool = ComponentCatalog.allComponents.find { it.id == "cool_box" }!!

        val newRig = MiningRig(
            id = "rig_${System.currentTimeMillis()}",
            name = name.ifBlank { "Риг #${_rigs.value.size + 1}" },
            location = activeFacility.name,
            isPoweredOn = true,
            currentCryptoId = "ETH",
            installedComponents = mutableListOf(
                InstalledRigComponent("slot_mb", mb),
                InstalledRigComponent("slot_cpu", cpu),
                InstalledRigComponent("slot_ram", ram),
                InstalledRigComponent("slot_psu", psu),
                InstalledRigComponent("slot_cool", cool)
            ),
            currentTemperatureC = activeFacility.ambientTempC + 10f,
            dustLevelPercent = 0f,
            thermalPasteCondition = 100f
        )

        _rigs.value = _rigs.value + newRig
        _playerStats.value = stats.copy(balanceUsd = stats.balanceUsd - frameCost)
        showNotification("Новый каркас для рига собран! Установите видеокарты в магазине.")
        saveGameState()
    }

    private fun checkRentExpiration() {
        val now = System.currentTimeMillis()
        var changed = false
        val currentFacilities = _facilities.value
        val updatedFacilities = currentFacilities.map { fac ->
            if (!fac.isPermanent && fac.isUnlocked && fac.rentExpiresTimestampMs in 1..now) {
                changed = true
                fac.copy(
                    isUnlocked = false,
                    isActiveLocation = false,
                    rentExpiresTimestampMs = 0L
                )
            } else {
                fac
            }
        }

        if (changed) {
            val hasActive = updatedFacilities.any { it.isActiveLocation && it.isUnlocked }
            val finalFacilities = if (!hasActive) {
                val fallback = updatedFacilities.firstOrNull { it.isUnlocked } ?: updatedFacilities[0]
                updatedFacilities.map {
                    if (it.id == fallback.id) it.copy(isActiveLocation = true) else it
                }
            } else {
                updatedFacilities
            }
            _facilities.value = finalFacilities

            val activeFac = finalFacilities.firstOrNull { it.isActiveLocation } ?: finalFacilities[0]
            val updatedRigs = _rigs.value.map { it.copy(location = activeFac.name) }
            _rigs.value = updatedRigs

            showNotification("Срок аренды истёк! Фермы перемещены в '${activeFac.name}'.")
            saveGameState()
        }
    }

    fun rentFacility(facilityId: String, period: RentPeriod) {
        val facility = _facilities.value.find { it.id == facilityId } ?: return
        val cost = facility.getPrice(period)
        val stats = _playerStats.value

        if (cost > 0.0 && stats.balanceUsd < cost) {
            showNotification("Не хватает $${cost.toInt()} для ${if (period == RentPeriod.FOREVER) "покупки" else "аренды"}!")
            return
        }

        val now = System.currentTimeMillis()
        val updatedFacilities = _facilities.value.map { fac ->
            if (fac.id == facilityId) {
                if (period == RentPeriod.FOREVER) {
                    fac.copy(
                        isUnlocked = true,
                        isPermanent = true,
                        rentExpiresTimestampMs = Long.MAX_VALUE,
                        isActiveLocation = true
                    )
                } else {
                    val durationMs = period.days.toLong() * 86_400_000L
                    val baseExp = if (fac.isUnlocked && fac.rentExpiresTimestampMs > now) {
                        fac.rentExpiresTimestampMs
                    } else {
                        now
                    }
                    fac.copy(
                        isUnlocked = true,
                        rentExpiresTimestampMs = baseExp + durationMs,
                        isActiveLocation = true
                    )
                }
            } else {
                fac.copy(isActiveLocation = false)
            }
        }

        val activated = updatedFacilities.find { it.id == facilityId }!!
        val updatedRigs = _rigs.value.map {
            it.copy(location = activated.name)
        }

        _facilities.value = updatedFacilities
        _rigs.value = updatedRigs
        _playerStats.value = stats.copy(balanceUsd = stats.balanceUsd - cost)

        val actionText = if (period == RentPeriod.FOREVER) {
            "Помещение '${facility.name}' выкуплено навсегда!"
        } else {
            "Помещение '${facility.name}' арендовано на ${period.title}!"
        }
        showNotification("$actionText База перенесена сюда.")
        saveGameState()

        // Проверяем, запускает ли это сюжетный диалог
        val ch = _storyChapters.value.find { it.requiredFacilityId == facilityId }
        if (ch != null && !ch.isCompleted) {
            _activeStoryDialogue.value = ch
        }
    }

    fun setActiveFacility(facilityId: String) {
        val facility = _facilities.value.find { it.id == facilityId } ?: return
        if (!facility.isUnlocked) {
            showNotification("Сначала необходимо арендовать или выкупить это помещение!")
            return
        }

        val updatedFacilities = _facilities.value.map {
            it.copy(isActiveLocation = (it.id == facilityId))
        }
        val updatedRigs = _rigs.value.map {
            it.copy(location = facility.name)
        }

        _facilities.value = updatedFacilities
        _rigs.value = updatedRigs
        showNotification("Активная база: '${facility.name}'! Тариф: $${facility.electricityCostKwhUsd}/кВт⋅ч")
        saveGameState()

        val ch = _storyChapters.value.find { it.requiredFacilityId == facilityId }
        if (ch != null && !ch.isCompleted) {
            _activeStoryDialogue.value = ch
        }
    }

    fun applyOverclock(rigId: String, coreMhz: Int, memMhz: Int, powerLimit: Int, fanSpeed: Int) {
        val updated = _rigs.value.map { rig ->
            if (rig.id == rigId) {
                rig.copy(
                    coreClockOffsetMhz = coreMhz,
                    memoryClockOffsetMhz = memMhz,
                    powerLimitPercent = powerLimit,
                    fanSpeedPercent = fanSpeed
                )
            } else rig
        }
        _rigs.value = updated
        saveGameState()
        showNotification("Разгон применён! Память: +${memMhz}MHz, Ядро: ${if (coreMhz >= 0) "+$coreMhz" else "$coreMhz"}MHz, PL: $powerLimit%")
    }

    fun takeLoan(loanId: String) {
        val loan = _loans.value.find { it.id == loanId } ?: return
        if (loan.isActive) {
            showNotification("Этот кредит уже активен!")
            return
        }
        val stats = _playerStats.value
        val updatedLoans = _loans.value.map {
            if (it.id == loanId) it.copy(isActive = true, remainingDebtUsd = it.totalToRepayUsd) else it
        }
        _loans.value = updatedLoans
        val newStats = stats.copy(
            balanceUsd = stats.balanceUsd + loan.amountUsd,
            activeDebtUsd = stats.activeDebtUsd + loan.totalToRepayUsd
        )
        _playerStats.value = newStats
        showNotification("Кредит '${loan.title}' одобрен! На счет зачислено $${loan.amountUsd.toInt()}.")
        saveGameState()
    }

    fun repayLoan(loanId: String) {
        val loan = _loans.value.find { it.id == loanId } ?: return
        if (!loan.isActive) return
        val stats = _playerStats.value
        if (stats.balanceUsd < loan.remainingDebtUsd) {
            showNotification("Не хватает $${loan.remainingDebtUsd.toInt()} для погашения кредита!")
            return
        }
        val updatedLoans = _loans.value.map {
            if (it.id == loanId) it.copy(isActive = false, remainingDebtUsd = 0.0) else it
        }
        _loans.value = updatedLoans
        val newStats = stats.copy(
            balanceUsd = stats.balanceUsd - loan.remainingDebtUsd,
            activeDebtUsd = (stats.activeDebtUsd - loan.remainingDebtUsd).coerceAtLeast(0.0)
        )
        _playerStats.value = newStats
        showNotification("Кредит '${loan.title}' полностью погашен! Кредитная история чиста.")
        saveGameState()
    }

    fun completeChapter(chapterNum: Int) {
        val chapter = _storyChapters.value.find { it.chapterNumber == chapterNum } ?: return
        if (chapter.isCompleted) return

        val updatedChapters = _storyChapters.value.map { ch ->
            if (ch.chapterNumber == chapterNum) {
                ch.copy(isCompleted = true)
            } else if (ch.chapterNumber == chapterNum + 1) {
                ch.copy(isUnlocked = true)
            } else ch
        }
        _storyChapters.value = updatedChapters

        val stats = _playerStats.value
        val newStats = stats.copy(
            balanceUsd = stats.balanceUsd + chapter.rewardUsd,
            xp = stats.xp + chapter.rewardXp,
            currentStoryChapter = chapterNum + 1
        )
        _playerStats.value = newStats
        showNotification("Глава $chapterNum пройдена! Награда: +$${chapter.rewardUsd.toInt()} и +${chapter.rewardXp} XP!")
        saveGameState()

        val nextCh = updatedChapters.find { it.chapterNumber == chapterNum + 1 }
        if (nextCh != null) {
            _activeStoryDialogue.value = nextCh
        }
    }

    private fun checkStoryProgress() {
        val currentChapNum = _playerStats.value.currentStoryChapter
        val chapter = _storyChapters.value.find { it.chapterNumber == currentChapNum } ?: return
        if (chapter.isCompleted) return

        val totalHash = _rigs.value.sumOf { it.totalHashRateMh }
        val balance = _playerStats.value.balanceUsd
        val hasFacility = _facilities.value.any { it.id == chapter.requiredFacilityId && it.isUnlocked }

        var canComplete = true
        if (chapter.targetHashrateMh > 0 && totalHash < chapter.targetHashrateMh) canComplete = false
        if (chapter.targetBalanceUsd > 0 && balance < chapter.targetBalanceUsd) canComplete = false
        if (!hasFacility && chapter.chapterNumber > 1) canComplete = false

        if (canComplete) {
            completeChapter(currentChapNum)
        }
    }

    fun unlockFacility(facilityId: String) {
        rentFacility(facilityId, RentPeriod.FOREVER)
    }

    // === P2P АВИТО ФУНКЦИИ (PEER-TO-PEER MARKETPLACE) ===

    fun scanP2PNetwork() {
        p2pService.scanNetwork {
            showNotification("P2P сеть обновлена: обнаружены новые пиры!")
        }
    }

    fun buyP2PListing(rigId: String, listing: com.example.model.P2PListing) {
        val stats = _playerStats.value
        val price = listing.finalPriceUsd
        if (stats.balanceUsd < price) {
            showNotification("Не хватает средств ($%.1f) для покупки через P2P Escrow!".format(price))
            return
        }

        val rig = _rigs.value.find { it.id == rigId } ?: return

        // Проверка слотов
        if (listing.component.type == ComponentType.GPU) {
            val mb = rig.installedComponents.find { it.component.type == ComponentType.MOTHERBOARD }?.component
            val currentGpuCount = rig.installedComponents.count { it.component.type == ComponentType.GPU }
            val maxSlots = mb?.maxGpuSlots ?: 2
            if (currentGpuCount >= maxSlots) {
                showNotification("В риге нет свободных слотов под GPU (максимум $maxSlots)!")
                return
            }
        }

        val realCond = listing.conditionPercent
        val newInstalled = InstalledRigComponent(
            slotId = "slot_${System.currentTimeMillis()}",
            component = listing.component.copy(isUsed = true, baseDurability = realCond),
            durabilityPercent = realCond
        )

        val updatedRigs = _rigs.value.map {
            if (it.id == rigId) {
                val list = it.installedComponents.toMutableList()
                if (listing.component.type == ComponentType.PSU || listing.component.type == ComponentType.MOTHERBOARD ||
                    listing.component.type == ComponentType.COOLING || listing.component.type == ComponentType.CPU
                ) {
                    list.removeAll { comp -> comp.component.type == listing.component.type }
                }
                list.add(newInstalled)
                it.copy(installedComponents = list)
            } else it
        }

        _rigs.value = updatedRigs
        _playerStats.value = stats.copy(balanceUsd = stats.balanceUsd - price)
        p2pService.markListingAsSold(listing.id)

        // Результат игры-угадайки как на реальном Авито!
        val condInt = realCond.toInt()
        val avitoResult = when {
            condInt >= 80 -> "🎉 Повезло на Авито! ${listing.component.name} в отличном состоянии (ресурс $condInt%)! Продавец не обманул."
            condInt >= 50 -> "📦 Покупка с Авито: ${listing.component.name} рабочая, но с пробегом (ресурс $condInt%). Термопаста подсохла."
            else -> "🔥 Ужарка с Авито! ${listing.component.name} зажарена майнингом (ресурс всего $condInt%)! Продавец слукавил."
        }
        showNotification(avitoResult)
        saveGameState()
    }

    fun sellComponentToP2P(rigId: String, slotId: String, askingPrice: Double) {
        val rig = _rigs.value.find { it.id == rigId } ?: return
        val comp = rig.installedComponents.find { it.slotId == slotId } ?: return

        // Удаляем из рига
        val updatedRigs = _rigs.value.map {
            if (it.id == rigId) {
                val list = it.installedComponents.filter { c -> c.slotId != slotId }.toMutableList()
                it.copy(installedComponents = list)
            } else it
        }
        _rigs.value = updatedRigs

        p2pService.publishPlayerComponent(
            component = comp.component,
            rigId = rigId,
            slotId = slotId,
            durability = comp.durabilityPercent,
            askingPrice = askingPrice
        )
        showNotification("Выставили ${comp.component.name} на Авито P2P за $${askingPrice.toInt()}!")
        saveGameState()
    }

    fun claimSoldP2PPayment(saleId: String) {
        val sale = myP2PSales.value.find { it.id == saleId && it.isSold } ?: return
        val stats = _playerStats.value
        _playerStats.value = stats.copy(balanceUsd = stats.balanceUsd + sale.askingPriceUsd)
        p2pService.removePlayerListing(saleId)
        showNotification("Получена выплата от пира ${sale.buyerPeerName ?: "Пир"}: +$${sale.askingPriceUsd.toInt()} USD!")
        saveGameState()
    }

    fun updateP2PPlayerStats() {
        val stats = _playerStats.value
        val totalHash = _rigs.value.sumOf { it.totalHashRateMh }
        p2pService.updateLocalPlayerStats(
            name = stats.playerName,
            hashrate = totalHash,
            balance = stats.balanceUsd,
            rigsCount = _rigs.value.size,
            chapter = stats.currentStoryChapter
        )
    }

    fun selectInitialDifficulty(difficulty: com.example.model.GameDifficulty) {
        repo.setDifficultyChosen(true)
        _needsInitialDifficultySelection.value = false
        setDifficulty(difficulty)
        showNotification("Начало игры на сложности: ${difficulty.title}")
    }

    fun resetGameProgress(newDifficulty: com.example.model.GameDifficulty) {
        val currentName = _playerStats.value.playerName
        // Досрочный сброс через Настройки НЕ меняет номер сезона и таймер вайпа!
        repo.resetGameProgress(newDifficulty, currentName)

        _seasonInfo.value = repo.loadSeasonInfo()
        _playerStats.value = repo.loadPlayerStats()
        _rigs.value = repo.loadRigs()
        _cryptos.value = repo.getInitialCryptos()
        _facilities.value = repo.getFacilities()
        _quests.value = repo.getStoryQuests()
        _storyChapters.value = repo.loadStoryChapters()
        _loans.value = repo.loadLoans()

        // Сбрасываем только лоты игрока, сезон и P2P-сеть не затрагиваются
        p2pService.resetPlayerSales(newDifficulty)
        updateP2PPlayerStats()
        _needsInitialDifficultySelection.value = false
        showNotification("Прогресс сброшен! Сезон ${_seasonInfo.value.seasonNumber} продолжается (${newDifficulty.title})")
    }

    fun performSeasonalWipe(newDifficulty: com.example.model.GameDifficulty) {
        val currentName = _playerStats.value.playerName
        val newSeason = repo.performSeasonalWipe(newDifficulty, currentName)
        _seasonInfo.value = newSeason

        _playerStats.value = repo.loadPlayerStats()
        _rigs.value = repo.loadRigs()
        _cryptos.value = repo.getInitialCryptos()
        _facilities.value = repo.getFacilities()
        _quests.value = repo.getStoryQuests()
        _storyChapters.value = repo.loadStoryChapters()
        _loans.value = repo.loadLoans()

        p2pService.wipeAllAvitoData(newSeason.seasonNumber)
        p2pService.setDifficulty(newDifficulty)
        updateP2PPlayerStats()
        _showSeasonalWipeDialog.value = false
        _needsInitialDifficultySelection.value = false
        showNotification("Глобальный вайп выполнен! Начался Сезон ${newSeason.seasonNumber} (${newDifficulty.title})")
    }

    fun dismissSeasonalWipeDialog() {
        _showSeasonalWipeDialog.value = false
    }

    fun setDifficulty(difficulty: com.example.model.GameDifficulty) {
        val stats = _playerStats.value.copy(difficulty = difficulty)
        _playerStats.value = stats
        p2pService.setDifficulty(difficulty)
        updateP2PPlayerStats()
        saveGameState()
        val msg = when (difficulty) {
            com.example.model.GameDifficulty.EASY -> "Сложность: ЛЁГКАЯ (Боты на Авито включены, без реальных игроков)"
            com.example.model.GameDifficulty.NORMAL -> "Сложность: НОРМАЛЬНАЯ (Боты отключены! P2P сеть, без автопродажи)"
            com.example.model.GameDifficulty.HARD -> "Сложность: СЛОЖНАЯ (Хардкор P2P, без ботов, без автопродажи)"
        }
        showNotification(msg)
    }

    fun setGameDifficulty(difficulty: com.example.model.GameDifficulty) = setDifficulty(difficulty)

    fun setPlayerName(newName: String) {
        val trimmed = newName.trim().take(20)
        if (trimmed.isEmpty()) return
        val stats = _playerStats.value.copy(playerName = trimmed)
        _playerStats.value = stats
        updateP2PPlayerStats()
        saveGameState()
        showNotification("P2P имя обновлено: $trimmed")
    }

    fun instantSellToScrapPeer(rigId: String, slotId: String) {
        val rig = _rigs.value.find { it.id == rigId } ?: return
        val comp = rig.installedComponents.find { it.slotId == slotId } ?: return
        val basePrice = comp.component.priceUsd
        val payout = (basePrice * (comp.durabilityPercent / 100.0) * 0.55).coerceAtLeast(10.0)

        val updatedRigs = _rigs.value.map {
            if (it.id == rigId) {
                val list = it.installedComponents.filter { c -> c.slotId != slotId }.toMutableList()
                it.copy(installedComponents = list)
            } else it
        }
        _rigs.value = updatedRigs

        val stats = _playerStats.value
        _playerStats.value = stats.copy(balanceUsd = stats.balanceUsd + payout)
        showNotification("Мгновенно сдано P2P скупщику: ${comp.component.name} за +$${payout.toInt()} USD!")
        saveGameState()
    }

    private fun showNotification(msg: String) {
        _statusNotification.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_statusNotification.value == msg) {
                _statusNotification.value = null
            }
        }
    }

    fun saveGameState() {
        repo.savePlayerStats(_playerStats.value)
        repo.saveRigs(_rigs.value)
        repo.saveFacilities(_facilities.value)
        repo.saveStoryChapters(_storyChapters.value)
        repo.saveLoans(_loans.value)
    }

    override fun onCleared() {
        super.onCleared()
        saveGameState()
    }
}
