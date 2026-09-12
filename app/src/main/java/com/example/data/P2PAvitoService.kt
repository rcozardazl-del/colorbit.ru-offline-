package com.example.data

import com.example.model.ComponentType
import com.example.model.GameDifficulty
import com.example.model.MyP2PSaleListing
import com.example.model.P2PLeaderboardEntry
import com.example.model.P2PListing
import com.example.model.P2PSwarmState
import com.example.model.PCComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.UUID
import kotlin.random.Random

class P2PAvitoService(private val scope: CoroutineScope) {

    private val myPeerId = "peer_0x" + UUID.randomUUID().toString().substring(0, 6)
    private var currentDifficulty: GameDifficulty = GameDifficulty.NORMAL
    private var localPlayerName: String = "Майнер-Игрок"
    private var localHashRate: Double = 0.0
    private var localBalance: Double = 350.0
    private var localRigsCount: Int = 1
    private var localChapter: Int = 1

    private val _swarmState = MutableStateFlow(
        P2PSwarmState(
            isConnected = true,
            myPeerId = myPeerId,
            myPeerName = "Вы ($localPlayerName)",
            peersCount = 18,
            averagePingMs = 21,
            networkMode = "P2P Mesh / UDP & DHT Swarm",
            localSubnetIp = "192.168.1." + Random.nextInt(10, 240),
            isScanning = false
        )
    )
    val swarmState: StateFlow<P2PSwarmState> = _swarmState.asStateFlow()

    private val _listings = MutableStateFlow<List<P2PListing>>(emptyList())
    val listings: StateFlow<List<P2PListing>> = _listings.asStateFlow()

    private val _mySales = MutableStateFlow<List<MyP2PSaleListing>>(emptyList())
    val mySales: StateFlow<List<MyP2PSaleListing>> = _mySales.asStateFlow()

    private val _leaderboardEntries = MutableStateFlow<List<P2PLeaderboardEntry>>(emptyList())
    val leaderboardEntries: StateFlow<List<P2PLeaderboardEntry>> = _leaderboardEntries.asStateFlow()

    // Базовые боты-продавцы (используются ТОЛЬКО на легкой сложности)
    private val botPeerNames = listOf(
        "@CryptoBrat_SPB", "@Miner_Vovan77", "@SatsStacker", "@Hardware_Ghost",
        "@CyberFarm_Novosib", "@BitMaster_Ekb", "@GarageRig_Vitek", "@GpuCollector",
        "@Overclock_Pro", "@BtcNomad", "@P2P_Escrow_Whale", "@Dmitriy_Mining",
        "@EthMaxi_Kazan", "@RigFixer_99", "@Alex_HashRate", "@Stas_Silicon"
    )

    private val botPeerClaims = listOf(
        "Не бита, не крашена, пломбы на месте",
        "Стояла в офисном ПК у бухгалтера",
        "Использовалась чисто под CS:GO и браузер",
        "Майнила только по праздникам с даунвольтом",
        "Без проверок, снята со старого рига",
        "Идеальное состояние, пылинки сдувал",
        "Вроде рабочая, вертушка немного шуршит",
        "Стояла на балконе, температуры ледяные",
        "Продаю как есть, манибэка нет",
        "Состояние пушка, любые тесты FurMark",
        "Домашний ПК, пломбы не срывались",
        "Память Samsung, не грелась (честно!)"
    )

    private val botPeerComments = listOf(
        "Стояла в сухом помещении, даунвольт 65%, вертушки в норме.",
        "Срочно нужны USD на оплату аренды розетки!",
        "Память Samsung! Разгоняется отлично, держала стабильно.",
        "Куплена год назад, пломбы на месте, термопаста свежая.",
        "Распродаю риг из-за переезда, отдаю через P2P Escrow.",
        "С домашнего ПК, майнила только по ночам, без перегревов.",
        "Блок питания с сертификатом 80+ Gold, чистые 12V без просадок.",
        "Использовалась в продуваемом каркасе, температуры в норме."
    )

    // Сохранённые лоты от живых игроков локальной сети
    private val realLanListings = mutableListOf<P2PListing>()
    // Реальные живые пиры из LAN сети
    private val discoveredLanPeers = mutableMapOf<String, P2PLeaderboardEntry>()

    init {
        initBaseLeaderboard()
        startLanPeerDiscovery()
        startSalesLifecycleEngine()
    }

    fun setDifficulty(difficulty: GameDifficulty) {
        if (currentDifficulty == difficulty) return
        currentDifficulty = difficulty

        if (difficulty == GameDifficulty.EASY) {
            // На легкой сложности: включаем ботов, скрываем реальных людей
            generateBotListings()
        } else {
            // На нормальной и сложной:
            // "в сложности норм и сложно убери генерацию ботов на Авито"
            // Очищаем ботов, оставляем ТОЛЬКО лоты от реальных P2P пиров!
            _listings.value = realLanListings.toList()
        }
        recalculateLeaderboard()
    }

    fun wipeAllAvitoData(newSeasonNumber: Int) {
        // Полный сброс рынка Авито и P2P сети каждые 3 месяца (вайп сезона):
        // 1. Очищаем все пользовательские лоты на продаже
        _mySales.value = emptyList()
        // 2. Очищаем сохраненные P2P объявления из локальной сети
        realLanListings.clear()
        // 3. Очищаем найденных LAN пиров
        discoveredLanPeers.clear()
        // 4. Перегенерируем лоты рынка для нового сезона
        if (currentDifficulty == GameDifficulty.EASY) {
            generateBotListings()
        } else {
            _listings.value = emptyList()
        }
        // 5. Перезапускаем базовую таблицу лидеров
        initBaseLeaderboard()
        // 6. Оповещаем локальную P2P сеть о смене сезона
        broadcastSeasonWipe(newSeasonNumber)
    }

    private fun broadcastSeasonWipe(seasonNumber: Int) {
        scope.launch(Dispatchers.IO) {
            try {
                val msg = "COLORBIT_SEASON_WIPE|$myPeerId|$seasonNumber|${System.currentTimeMillis()}"
                val sendData = msg.toByteArray()
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                DatagramSocket().use { sock ->
                    sock.broadcast = true
                    sock.send(DatagramPacket(sendData, sendData.size, broadcastAddress, 18888))
                }
            } catch (_: Exception) {
            }
        }
    }

    fun updateLocalPlayerStats(name: String, hashrate: Double, balance: Double, rigsCount: Int, chapter: Int) {
        localPlayerName = name
        localHashRate = hashrate
        localBalance = balance
        localRigsCount = rigsCount
        localChapter = chapter

        _swarmState.value = _swarmState.value.copy(
            myPeerName = "Вы ($name)"
        )
        recalculateLeaderboard()
    }

    private fun initBaseLeaderboard() {
        // Базовые майнинг-ноды P2P сети (Mesh DHT ноды)
        val initialNodes = listOf(
            P2PLeaderboardEntry("peer_0x9a1c", "Satoshi_Ghost", 2850.0, 142000.0, 12, 6, 14, isLocalPlayer = false, isLanPeer = false),
            P2PLeaderboardEntry("peer_0x4b72", "AsicMaster_CN", 1920.0, 89500.0, 8, 5, 26, isLocalPlayer = false, isLanPeer = false),
            P2PLeaderboardEntry("peer_0x7d3e", "CyberPool_MSK", 1340.0, 48300.0, 6, 5, 18, isLocalPlayer = false, isLanPeer = false),
            P2PLeaderboardEntry("peer_0x3e8a", "GigaWatt_Miner", 860.0, 29800.0, 5, 4, 22, isLocalPlayer = false, isLanPeer = false),
            P2PLeaderboardEntry("peer_0x1f90", "EtherBaron", 540.0, 16400.0, 4, 3, 19, isLocalPlayer = false, isLanPeer = false),
            P2PLeaderboardEntry("peer_0x8c4b", "GarageRig_Pro", 380.0, 9200.0, 3, 3, 24, isLocalPlayer = false, isLanPeer = false),
            P2PLeaderboardEntry("peer_0x5a2d", "SoloStaker_99", 210.0, 5100.0, 2, 2, 28, isLocalPlayer = false, isLanPeer = false),
            P2PLeaderboardEntry("peer_0x2c1f", "BalconyFarm", 145.0, 3400.0, 2, 2, 17, isLocalPlayer = false, isLanPeer = false),
            P2PLeaderboardEntry("peer_0x6e78", "NoviceHash_01", 60.0, 1100.0, 1, 1, 31, isLocalPlayer = false, isLanPeer = false)
        )
        val localEntry = P2PLeaderboardEntry(
            peerId = myPeerId,
            playerName = localPlayerName,
            hashRateMh = localHashRate,
            balanceUsd = localBalance,
            rigsCount = localRigsCount,
            chapter = localChapter,
            pingMs = 1,
            isLocalPlayer = true,
            isLanPeer = false,
            difficulty = currentDifficulty.id
        )
        val combined = (initialNodes + localEntry).sortedByDescending { it.hashRateMh }
        _leaderboardEntries.value = combined
    }

    private fun recalculateLeaderboard() {
        val currentList = _leaderboardEntries.value.toMutableList()
        // Удаляем старую запись игрока
        currentList.removeAll { it.isLocalPlayer }

        val localEntry = P2PLeaderboardEntry(
            peerId = myPeerId,
            playerName = localPlayerName,
            hashRateMh = localHashRate,
            balanceUsd = localBalance,
            rigsCount = localRigsCount,
            chapter = localChapter,
            pingMs = 1,
            isLocalPlayer = true,
            isLanPeer = false,
            difficulty = currentDifficulty.id
        )
        currentList.add(localEntry)

        // Добавляем обнаруженных реальных LAN пиров (если не на легкой сложности)
        if (currentDifficulty != GameDifficulty.EASY) {
            discoveredLanPeers.values.forEach { lanPeer ->
                currentList.removeAll { it.peerId == lanPeer.peerId }
                currentList.add(lanPeer)
            }
        } else {
            // "на лехкой сложности не должно быть ряльных людей" -> убираем LAN пиров
            currentList.removeAll { it.isLanPeer }
        }

        _leaderboardEntries.value = currentList.sortedByDescending { it.hashRateMh }
    }

    // Генерация ботов на Авито (ТОЛЬКО для легкой сложности!)
    private fun generateBotListings() {
        val baseComponents = ComponentCatalog.allComponents
        val generated = mutableListOf<P2PListing>()

        baseComponents.forEachIndexed { index, comp ->
            val priceFactor = Random.nextDouble(0.65, 0.85)
            val realCondition = Random.nextInt(20, 96).toFloat()
            val peerIndex = index % botPeerNames.size
            val sellerName = botPeerNames[peerIndex]
            val peerId = "bot_0x" + Integer.toHexString(sellerName.hashCode()).takeLast(4)
            val p2pPrice = (comp.priceUsd * priceFactor).coerceAtLeast(15.0)
            val claim = botPeerClaims[index % botPeerClaims.size]

            val usedComp = comp.copy(
                id = "p2p_${comp.id}_$index",
                name = comp.name,
                priceUsd = p2pPrice,
                isUsed = true,
                baseDurability = realCondition,
                description = "P2P лот от $sellerName. Продавец: «$claim»"
            )

            generated.add(
                P2PListing(
                    id = "p2p_bot_${UUID.randomUUID().toString().take(8)}",
                    component = usedComp,
                    sellerPeerId = peerId,
                    sellerName = sellerName,
                    sellerRating = Random.nextDouble(4.6, 5.0).let { "%.1f".format(it).replace(',', '.').toFloat() },
                    successfulDeals = Random.nextInt(12, 85),
                    pingMs = Random.nextInt(12, 48),
                    conditionPercent = realCondition,
                    sellerClaim = claim,
                    sellerComment = botPeerComments[index % botPeerComments.size],
                    priceUsd = p2pPrice,
                    isRealPeer = false
                )
            )
        }

        _listings.value = generated.shuffled()
    }

    fun scanNetwork(onCompleted: () -> Unit = {}) {
        scope.launch {
            _swarmState.value = _swarmState.value.copy(isScanning = true)
            delay(1000)

            // Отправляем форсированный broadcast в локальную сеть
            broadcastPeerAnnounce()

            if (currentDifficulty == GameDifficulty.EASY) {
                // На легкой сложности: можно добавить пару ботов
                val current = _listings.value.filter { !it.isSold }.toMutableList()
                val randomComp = ComponentCatalog.allComponents.random()
                val factor = Random.nextDouble(0.60, 0.82)
                val realCond = Random.nextInt(20, 96).toFloat()
                val seller = botPeerNames.random()
                val claim = botPeerClaims.random()
                val freshP2P = P2PListing(
                    id = "p2p_bot_fresh_${System.currentTimeMillis()}",
                    component = randomComp.copy(
                        id = "p2p_bot_comp_${System.currentTimeMillis()}",
                        priceUsd = randomComp.priceUsd * factor,
                        isUsed = true,
                        baseDurability = realCond,
                        description = "P2P лот от $seller. Продавец: «$claim»"
                    ),
                    sellerPeerId = "bot_0x" + Random.nextInt(1000, 9999),
                    sellerName = seller,
                    sellerRating = 4.9f,
                    successfulDeals = Random.nextInt(20, 60),
                    pingMs = Random.nextInt(15, 35),
                    conditionPercent = realCond,
                    sellerClaim = claim,
                    sellerComment = "Только что выложил на Авито! Забирайте.",
                    priceUsd = randomComp.priceUsd * factor,
                    isRealPeer = false
                )
                current.add(0, freshP2P)
                _listings.value = current.take(25)
            } else {
                // На нормальной и сложной сложности:
                // "в сложности норм и сложно убери генерацию ботов на Авито"
                // НИКАКОЙ генерации ботов! Только реальные пиры из LAN сети!
                _listings.value = realLanListings.toList()
            }

            val lanCount = if (currentDifficulty == GameDifficulty.EASY) 0 else discoveredLanPeers.size
            _swarmState.value = _swarmState.value.copy(
                peersCount = 18 + lanCount,
                averagePingMs = if (lanCount > 0) 8 else 21,
                isScanning = false
            )
            onCompleted()
        }
    }

    fun markListingAsSold(listingId: String) {
        _listings.value = _listings.value.map {
            if (it.id == listingId) it.copy(isSold = true) else it
        }
        realLanListings.removeAll { it.id == listingId }
    }

    fun publishPlayerComponent(
        component: PCComponent,
        rigId: String,
        slotId: String,
        durability: Float,
        askingPrice: Double
    ): MyP2PSaleListing {
        val newSale = MyP2PSaleListing(
            id = "sale_${System.currentTimeMillis()}",
            component = component,
            rigId = rigId,
            slotId = slotId,
            durabilityPercent = durability,
            askingPriceUsd = askingPrice,
            createdAtTimestamp = System.currentTimeMillis(),
            remainingSecondsToDemandCheck = 300 // 5 минут (300 сек) до шанса продажи
        )
        _mySales.value = listOf(newSale) + _mySales.value

        // Если нормальная или сложная сложность: транслируем объявление реальным игрокам в LAN!
        if (currentDifficulty != GameDifficulty.EASY) {
            broadcastListingToLan(newSale)
        }

        return newSale
    }

    fun removePlayerListing(saleId: String) {
        _mySales.value = _mySales.value.filter { it.id != saleId }
    }

    // Движок жизненного цикла продаж деталей игрока
    // На ЛЁГКОЙ сложности: продажа каждые 5 минут (300 сек) ботом Авито
    // На НОРМАЛЬНОЙ и СЛОЖНОЙ: никакой периодической автопродажи ботами нет! Только реальные игроки в LAN (MSG_P2P_BUY) или сдача в утиль
    private fun startSalesLifecycleEngine() {
        scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000)

                val currentSales = _mySales.value
                val activeSales = currentSales.filter { !it.isSold }
                if (activeSales.isEmpty()) continue

                if (currentDifficulty == GameDifficulty.EASY) {
                    // ЛЁГКАЯ СЛОЖНОСТЬ: продажа каждые 5 минут (300 секунд)
                    val updatedSales = currentSales.map { sale ->
                        if (!sale.isSold) {
                            val newSec = (sale.remainingSecondsToDemandCheck - 1).coerceAtLeast(0)
                            if (newSec == 0) {
                                val buyerBot = botPeerNames.random()
                                sale.copy(isSold = true, buyerPeerName = buyerBot, remainingSecondsToDemandCheck = 0)
                            } else {
                                sale.copy(remainingSecondsToDemandCheck = newSec)
                            }
                        } else sale
                    }
                    _mySales.value = updatedSales
                } else {
                    // НОРМАЛЬНАЯ И СЛОЖНАЯ СЛОЖНОСТЬ:
                    // Продажи каждые 5 минут НЕТ! Автовыкуп ботами полностью отключен.
                    // Лоты продаются исключительно реальным пирам по локальной сети через broadcast / MSG_P2P_BUY.
                }
            }
        }
    }

    // P2P UDP Broadcast: обнаружение реальных людей в локальной сети и таблица лидеров
    private fun startLanPeerDiscovery() {
        scope.launch(Dispatchers.IO) {
            try {
                val socket = DatagramSocket(18888).apply {
                    broadcast = true
                    soTimeout = 4000
                }
                val buffer = ByteArray(1024)

                // Фоновый поток слушателя
                while (isActive) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        val message = String(packet.data, 0, packet.length).trim()

                        // Обработка пакетов от реальных людей в LAN
                        handleIncomingLanPacket(message, packet.address.hostAddress ?: "192.168.1.1")
                    } catch (_: Exception) {
                        // Сокет таймаут — продолжаем слушать
                    }

                    // Периодический анонс своего присутствия в сети (каждые ~5 секунд)
                    broadcastPeerAnnounce(socket)
                    delay(5000)
                }
            } catch (_: Exception) {
                // Если порт 18888 занят или нет прав на сокет, продолжаем работу через Mesh Swarm
            }
        }
    }

    private fun handleIncomingLanPacket(message: String, senderIp: String) {
        // "на лехкой сложности не должно быть ряльных людей"
        if (currentDifficulty == GameDifficulty.EASY) {
            return
        }

        if (message.startsWith("COLORBIT_LEADER_V2|")) {
            val parts = message.split("|")
            if (parts.size >= 7) {
                val peerId = parts[1]
                if (peerId == myPeerId) return // игнорируем свои же пакеты

                val name = parts[2]
                val hashrate = parts[3].toDoubleOrNull() ?: 0.0
                val balance = parts[4].toDoubleOrNull() ?: 0.0
                val rigs = parts[5].toIntOrNull() ?: 1
                val chapter = parts[6].toIntOrNull() ?: 1
                val diff = if (parts.size >= 8) parts[7] else "NORMAL"

                val entry = P2PLeaderboardEntry(
                    peerId = peerId,
                    playerName = name,
                    hashRateMh = hashrate,
                    balanceUsd = balance,
                    rigsCount = rigs,
                    chapter = chapter,
                    pingMs = 7, // Низкий LAN пинг
                    isLocalPlayer = false,
                    isLanPeer = true,
                    difficulty = diff,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
                discoveredLanPeers[peerId] = entry
                recalculateLeaderboard()
            }
        } else if (message.startsWith("COLORBIT_LAN_LISTING|")) {
            val parts = message.split("|")
            if (parts.size >= 8) {
                val listingId = parts[1]
                val sellerId = parts[2]
                if (sellerId == myPeerId) return

                val sellerName = parts[3]
                val compName = parts[4]
                val price = parts[5].toDoubleOrNull() ?: 50.0
                val durability = parts[6].toFloatOrNull() ?: 80f
                val compTypeStr = parts[7]

                val type = try {
                    ComponentType.valueOf(compTypeStr)
                } catch (_: Exception) {
                    ComponentType.GPU
                }

                val comp = PCComponent(
                    id = "lan_comp_$listingId",
                    name = compName,
                    type = type,
                    priceUsd = price,
                    isUsed = true,
                    baseDurability = durability,
                    description = "Реальный P2P лот от игрока $sellerName в локальной сети"
                )

                val listing = P2PListing(
                    id = listingId,
                    component = comp,
                    sellerPeerId = sellerId,
                    sellerName = "$sellerName [LAN]",
                    sellerRating = 5.0f,
                    successfulDeals = 40,
                    pingMs = 7,
                    conditionPercent = durability,
                    sellerClaim = "Реальный игрок из вашей сети Wi-Fi",
                    sellerComment = "Выставлено через P2P LAN!",
                    priceUsd = price,
                    isRealPeer = true
                )

                if (realLanListings.none { it.id == listingId }) {
                    realLanListings.add(0, listing)
                    _listings.value = realLanListings.toList()
                }
            }
        }
    }

    private fun broadcastPeerAnnounce(existingSocket: DatagramSocket? = null) {
        scope.launch(Dispatchers.IO) {
            try {
                val broadcastMsg = "COLORBIT_LEADER_V2|$myPeerId|$localPlayerName|$localHashRate|$localBalance|$localRigsCount|$localChapter|${currentDifficulty.id}"
                val sendData = broadcastMsg.toByteArray()
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                val sendPacket = DatagramPacket(sendData, sendData.size, broadcastAddress, 18888)

                if (existingSocket != null && !existingSocket.isClosed) {
                    existingSocket.send(sendPacket)
                } else {
                    DatagramSocket().use { sock ->
                        sock.broadcast = true
                        sock.send(sendPacket)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun broadcastListingToLan(sale: MyP2PSaleListing) {
        scope.launch(Dispatchers.IO) {
            try {
                val msg = "COLORBIT_LAN_LISTING|${sale.id}|$myPeerId|$localPlayerName|${sale.component.name}|${sale.askingPriceUsd}|${sale.durabilityPercent}|${sale.component.type.name}"
                val sendData = msg.toByteArray()
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                DatagramSocket().use { sock ->
                    sock.broadcast = true
                    sock.send(DatagramPacket(sendData, sendData.size, broadcastAddress, 18888))
                }
            } catch (_: Exception) {
            }
        }
    }
}
