package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ComponentCatalog
import com.example.model.ComponentType
import com.example.model.GameDifficulty
import com.example.model.InstalledRigComponent
import com.example.model.MiningRig
import com.example.model.MyP2PSaleListing
import com.example.model.P2PLeaderboardEntry
import com.example.model.P2PListing
import com.example.model.P2PSwarmState
import com.example.model.PCComponent
import com.example.model.VirtualLoan
import com.example.ui.theme.ColorbitBorder
import com.example.ui.theme.ColorbitCard
import com.example.ui.theme.DarkCyberBorder
import com.example.ui.theme.DarkCyberCard
import com.example.ui.theme.DarkCyberCardElevated
import com.example.ui.theme.HeatRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class ShopMarketTab(val title: String) {
    DHS("DHS (Новое)"),
    AVINTO("Авито P2P"),
    LOANS("СберБит Банк"),
    LEADERBOARD("Топ P2P")
}

enum class P2PSection(val title: String) {
    BUY("Купить у пиров"),
    SELL("Продать свои"),
    MY_SALES("Мои сделки")
}

@Composable
fun ShopScreen(
    balanceUsd: Double,
    activeDebtUsd: Double,
    rigs: List<MiningRig>,
    loans: List<VirtualLoan>,
    p2pSwarm: P2PSwarmState,
    p2pListings: List<P2PListing>,
    myP2PSales: List<MyP2PSaleListing>,
    difficulty: GameDifficulty,
    leaderboardEntries: List<P2PLeaderboardEntry>,
    seasonInfo: com.example.model.SeasonInfo = com.example.model.SeasonInfo(),
    onBuyComponent: (String, PCComponent) -> Unit,
    onTakeLoan: (String) -> Unit,
    onRepayLoan: (String) -> Unit,
    onScanP2P: () -> Unit,
    onBuyP2PListing: (String, P2PListing) -> Unit,
    onSellComponentToP2P: (String, String, Double) -> Unit,
    onClaimSoldPayment: (String) -> Unit,
    onInstantSellScrap: (String, String) -> Unit,
    onChangePlayerName: (String) -> Unit
) {
    var selectedMarket by remember { mutableStateOf(ShopMarketTab.AVINTO) }
    var selectedCategory by remember { mutableStateOf<ComponentType?>(ComponentType.GPU) }
    var componentToInstallDHS by remember { mutableStateOf<PCComponent?>(null) }
    var listingToInstallP2P by remember { mutableStateOf<P2PListing?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("shop_screen")
    ) {
        // Переключатель верхних рынков: DHS / Авито P2P / Банк Кредиты
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShopMarketTab.values().forEach { tab ->
                val isSelected = selectedMarket == tab
                val tabColor = when (tab) {
                    ShopMarketTab.DHS -> NeonGreen
                    ShopMarketTab.AVINTO -> NeonOrange
                    ShopMarketTab.LOANS -> NeonCyan
                    ShopMarketTab.LEADERBOARD -> Color(0xFFFFD700)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) tabColor.copy(alpha = 0.18f) else ColorbitCard)
                        .border(1.dp, if (isSelected) tabColor else ColorbitBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedMarket = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (tab == ShopMarketTab.AVINTO) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (p2pSwarm.isConnected) NeonGreen else HeatRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            tab.title,
                            color = if (isSelected) tabColor else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        when (selectedMarket) {
            ShopMarketTab.AVINTO -> {
                // ПОЛНОЦЕННЫЙ P2P МАРКЕТПЛЕЙС АВИТО
                P2PAvitoMarketplaceView(
                    balanceUsd = balanceUsd,
                    rigs = rigs,
                    p2pSwarm = p2pSwarm,
                    p2pListings = p2pListings,
                    myP2PSales = myP2PSales,
                    difficulty = difficulty,
                    selectedCategory = selectedCategory,
                    seasonInfo = seasonInfo,
                    onSelectCategory = { selectedCategory = it },
                    onScanP2P = onScanP2P,
                    onSelectToBuyP2P = { listingToInstallP2P = it },
                    onSellComponentToP2P = onSellComponentToP2P,
                    onClaimPayment = onClaimSoldPayment,
                    onInstantSellScrap = onInstantSellScrap
                )
            }
            ShopMarketTab.DHS -> {
                // ОФИЦИАЛЬНЫЙ МАГАЗИН DHS
                DhsRetailStoreView(
                    balanceUsd = balanceUsd,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    onSelectToBuy = { componentToInstallDHS = it }
                )
            }
            ShopMarketTab.LOANS -> {
                // КРЕДИТНЫЙ БАНК
                LoansMarketView(
                    balanceUsd = balanceUsd,
                    activeDebtUsd = activeDebtUsd,
                    loans = loans,
                    onTakeLoan = onTakeLoan,
                    onRepayLoan = onRepayLoan
                )
            }
            ShopMarketTab.LEADERBOARD -> {
                // P2P ТАБЛИЦА ЛИДЕРОВ СЕТИ
                P2PLeaderboardScreen(
                    leaderboardEntries = leaderboardEntries,
                    swarmState = p2pSwarm,
                    difficulty = difficulty,
                    seasonInfo = seasonInfo,
                    onScanNetwork = onScanP2P,
                    onChangePlayerName = onChangePlayerName
                )
            }
        }
    }

    // Диалог покупки нового компонента DHS
    if (componentToInstallDHS != null) {
        val comp = componentToInstallDHS!!
        RigSelectDialog(
            title = "Установить в какой риг?",
            compName = comp.name,
            compSubtitle = "DHS Retail • 100% ресурс",
            rigs = rigs,
            onRigSelected = { rigId ->
                onBuyComponent(rigId, comp)
                componentToInstallDHS = null
            },
            onDismiss = { componentToInstallDHS = null }
        )
    }

    // Диалог покупки P2P лота через Escrow (игра-угадайка: состояние скрыто!)
    if (listingToInstallP2P != null) {
        val listing = listingToInstallP2P!!
        RigSelectDialog(
            title = "P2P Escrow сделка: Куда установить?",
            compName = listing.component.name,
            compSubtitle = "Пир: ${listing.sellerName} • $${listing.priceUsd.toInt()} USD • «${listing.sellerClaim}»",
            rigs = rigs,
            onRigSelected = { rigId ->
                onBuyP2PListing(rigId, listing)
                listingToInstallP2P = null
            },
            onDismiss = { listingToInstallP2P = null }
        )
    }
}

// === P2P АВИТО МАРКЕТПЛЕЙС КОМПОНЕНТ ===

@Composable
private fun P2PAvitoMarketplaceView(
    balanceUsd: Double,
    rigs: List<MiningRig>,
    p2pSwarm: P2PSwarmState,
    p2pListings: List<P2PListing>,
    myP2PSales: List<MyP2PSaleListing>,
    difficulty: GameDifficulty,
    selectedCategory: ComponentType?,
    seasonInfo: com.example.model.SeasonInfo,
    onSelectCategory: (ComponentType?) -> Unit,
    onScanP2P: () -> Unit,
    onSelectToBuyP2P: (P2PListing) -> Unit,
    onSellComponentToP2P: (String, String, Double) -> Unit,
    onClaimPayment: (String) -> Unit,
    onInstantSellScrap: (String, String) -> Unit
) {
    var p2pSection by remember { mutableStateOf(P2PSection.BUY) }
    var sellPublishComponent by remember { mutableStateOf<Pair<String, InstalledRigComponent>?>(null) }

    val activeListings = remember(p2pListings, selectedCategory) {
        val unsold = p2pListings.filter { !it.isSold }
        if (selectedCategory == null) unsold else unsold.filter { it.component.type == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // P2P Swarm Hub Bar (Индикатор пиров, задержки, локальной сети)
        P2PSwarmStatusHeader(
            swarm = p2pSwarm,
            onScan = onScanP2P
        )

        // Баннер 3-месячного сезонного вайпа Авито
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "СЕЗОН ${seasonInfo.seasonNumber} • ВАЙП АВИТО КАЖДЫЕ 3 МЕСЯЦА",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Все активные лоты, сделки и аккаунты сбрасываются раз в 3 месяца (как в оригинале)",
                            color = TextMuted,
                            fontSize = 9.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonPurple.copy(alpha = 0.25f))
                        .border(1.dp, NeonPurple, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("До вайпа", color = TextSecondary, fontSize = 8.5.sp)
                        Text(seasonInfo.formattedRemaining, color = NeonCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Вкладки P2P Авито: Купить / Продать свои / Мои сделки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            P2PSection.values().forEach { sec ->
                val isSelected = p2pSection == sec
                val badgeCount = when (sec) {
                    P2PSection.BUY -> activeListings.size
                    P2PSection.SELL -> rigs.sumOf { it.installedComponents.size }
                    P2PSection.MY_SALES -> myP2PSales.size
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonOrange else DarkCyberCardElevated)
                        .clickable { p2pSection = sec }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            sec.title,
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (badgeCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color.Black.copy(alpha = 0.2f) else DarkCyberBorder)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    "$badgeCount",
                                    color = if (isSelected) Color.Black else NeonOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        when (p2pSection) {
            P2PSection.BUY -> {
                // Категории комплектующих
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryChip(
                            title = "Все (${p2pListings.count { !it.isSold }})",
                            isSelected = selectedCategory == null,
                            onClick = { onSelectCategory(null) }
                        )
                    }
                    items(ComponentType.values()) { type ->
                        val count = p2pListings.count { !it.isSold && it.component.type == type }
                        CategoryChip(
                            title = "${type.title} ($count)",
                            isSelected = selectedCategory == type,
                            onClick = { onSelectCategory(type) }
                        )
                    }
                }

                // Лента объявлений от P2P пиров
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (activeListings.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.NetworkCheck,
                                        contentDescription = null,
                                        tint = if (difficulty == GameDifficulty.EASY) NeonOrange else NeonCyan,
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        if (difficulty == GameDifficulty.EASY) {
                                            "В данной категории нет активных P2P лотов"
                                        } else {
                                            "Боты на Авито отключены (${difficulty.title})"
                                        },
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        if (difficulty == GameDifficulty.EASY) {
                                            "Нажмите 'Поиск пиров' выше, чтобы найти предложения от новых майнеров!"
                                        } else {
                                            "На ${difficulty.title.lowercase()} сложности генерация ботов отключена. Лоты поступают только от реальных игроков по локальной сети (LAN/Wi-Fi). Вы можете выставить свои комплектующие во вкладке 'Продать свои'!"
                                        },
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    items(activeListings, key = { it.id }) { listing ->
                        P2PListingCard(
                            listing = listing,
                            canAfford = balanceUsd >= listing.priceUsd,
                            onBuyEscrowClick = { onSelectToBuyP2P(listing) }
                        )
                    }
                }
            }

            P2PSection.SELL -> {
                // Раздел продажи своих компонентов пирам
                P2PSellMyComponentsView(
                    rigs = rigs,
                    onOpenPublishDialog = { rigId, comp ->
                        sellPublishComponent = Pair(rigId, comp)
                    },
                    onInstantSellScrap = onInstantSellScrap
                )
            }

            P2PSection.MY_SALES -> {
                // Раздел моих активных продаж в P2P сети
                P2PMySalesView(
                    mySales = myP2PSales,
                    difficulty = difficulty,
                    onClaimPayment = onClaimPayment
                )
            }
        }
    }

    // Диалог выставления своего компонента на P2P Авито
    if (sellPublishComponent != null) {
        val (rigId, installedComp) = sellPublishComponent!!
        PublishP2PSaleDialog(
            rigId = rigId,
            installedComp = installedComp,
            onPublish = { price ->
                onSellComponentToP2P(rigId, installedComp.slotId, price)
                sellPublishComponent = null
            },
            onDismiss = { sellPublishComponent = null }
        )
    }
}

// === СТАТУС-БАР P2P СЕТИ ===

@Composable
private fun P2PSwarmStatusHeader(
    swarm: P2PSwarmState,
    onScan: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = ColorbitCard),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "P2P СЕТЬ АВИТО АКТИВНА",
                        color = NeonOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Кнопка сканирования P2P сети
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF212121))
                        .border(1.dp, ColorbitBorder, RoundedCornerShape(6.dp))
                        .clickable(enabled = !swarm.isScanning, onClick = onScan)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Сканировать сеть",
                            tint = NeonOrange,
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(if (swarm.isScanning) angle else 0f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (swarm.isScanning) "Поиск пиров..." else "Поиск пиров",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Метрики P2P узла
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Ваш узел", color = TextMuted, fontSize = 10.sp)
                    Text(swarm.myPeerId, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Пиров в DHT", color = TextMuted, fontSize = 10.sp)
                    Text("${swarm.peersCount} онлайн", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("P2P Пинг", color = TextMuted, fontSize = 10.sp)
                    Text("${swarm.averagePingMs} ms", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Режим", color = TextMuted, fontSize = 10.sp)
                    Text("Direct Escrow", color = NeonOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wifi, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Подсеть: ${swarm.localSubnetIp} • P2P Mesh • Офлайн-совместимо без центрального сервера",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// === КАРТОЧКА P2P ОБЪЯВЛЕНИЯ ===

@Composable
private fun P2PListingCard(
    listing: P2PListing,
    canAfford: Boolean,
    onBuyEscrowClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ColorbitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Шапка пира-продавца
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NeonOrange.copy(alpha = 0.2f))
                            .border(1.dp, NeonOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            listing.sellerName.take(2).uppercase().replace("@", ""),
                            color = NeonOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                listing.sellerName,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DarkCyberCardElevated)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    listing.sellerPeerId,
                                    color = NeonCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (listing.isRealPeer) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("⚡ LAN Пир", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(11.dp))
                            Text(" ${listing.sellerRating} (${listing.successfulDeals} сд.)", color = TextSecondary, fontSize = 10.sp)
                            Text(" • ", color = TextMuted, fontSize = 10.sp)
                            Text("${listing.pingMs} ms", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Бейдж P2P Escrow
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("P2P Escrow", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Название комплектующего
            Text(
                listing.component.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Характеристики и слова продавца (без раскрытия реального процента износа!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (listing.component.hashRateMh > 0) {
                    SpecPill(
                        icon = Icons.Default.Speed,
                        text = "+${listing.component.hashRateMh} MH/s",
                        color = NeonGreen
                    )
                }
                if (listing.component.powerWatts > 0) {
                    SpecPill(
                        icon = Icons.Default.ElectricBolt,
                        text = "${listing.component.powerWatts}W",
                        color = NeonOrange
                    )
                }
                SpecPill(
                    icon = Icons.Default.CheckCircle,
                    text = listing.sellerClaim,
                    color = NeonCyan
                )
            }

            // Комментарий продавца
            if (listing.sellerComment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCyberCardElevated)
                        .padding(8.dp)
                ) {
                    Text(
                        "\"${listing.sellerComment}\"",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Игра-угадайка: реальный износ неизвестен до покупки!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkCyberCardElevated.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Авито: реальный износ скрыт — игра-угадайка! Узнаете после покупки.",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Честная фиксированная цена без скидок и кнопка покупки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "$${listing.priceUsd.toInt()} USD",
                        color = NeonOrange,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Кнопка P2P Escrow покупки
                Button(
                    onClick = onBuyEscrowClick,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        disabledContainerColor = DarkCyberCardElevated
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Handshake, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        if (canAfford) "Купить лот" else "Мало USD",
                        color = if (canAfford) Color.Black else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// === РАЗДЕЛ ПРОДАЖИ СВОИХ КОМПЛЕКТУЮЩИХ ПИРАМ ===

@Composable
private fun P2PSellMyComponentsView(
    rigs: List<MiningRig>,
    onOpenPublishDialog: (String, InstalledRigComponent) -> Unit,
    onInstantSellScrap: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sell, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Продажа своих деталей в P2P сеть",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Снимите любую деталь с рига и выставьте на P2P Авито по своей цене или моментально сдайте P2P скупщику за наличные!",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        val allInstalled = rigs.flatMap { rig ->
            rig.installedComponents.map { comp -> Triple(rig.id, rig.name, comp) }
        }

        if (allInstalled.isEmpty()) {
            item {
                Text(
                    "У вас нет установленных деталей для продажи.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        items(allInstalled) { (rigId, rigName, comp) ->
            val estMarketPrice = (comp.component.priceUsd * (comp.durabilityPercent / 100.0) * 0.75).coerceAtLeast(10.0)
            val scrapPrice = (comp.component.priceUsd * (comp.durabilityPercent / 100.0) * 0.55).coerceAtLeast(8.0)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCyberCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCyberBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(comp.component.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Установлено в: $rigName", color = NeonCyan, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkCyberCard)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Ресурс: ${comp.durabilityPercent.toInt()}%", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Рыночная цена P2P", color = TextMuted, fontSize = 10.sp)
                            Text("~$${estMarketPrice.toInt()} USD", color = NeonOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Мгновенный скуп
                            OutlinedButton(
                                onClick = { onInstantSellScrap(rigId, comp.slotId) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Скуп ($${scrapPrice.toInt()})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Выставить на P2P
                            Button(
                                onClick = { onOpenPublishDialog(rigId, comp) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Sell, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Выставить", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// === РАЗДЕЛ МОИХ СДЕЛОК В P2P СЕТИ ===

@Composable
private fun P2PMySalesView(
    mySales: List<MyP2PSaleListing>,
    difficulty: GameDifficulty,
    onClaimPayment: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (mySales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("У вас нет активных объявлений на P2P Авито", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "Перейдите во вкладку 'Продать свои' и выставьте ненужные комплектующие на продажу майнерам.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(mySales, key = { it.id }) { sale ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (sale.isSold) NeonGreen.copy(alpha = 0.12f) else DarkCyberCardElevated
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (sale.isSold) NeonGreen else NeonOrange.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sale.component.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$${sale.askingPriceUsd.toInt()} USD", color = NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (sale.isSold) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ЛОТ ВЫКУПЛЕН!", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text("Покупатель: ${sale.buyerPeerName ?: "Пир в P2P сети"}", color = TextSecondary, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onClaimPayment(sale.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Paid, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Забрать $${sale.askingPriceUsd.toInt()}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        if (difficulty == GameDifficulty.EASY) {
                            val mm = sale.remainingSecondsToDemandCheck / 60
                            val ss = sale.remainingSecondsToDemandCheck % 60
                            val progress = (300 - sale.remainingSecondsToDemandCheck) / 300f

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = NeonGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Выкуп ботом Авито через: %02d:%02d".format(mm, ss),
                                            color = NeonGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        "Каждые 5 мин",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = NeonGreen,
                                    trackColor = DarkCyberBorder
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    "Сложность: Лёгкая (автопродажа ботами каждые 5 минут)",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        } else {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Ожидание покупателя в P2P сети LAN (Wi-Fi)",
                                        color = NeonCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Сложность: ${difficulty.title}. Без автопродажи ботами — покупка только реальными игроками или сдача на радиорынок.",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// === ДИАЛОГ ВЫСТАВЛЕНИЯ СВОЕГО ТОВАРА НА P2P АВИТО ===

@Composable
private fun PublishP2PSaleDialog(
    rigId: String,
    installedComp: InstalledRigComponent,
    onPublish: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val comp = installedComp.component
    val recPrice = (comp.priceUsd * (installedComp.durabilityPercent / 100.0) * 0.75).coerceAtLeast(10.0)
    var askingPrice by remember { mutableStateOf(recPrice.toInt().toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Выставить на Авито P2P", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(comp.name, color = NeonOrange, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Износ: ${(100 - installedComp.durabilityPercent).toInt()}% • Ресурс: ${installedComp.durabilityPercent.toInt()}%", color = TextSecondary, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Укажите вашу цену продажи:", color = TextMuted, fontSize = 12.sp)
                Text("$${askingPrice.toInt()} USD", color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Black)

                val minRange = (comp.priceUsd.toFloat() * 0.2f).coerceAtLeast(5f)
                val maxRange = (comp.priceUsd.toFloat() * 1.1f).coerceAtLeast(minRange + 10f)

                Slider(
                    value = askingPrice.coerceIn(minRange, maxRange),
                    onValueChange = { askingPrice = it },
                    valueRange = minRange..maxRange,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonGreen,
                        activeTrackColor = NeonGreen,
                        inactiveTrackColor = DarkCyberBorder
                    )
                )

                Text(
                    "Рекомендованная цена для быстрой P2P сделки: ~$${recPrice.toInt()} USD",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = { onPublish(askingPrice.toDouble()) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Выставить", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// === ОФИЦИАЛЬНЫЙ DHS РИТЕЙЛ МАГАЗИН ===

@Composable
private fun DhsRetailStoreView(
    balanceUsd: Double,
    selectedCategory: ComponentType?,
    onSelectCategory: (ComponentType?) -> Unit,
    onSelectToBuy: (PCComponent) -> Unit
) {
    val catalog = ComponentCatalog.allComponents
    val filtered = remember(selectedCategory) {
        if (selectedCategory == null) catalog else catalog.filter { it.type == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Баннер DHS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NeonGreen.copy(alpha = 0.12f))
                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "DHS: Официальный ритейлер новых комплектующих. 100% ресурс и гарантия производителя.",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip(
                    title = "Все",
                    isSelected = selectedCategory == null,
                    onClick = { onSelectCategory(null) }
                )
            }
            items(ComponentType.values()) { type ->
                CategoryChip(
                    title = type.title,
                    isSelected = selectedCategory == type,
                    onClick = { onSelectCategory(type) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { comp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCyberBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(comp.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(comp.description, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (comp.hashRateMh > 0) {
                                SpecPill(Icons.Default.Speed, "+${comp.hashRateMh} MH/s", NeonGreen)
                            }
                            if (comp.powerWatts > 0) {
                                SpecPill(Icons.Default.ElectricBolt, "${comp.powerWatts}W", NeonOrange)
                            }
                            SpecPill(Icons.Default.Shield, "100% Ресурс", NeonCyan)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$${comp.priceUsd.toInt()} USD", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)

                            Button(
                                onClick = { onSelectToBuy(comp) },
                                enabled = balanceUsd >= comp.priceUsd,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    if (balanceUsd >= comp.priceUsd) "Купить" else "Мало USD",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// === РАЗДЕЛ БАНКА И КРЕДИТОВ ===

@Composable
private fun LoansMarketView(
    balanceUsd: Double,
    activeDebtUsd: Double,
    loans: List<VirtualLoan>,
    onTakeLoan: (String) -> Unit,
    onRepayLoan: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeDebtUsd > 0.0) HeatRed.copy(alpha = 0.15f) else DarkCyberCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeDebtUsd > 0.0) HeatRed else DarkCyberBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ОБЩИЙ КРЕДИТНЫЙ ДОЛГ", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "$${activeDebtUsd.toInt()}",
                            color = if (activeDebtUsd > 0.0) HeatRed else NeonGreen,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (activeDebtUsd > 0.0) HeatRed else NeonCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        items(loans, key = { it.id }) { loan ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCyberCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (loan.isActive) NeonCyan else DarkCyberBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(loan.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkCyberCard, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Сумма займа", color = TextMuted, fontSize = 10.sp)
                            Text("+$${loan.amountUsd.toInt()}", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Ставка", color = TextMuted, fontSize = 10.sp)
                            Text("${loan.interestRatePercent}%", color = NeonOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("К возврату", color = TextMuted, fontSize = 10.sp)
                            Text("$${loan.totalToRepayUsd.toInt()}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (loan.isActive) {
                        Button(
                            onClick = { onRepayLoan(loan.id) },
                            enabled = balanceUsd >= loan.remainingDebtUsd,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (balanceUsd >= loan.remainingDebtUsd) "Погасить долг ($${loan.remainingDebtUsd.toInt()})"
                                else "Не хватает средств для погашения",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = { onTakeLoan(loan.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Оформить займ +$${loan.amountUsd.toInt()}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// === ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ ===

@Composable
private fun RigSelectDialog(
    title: String,
    compName: String,
    compSubtitle: String,
    rigs: List<MiningRig>,
    onRigSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(compName, color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(compSubtitle, color = TextSecondary, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(16.dp))

                rigs.forEach { rig ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onRigSelected(rig.id) },
                        colors = CardDefaults.cardColors(containerColor = DarkCyberCardElevated),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rig.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Деталей: ${rig.installedComponents.size} | %.1f MH/s".format(rig.totalHashRateMh), color = TextSecondary, fontSize = 11.sp)
                            }
                            Text("Выбрать", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Отмена")
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) NeonGreen else DarkCyberCardElevated)
            .border(1.dp, if (isSelected) NeonGreen else DarkCyberBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            title,
            color = if (isSelected) Color.Black else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SpecPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
