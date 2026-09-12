package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CryptoCurrency
import com.example.model.InstalledRigComponent
import com.example.model.MiningFacility
import com.example.model.MiningRig
import com.example.model.PlayerStats
import com.example.model.QuestTargetType
import com.example.model.StoryQuest
import org.json.JSONArray
import org.json.JSONObject

class ColorbitRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("colorbit_offline_save", Context.MODE_PRIVATE)

    fun loadPlayerStats(): PlayerStats {
        val balance = prefs.getFloat("balance_usd", 320.0f).toDouble()
        val eth = prefs.getFloat("crypto_ETH", 0.0f).toDouble()
        val btc = prefs.getFloat("crypto_BTC", 0.0f).toDouble()
        val rvn = prefs.getFloat("crypto_RVN", 0.0f).toDouble()
        val cfx = prefs.getFloat("crypto_CFX", 0.0f).toDouble()
        val totalMined = prefs.getFloat("total_mined_usd", 0.0f).toDouble()
        val electricity = prefs.getFloat("electricity_spent", 0.0f).toDouble()
        val level = prefs.getInt("level", 1)
        val xp = prefs.getInt("xp", 0)
        val days = prefs.getInt("in_game_days", 1)
        val lastTs = prefs.getLong("last_offline_ts", System.currentTimeMillis())
        val chapter = prefs.getInt("story_chapter", 1)
        val debt = prefs.getFloat("active_debt_usd", 0f).toDouble()
        val diffId = prefs.getString("game_difficulty", "NORMAL")
        val pName = prefs.getString("player_p2p_name", "Майнер-Игрок") ?: "Майнер-Игрок"

        return PlayerStats(
            balanceUsd = balance,
            cryptoBalances = mutableMapOf(
                "ETH" to eth,
                "BTC" to btc,
                "RVN" to rvn,
                "CFX" to cfx
            ),
            totalMinedUsd = totalMined,
            electricitySpentUsd = electricity,
            level = level,
            xp = xp,
            inGameDaysPassed = days,
            lastOfflineTimestampMs = lastTs,
            currentStoryChapter = chapter,
            activeDebtUsd = debt,
            difficulty = com.example.model.GameDifficulty.fromId(diffId),
            playerName = pName
        )
    }

    fun savePlayerStats(stats: PlayerStats) {
        prefs.edit().apply {
            putFloat("balance_usd", stats.balanceUsd.toFloat())
            putFloat("crypto_ETH", stats.cryptoBalances["ETH"]?.toFloat() ?: 0f)
            putFloat("crypto_BTC", stats.cryptoBalances["BTC"]?.toFloat() ?: 0f)
            putFloat("crypto_RVN", stats.cryptoBalances["RVN"]?.toFloat() ?: 0f)
            putFloat("crypto_CFX", stats.cryptoBalances["CFX"]?.toFloat() ?: 0f)
            putFloat("total_mined_usd", stats.totalMinedUsd.toFloat())
            putFloat("electricity_spent", stats.electricitySpentUsd.toFloat())
            putInt("level", stats.level)
            putInt("xp", stats.xp)
            putInt("in_game_days", stats.inGameDaysPassed)
            putLong("last_offline_ts", System.currentTimeMillis())
            putInt("story_chapter", stats.currentStoryChapter)
            putFloat("active_debt_usd", stats.activeDebtUsd.toFloat())
            putString("game_difficulty", stats.difficulty.id)
            putString("player_p2p_name", stats.playerName)
            apply()
        }
    }

    fun loadRigs(): List<MiningRig> {
        val json = prefs.getString("rigs_json", null)
        if (json.isNullOrEmpty()) {
            return getInitialRigs()
        }
        return try {
            val list = mutableListOf<MiningRig>()
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val rigId = obj.getString("id")
                val name = obj.getString("name")
                val location = obj.optString("location", "Гараж")
                val isPoweredOn = obj.optBoolean("isPoweredOn", true)
                val cryptoId = obj.optString("currentCryptoId", "ETH")
                val temp = obj.optDouble("currentTemperatureC", 45.0).toFloat()
                val dust = obj.optDouble("dustLevelPercent", 10.0).toFloat()
                val paste = obj.optDouble("thermalPasteCondition", 100.0).toFloat()
                val coreOffset = obj.optInt("coreClockOffsetMhz", 0)
                val memOffset = obj.optInt("memoryClockOffsetMhz", 0)
                val powerLimit = obj.optInt("powerLimitPercent", 100)
                val fanSpeed = obj.optInt("fanSpeedPercent", 75)

                val compArray = obj.getJSONArray("components")
                val compList = mutableListOf<InstalledRigComponent>()
                for (j in 0 until compArray.length()) {
                    val cObj = compArray.getJSONObject(j)
                    val compId = cObj.getString("componentId")
                    val slotId = cObj.optString("slotId", "slot_$j")
                    val dur = cObj.optDouble("durability", 100.0).toFloat()
                    val dustComp = cObj.optDouble("dust", 0.0).toFloat()

                    val allCompCatalog = ComponentCatalog.allComponents + ComponentCatalog.usedComponents
                    val catalogComp = allCompCatalog.find { it.id == compId }
                    if (catalogComp != null) {
                        compList.add(
                            InstalledRigComponent(
                                slotId = slotId,
                                component = catalogComp,
                                durabilityPercent = dur,
                                dustPercent = dustComp
                            )
                        )
                    }
                }

                list.add(
                    MiningRig(
                        id = rigId,
                        name = name,
                        location = location,
                        isPoweredOn = isPoweredOn,
                        currentCryptoId = cryptoId,
                        installedComponents = compList,
                        currentTemperatureC = temp,
                        dustLevelPercent = dust,
                        thermalPasteCondition = paste,
                        coreClockOffsetMhz = coreOffset,
                        memoryClockOffsetMhz = memOffset,
                        powerLimitPercent = powerLimit,
                        fanSpeedPercent = fanSpeed
                    )
                )
            }
            if (list.isEmpty()) getInitialRigs() else list
        } catch (e: Exception) {
            getInitialRigs()
        }
    }

    fun saveRigs(rigs: List<MiningRig>) {
        val array = JSONArray()
        for (rig in rigs) {
            val obj = JSONObject().apply {
                put("id", rig.id)
                put("name", rig.name)
                put("location", rig.location)
                put("isPoweredOn", rig.isPoweredOn)
                put("currentCryptoId", rig.currentCryptoId)
                put("currentTemperatureC", rig.currentTemperatureC.toDouble())
                put("dustLevelPercent", rig.dustLevelPercent.toDouble())
                put("thermalPasteCondition", rig.thermalPasteCondition.toDouble())
                put("coreClockOffsetMhz", rig.coreClockOffsetMhz)
                put("memoryClockOffsetMhz", rig.memoryClockOffsetMhz)
                put("powerLimitPercent", rig.powerLimitPercent)
                put("fanSpeedPercent", rig.fanSpeedPercent)

                val compArray = JSONArray()
                for (c in rig.installedComponents) {
                    val cObj = JSONObject().apply {
                        put("slotId", c.slotId)
                        put("componentId", c.component.id)
                        put("durability", c.durabilityPercent.toDouble())
                        put("dust", c.dustPercent.toDouble())
                    }
                    compArray.put(cObj)
                }
                put("components", compArray)
            }
            array.put(obj)
        }
        prefs.edit().putString("rigs_json", array.toString()).apply()
    }

    private fun getInitialRigs(): List<MiningRig> {
        val mb = ComponentCatalog.allComponents.find { it.id == "mb_basic" }!!
        val cpu = ComponentCatalog.allComponents.find { it.id == "cpu_celeron" }!!
        val ram = ComponentCatalog.allComponents.find { it.id == "ram_4gb" }!!
        val psu = ComponentCatalog.allComponents.find { it.id == "psu_500w" }!!
        val cool = ComponentCatalog.allComponents.find { it.id == "cool_box" }!!
        val gpu = ComponentCatalog.allComponents.find { it.id == "gpu_gtx1050ti" }!!

        val starterRig = MiningRig(
            id = "rig_1",
            name = "Риг #1 (Стартовый)",
            location = "Квартира родителей",
            isPoweredOn = true,
            currentCryptoId = "ETH",
            installedComponents = mutableListOf(
                InstalledRigComponent("slot_mb", mb),
                InstalledRigComponent("slot_cpu", cpu),
                InstalledRigComponent("slot_ram_1", ram),
                InstalledRigComponent("slot_psu", psu),
                InstalledRigComponent("slot_cool", cool),
                InstalledRigComponent("slot_gpu_1", gpu)
            ),
            currentTemperatureC = 46f,
            dustLevelPercent = 5f,
            thermalPasteCondition = 100f
        )
        return listOf(starterRig)
    }

    fun getInitialCryptos(): List<CryptoCurrency> {
        return listOf(
            CryptoCurrency(
                id = "ETH",
                name = "Ethereum",
                symbol = "ETH",
                priceUsd = 2650.0,
                difficulty = 1.0,
                algo = "Ethash",
                colorHex = 0xFF627EEA,
                priceHistory = mutableListOf(2580.0, 2610.0, 2640.0, 2650.0)
            ),
            CryptoCurrency(
                id = "BTC",
                name = "Bitcoin",
                symbol = "BTC",
                priceUsd = 62400.0,
                difficulty = 28.5,
                algo = "SHA-256",
                colorHex = 0xFFF7931A,
                priceHistory = mutableListOf(61200.0, 61900.0, 62200.0, 62400.0)
            ),
            CryptoCurrency(
                id = "RVN",
                name = "Ravencoin",
                symbol = "RVN",
                priceUsd = 0.024,
                difficulty = 0.4,
                algo = "KawPow",
                colorHex = 0xFFF05238,
                priceHistory = mutableListOf(0.022, 0.023, 0.025, 0.024)
            ),
            CryptoCurrency(
                id = "CFX",
                name = "Conflux",
                symbol = "CFX",
                priceUsd = 0.175,
                difficulty = 0.7,
                algo = "Octopus",
                colorHex = 0xFF18A0FB,
                priceHistory = mutableListOf(0.165, 0.169, 0.172, 0.175)
            )
        )
    }

    fun getDefaultFacilities(): List<MiningFacility> {
        return listOf(
            MiningFacility(
                id = "facility_parents_apt",
                name = "Квартира родителей",
                category = "Родительский дом",
                description = "Стартовая комната в родительской квартире. Тесно, шуметь нельзя, а за свет ругают, зато аренда полностью бесплатная.",
                electricityCostKwhUsd = 0.08,
                maxRigs = 2,
                ambientTempC = 24.0f,
                dustAccumulationRate = 1.0f,
                price2DaysUsd = 0.0,
                price7DaysUsd = 0.0,
                price30DaysUsd = 0.0,
                priceForeverUsd = 0.0,
                isUnlocked = true,
                isPermanent = true,
                isActiveLocation = true
            ),
            MiningFacility(
                id = "facility_garage",
                name = "Гараж в кооперативе",
                category = "Кооператив",
                description = "Капитальный гараж с отдельным щитком. Можно гудеть вентиляторами круглые сутки, но пыльно и сквозит.",
                electricityCostKwhUsd = 0.06,
                maxRigs = 5,
                ambientTempC = 20.0f,
                dustAccumulationRate = 1.2f,
                price2DaysUsd = 35.0,
                price7DaysUsd = 95.0,
                price30DaysUsd = 320.0,
                priceForeverUsd = 950.0,
                isUnlocked = false,
                isPermanent = false,
                isActiveLocation = false
            ),
            MiningFacility(
                id = "facility_apartment",
                name = "Съемная 2-комнатная квартира",
                category = "Арендное жилье",
                description = "Квартира с отдельной комнатой под фермы. Проведена медная проводка 10 кВт, есть кондиционер.",
                electricityCostKwhUsd = 0.05,
                maxRigs = 10,
                ambientTempC = 18.0f,
                dustAccumulationRate = 0.6f,
                price2DaysUsd = 80.0,
                price7DaysUsd = 240.0,
                price30DaysUsd = 790.0,
                priceForeverUsd = 2400.0,
                isUnlocked = false,
                isPermanent = false,
                isActiveLocation = false
            ),
            MiningFacility(
                id = "facility_cottage",
                name = "Загородный дом / Дача",
                category = "Частный сектор",
                description = "Собственный участок за городом. Ввод 380В на 15 кВт, прохладный сухой подвал для идеального охлаждения.",
                electricityCostKwhUsd = 0.04,
                maxRigs = 18,
                ambientTempC = 15.0f,
                dustAccumulationRate = 0.45f,
                price2DaysUsd = 180.0,
                price7DaysUsd = 540.0,
                price30DaysUsd = 1800.0,
                priceForeverUsd = 5500.0,
                isUnlocked = false,
                isPermanent = false,
                isActiveLocation = false
            ),
            MiningFacility(
                id = "facility_warehouse",
                name = "Складской ангар в промзоне",
                category = "Коммерческая недвижимость",
                description = "Охраняемый ангар с промышленной сетью 50 кВт и профессиональной приточно-вытяжной вентиляцией.",
                electricityCostKwhUsd = 0.032,
                maxRigs = 35,
                ambientTempC = 13.0f,
                dustAccumulationRate = 0.3f,
                price2DaysUsd = 450.0,
                price7DaysUsd = 1350.0,
                price30DaysUsd = 4500.0,
                priceForeverUsd = 14000.0,
                isUnlocked = false,
                isPermanent = false,
                isActiveLocation = false
            ),
            MiningFacility(
                id = "facility_hydro",
                name = "Майнинг-отель у ГЭС в Сибири",
                category = "Промышленный ЦОД",
                description = "Дата-центр вблизи Братской ГЭС. Круглогодичный морозный воздух и сверхдешевая экологичная гидроэнергия.",
                electricityCostKwhUsd = 0.020,
                maxRigs = 75,
                ambientTempC = 7.0f,
                dustAccumulationRate = 0.1f,
                price2DaysUsd = 1200.0,
                price7DaysUsd = 3600.0,
                price30DaysUsd = 12000.0,
                priceForeverUsd = 38000.0,
                isUnlocked = false,
                isPermanent = false,
                isActiveLocation = false
            ),
            MiningFacility(
                id = "facility_dubai_villa",
                name = "Элитная вилла в Дубае (Palm Jumeirah)",
                category = "Люкс / Финал сюжета",
                description = "Вершина успеха в Colorbit! Роскошная вилла с подземной серверной, мощнейшей чиллерной системой и солнечными батареями.",
                electricityCostKwhUsd = 0.012,
                maxRigs = 150,
                ambientTempC = 14.0f,
                dustAccumulationRate = 0.04f,
                price2DaysUsd = 3000.0,
                price7DaysUsd = 9000.0,
                price30DaysUsd = 29000.0,
                priceForeverUsd = 90000.0,
                isUnlocked = false,
                isPermanent = false,
                isActiveLocation = false
            )
        )
    }

    fun loadFacilities(): List<MiningFacility> {
        val defaults = getDefaultFacilities()
        val json = prefs.getString("facilities_json", null) ?: return defaults
        return try {
            val array = JSONArray(json)
            val map = mutableMapOf<String, JSONObject>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                map[obj.getString("id")] = obj
            }
            defaults.map { defaultFac ->
                val saved = map[defaultFac.id]
                if (saved != null) {
                    val isUnlocked = saved.optBoolean("isUnlocked", defaultFac.isUnlocked)
                    val isPermanent = saved.optBoolean("isPermanent", defaultFac.isPermanent)
                    val rentExp = saved.optLong("rentExpiresTimestampMs", defaultFac.rentExpiresTimestampMs)
                    val isActive = saved.optBoolean("isActiveLocation", defaultFac.isActiveLocation)
                    defaultFac.copy(
                        isUnlocked = isUnlocked,
                        isPermanent = isPermanent,
                        rentExpiresTimestampMs = rentExp,
                        isActiveLocation = isActive
                    )
                } else {
                    defaultFac
                }
            }
        } catch (e: Exception) {
            defaults
        }
    }

    fun saveFacilities(facilities: List<MiningFacility>) {
        val array = JSONArray()
        for (f in facilities) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("isUnlocked", f.isUnlocked)
                put("isPermanent", f.isPermanent)
                put("rentExpiresTimestampMs", f.rentExpiresTimestampMs)
                put("isActiveLocation", f.isActiveLocation)
            }
            array.put(obj)
        }
        prefs.edit().putString("facilities_json", array.toString()).apply()
    }

    fun getFacilities(): List<MiningFacility> = loadFacilities()

    fun getStoryQuests(): List<StoryQuest> {
        return listOf(
            StoryQuest(
                id = "quest_first_crypto",
                title = "Первые мегахеши",
                description = "Запусти риг и добудь свои первые 0.005 ETH",
                targetType = QuestTargetType.MINE_CRYPTO_AMOUNT,
                targetValue = 0.005,
                rewardUsd = 50.0,
                rewardXp = 100
            ),
            StoryQuest(
                id = "quest_upgrade_gpu",
                title = "Апгрейд железа",
                description = "Купи вторую видеокарту в магазине и установи в риг",
                targetType = QuestTargetType.BUY_COMPONENTS_COUNT,
                targetValue = 2.0,
                rewardUsd = 75.0,
                rewardXp = 150
            ),
            StoryQuest(
                id = "quest_clean_dust",
                title = "Мастер чистоты",
                description = "Очисти ферму баллоном со сжатым воздухом или замени термопасту",
                targetType = QuestTargetType.CLEAN_DUST_TIMES,
                targetValue = 1.0,
                rewardUsd = 30.0,
                rewardXp = 80
            ),
            StoryQuest(
                id = "quest_big_hashrate",
                title = "Ферма на взлёт",
                description = "Разгони суммарный хэшрейт до 100 MH/s",
                targetType = QuestTargetType.BUILD_TOTAL_HASHRATE,
                targetValue = 100.0,
                rewardUsd = 200.0,
                rewardXp = 350
            ),
            StoryQuest(
                id = "quest_rich",
                title = "Крипто-магнат",
                description = "Накопи 1,000$ на балансе наличными",
                targetType = QuestTargetType.REACH_BALANCE_USD,
                targetValue = 1000.0,
                rewardUsd = 400.0,
                rewardXp = 600
            )
        )
    }

    fun loadStoryChapters(): List<com.example.model.StoryChapter> {
        val defaults = StoryCampaignCatalog.chapters
        val json = prefs.getString("story_chapters_json", null) ?: return defaults
        return try {
            val array = JSONArray(json)
            val map = mutableMapOf<Int, JSONObject>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                map[obj.getInt("chapterNumber")] = obj
            }
            defaults.map { def ->
                val saved = map[def.chapterNumber]
                if (saved != null) {
                    def.copy(
                        isCompleted = saved.optBoolean("isCompleted", def.isCompleted),
                        isUnlocked = saved.optBoolean("isUnlocked", def.isUnlocked)
                    )
                } else def
            }
        } catch (e: Exception) {
            defaults
        }
    }

    fun saveStoryChapters(chapters: List<com.example.model.StoryChapter>) {
        val array = JSONArray()
        for (ch in chapters) {
            val obj = JSONObject().apply {
                put("chapterNumber", ch.chapterNumber)
                put("isCompleted", ch.isCompleted)
                put("isUnlocked", ch.isUnlocked)
            }
            array.put(obj)
        }
        prefs.edit().putString("story_chapters_json", array.toString()).apply()
    }

    fun loadLoans(): List<com.example.model.VirtualLoan> {
        val defaults = ComponentCatalog.loanOptions
        val json = prefs.getString("loans_json", null) ?: return defaults
        return try {
            val array = JSONArray(json)
            val map = mutableMapOf<String, JSONObject>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                map[obj.getString("id")] = obj
            }
            defaults.map { def ->
                val saved = map[def.id]
                if (saved != null) {
                    def.copy(
                        isActive = saved.optBoolean("isActive", def.isActive),
                        remainingDebtUsd = saved.optDouble("remainingDebtUsd", def.remainingDebtUsd)
                    )
                } else def
            }
        } catch (e: Exception) {
            defaults
        }
    }

    fun saveLoans(loans: List<com.example.model.VirtualLoan>) {
        val array = JSONArray()
        for (l in loans) {
            val obj = JSONObject().apply {
                put("id", l.id)
                put("isActive", l.isActive)
                put("remainingDebtUsd", l.remainingDebtUsd)
            }
            array.put(obj)
        }
        prefs.edit().putString("loans_json", array.toString()).apply()
    }

    fun hasChosenDifficulty(): Boolean {
        return prefs.getBoolean("has_chosen_difficulty", false)
    }

    fun setDifficultyChosen(chosen: Boolean) {
        prefs.edit().putBoolean("has_chosen_difficulty", chosen).apply()
    }

    fun loadSeasonInfo(): com.example.model.SeasonInfo {
        val season = prefs.getInt("season_number", 1)
        var startTs = prefs.getLong("season_start_ts", 0L)
        if (startTs <= 0L) {
            startTs = System.currentTimeMillis()
            prefs.edit().putLong("season_start_ts", startTs).apply()
        }
        return com.example.model.SeasonInfo(seasonNumber = season, seasonStartTimestampMs = startTs, durationDays = 90)
    }

    fun saveSeasonInfo(seasonInfo: com.example.model.SeasonInfo) {
        prefs.edit().apply {
            putInt("season_number", seasonInfo.seasonNumber)
            putLong("season_start_ts", seasonInfo.seasonStartTimestampMs)
            apply()
        }
    }

    fun performSeasonalWipe(initialDifficulty: com.example.model.GameDifficulty, keepPlayerName: String? = null): com.example.model.SeasonInfo {
        val currentSeason = prefs.getInt("season_number", 1)
        val nextSeason = currentSeason + 1
        val newStartTs = System.currentTimeMillis()

        prefs.edit().clear().apply()
        prefs.edit().apply {
            putBoolean("has_chosen_difficulty", true)
            putString("game_difficulty", initialDifficulty.id)
            putInt("season_number", nextSeason)
            putLong("season_start_ts", newStartTs)
            if (!keepPlayerName.isNullOrBlank()) {
                putString("player_p2p_name", keepPlayerName)
            }
            apply()
        }
        return com.example.model.SeasonInfo(seasonNumber = nextSeason, seasonStartTimestampMs = newStartTs, durationDays = 90)
    }

    fun resetGameProgress(initialDifficulty: com.example.model.GameDifficulty, keepPlayerName: String? = null) {
        val curSeason = prefs.getInt("season_number", 1)
        val curStartTs = prefs.getLong("season_start_ts", System.currentTimeMillis())

        prefs.edit().clear().apply()
        prefs.edit().apply {
            putBoolean("has_chosen_difficulty", true)
            putString("game_difficulty", initialDifficulty.id)
            putInt("season_number", curSeason)
            putLong("season_start_ts", curStartTs)
            if (!keepPlayerName.isNullOrBlank()) {
                putString("player_p2p_name", keepPlayerName)
            }
            apply()
        }
    }
}
