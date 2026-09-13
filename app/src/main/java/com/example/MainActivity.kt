package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ColorbitViewModel
import com.example.ui.GameTab
import com.example.ui.screens.ExchangeScreen
import com.example.ui.screens.FacilitiesScreen
import com.example.ui.screens.OnlineWebViewScreen
import com.example.ui.screens.QuestsScreen
import com.example.ui.screens.RigsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.StoryDialogueDialog
import com.example.ui.theme.ColorbitBorder
import com.example.ui.theme.ColorbitCard
import com.example.ui.theme.DarkCyberBorder
import com.example.ui.theme.DarkCyberCard
import com.example.ui.theme.DarkCyberCardElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ColorbitApp()
            }
        }
    }
}

@Composable
fun ColorbitApp(vm: ColorbitViewModel = viewModel()) {
    var isOnlineMode by remember { mutableStateOf(false) }

    val selectedTab by vm.selectedTab.collectAsState()
    val stats by vm.playerStats.collectAsState()
    val rigs by vm.rigs.collectAsState()
    val cryptos by vm.cryptos.collectAsState()
    val facilities by vm.facilities.collectAsState()
    val quests by vm.quests.collectAsState()
    val storyChapters by vm.storyChapters.collectAsState()
    val activeStoryDialogue by vm.activeStoryDialogue.collectAsState()
    val loans by vm.loans.collectAsState()
    val p2pSwarm by vm.p2pSwarmState.collectAsState()
    val p2pListings by vm.p2pListings.collectAsState()
    val myP2PSales by vm.myP2PSales.collectAsState()
    val leaderboardEntries by vm.leaderboardEntries.collectAsState()
    val notification by vm.statusNotification.collectAsState()
    val offlineEarnings by vm.offlineDialogInfo.collectAsState()
    val needsInitialDifficultySelection by vm.needsInitialDifficultySelection.collectAsState()
    val seasonInfo by vm.seasonInfo.collectAsState()
    val showSeasonalWipeDialog by vm.showSeasonalWipeDialog.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var selectedResetDifficulty by remember { mutableStateOf(stats.difficulty) }
    var editPlayerName by remember(stats.playerName) { mutableStateOf(stats.playerName) }

    if (isOnlineMode) {
        OnlineWebViewScreen(
            onSwitchToOffline = { isOnlineMode = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Верхняя панель: Название + баланс наличных в USD + переключатель режима (Colorbit web header)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "COLOR",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "BIT",
                                color = NeonGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonGreen.copy(alpha = 0.18f))
                                    .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                    .clickable { isOnlineMode = true }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("ОФЛАЙН", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            val diffColor = when (stats.difficulty) {
                                com.example.model.GameDifficulty.EASY -> NeonGreen
                                com.example.model.GameDifficulty.NORMAL -> NeonOrange
                                com.example.model.GameDifficulty.HARD -> Color(0xFFFF5252)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(diffColor.copy(alpha = 0.15f))
                                    .border(1.dp, diffColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .clickable { showSettingsDialog = true }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = diffColor,
                                        modifier = Modifier.size(9.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        stats.difficulty.title.uppercase(),
                                        color = diffColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Кнопка перехода в онлайн colorbit.ru
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2B2B2B))
                                    .border(1.dp, ColorbitBorder, RoundedCornerShape(6.dp))
                                    .clickable { isOnlineMode = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = "Онлайн",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Онлайн", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Баланс USD
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "$%.2f".format(stats.balanceUsd),
                                        color = if (stats.balanceUsd >= 0) NeonGreen else Color(0xFFFF5252),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Кнопка настроек и профиля
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2B2B2B))
                                    .border(1.dp, ColorbitBorder, RoundedCornerShape(6.dp))
                                    .clickable { showSettingsDialog = true }
                                    .padding(5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Настройки и профиль",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Плашка сезона и 3-месячного вайпа
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF242028))
                            .border(1.dp, Color(0xFF8000D7).copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                            .clickable { showSettingsDialog = true }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("СЕЗОН ${seasonInfo.seasonNumber}", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("•", color = TextMuted, fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Вайп аккаунтов и Авито через:", color = TextSecondary, fontSize = 9.5.sp)
                        }
                        Text(seasonInfo.formattedRemaining, color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            bottomBar = {
                // Нижняя навигация
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = Color(0xFF1E1E1E),
                    tonalElevation = 2.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == GameTab.RIGS,
                        onClick = { vm.selectTab(GameTab.RIGS) },
                        icon = { Icon(Icons.Default.Memory, contentDescription = "Фермы") },
                        label = { Text("Риги") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = NeonGreen,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonGreen,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_rigs")
                    )
                    NavigationBarItem(
                        selected = selectedTab == GameTab.SHOP,
                        onClick = { vm.selectTab(GameTab.SHOP) },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Магазин") },
                        label = { Text("Магазин") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = NeonGreen,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonGreen,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_shop")
                    )
                    NavigationBarItem(
                        selected = selectedTab == GameTab.EXCHANGE,
                        onClick = { vm.selectTab(GameTab.EXCHANGE) },
                        icon = { Icon(Icons.Default.CurrencyExchange, contentDescription = "Биржа") },
                        label = { Text("Биржа") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = NeonGreen,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonGreen,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_exchange")
                    )
                    NavigationBarItem(
                        selected = selectedTab == GameTab.FACILITIES,
                        onClick = { vm.selectTab(GameTab.FACILITIES) },
                        icon = { Icon(Icons.Default.LocationCity, contentDescription = "Локации") },
                        label = { Text("ДомКлик") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = NeonGreen,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonGreen,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_facilities")
                    )
                    NavigationBarItem(
                        selected = selectedTab == GameTab.QUESTS,
                        onClick = { vm.selectTab(GameTab.QUESTS) },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Сюжет") },
                        label = { Text("Сюжет") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = NeonGreen,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonGreen,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_quests")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    GameTab.RIGS -> RigsScreen(
                        rigs = rigs,
                        cryptos = cryptos,
                        onTogglePower = { vm.toggleRigPower(it) },
                        onSwitchCoin = { rigId, coinId -> vm.switchRigCoin(rigId, coinId) },
                        onCleanDust = { vm.cleanDust(it) },
                        onReplacePaste = { vm.replaceThermalPaste(it) },
                        onAddNewRig = { vm.addNewRig(it) },
                        onApplyOverclock = { rigId, core, mem, pl, fan ->
                            vm.applyOverclock(rigId, core, mem, pl, fan)
                        }
                    )
                    GameTab.SHOP -> ShopScreen(
                        balanceUsd = stats.balanceUsd,
                        activeDebtUsd = stats.activeDebtUsd,
                        rigs = rigs,
                        loans = loans,
                        p2pSwarm = p2pSwarm,
                        p2pListings = p2pListings,
                        myP2PSales = myP2PSales,
                        difficulty = stats.difficulty,
                        leaderboardEntries = leaderboardEntries,
                        seasonInfo = seasonInfo,
                        onBuyComponent = { rigId, comp -> vm.buyAndInstallComponent(rigId, comp) },
                        onTakeLoan = { vm.takeLoan(it) },
                        onRepayLoan = { vm.repayLoan(it) },
                        onScanP2P = { vm.scanP2PNetwork() },
                        onBuyP2PListing = { rigId, listing -> vm.buyP2PListing(rigId, listing) },
                        onSellComponentToP2P = { rigId, slotId, price -> vm.sellComponentToP2P(rigId, slotId, price) },
                        onClaimSoldPayment = { saleId -> vm.claimSoldP2PPayment(saleId) },
                        onInstantSellScrap = { rigId, slotId -> vm.instantSellToScrapPeer(rigId, slotId) },
                        onChangePlayerName = { vm.setPlayerName(it) }
                    )
                    GameTab.EXCHANGE -> ExchangeScreen(
                        cryptos = cryptos,
                        balances = stats.cryptoBalances,
                        onSellCrypto = { coinId, amt -> vm.sellCrypto(coinId, amt) }
                    )
                    GameTab.FACILITIES -> FacilitiesScreen(
                        facilities = facilities,
                        balanceUsd = stats.balanceUsd,
                        currentRigsCount = rigs.size,
                        onRentFacility = { id, period -> vm.rentFacility(id, period) },
                        onSetActiveFacility = { id -> vm.setActiveFacility(id) },
                        onUnlockFacility = { vm.unlockFacility(it) }
                    )
                    GameTab.QUESTS -> QuestsScreen(
                        quests = quests,
                        stats = stats,
                        storyChapters = storyChapters,
                        onClaimReward = { vm.claimQuestReward(it) },
                        onOpenStoryDialogue = { vm.showStoryDialogue(it) },
                        onClaimChapterReward = { vm.completeChapter(it) }
                    )
                }

                // Всплывающее уведомление о действиях
                AnimatedVisibility(
                    visible = notification != null,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(notification ?: "", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Диалог сюжетного диалога (катсцена)
        if (activeStoryDialogue != null) {
            StoryDialogueDialog(
                chapter = activeStoryDialogue!!,
                onDismiss = { vm.dismissStoryDialogue() }
            )
        }

        // Диалог офлайн-дохода при возвращении в игру
        if (offlineEarnings != null) {
            val earnings = offlineEarnings!!
            val hours = earnings.offlineSeconds / 3600
            val mins = (earnings.offlineSeconds % 3600) / 60

            Dialog(onDismissRequest = { vm.dismissOfflineDialog() }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Добро пожаловать обратно!",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Фермы работали без вас: ${hours}ч ${mins}мин",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        earnings.minedAmounts.forEach { (coinId, amount) ->
                            if (amount > 0.000001) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkCyberCardElevated, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(coinId, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("+%.6f".format(amount), color = NeonGreen, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Счёт за свет:", color = TextSecondary, fontSize = 12.sp)
                            Text("-$%.2f USD".format(earnings.electricityCostUsd), color = NeonOrange, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { vm.dismissOfflineDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Забрать доход", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1. Диалог выбора сложности при ПЕРВОМ ЗАХОДЕ (нельзя пропустить)
        if (needsInitialDifficultySelection) {
            var selectedStartDiff by remember { mutableStateOf(com.example.model.GameDifficulty.NORMAL) }

            Dialog(
                onDismissRequest = { /* Нельзя закрыть без выбора сложности */ },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "ВЫБОР СЛОЖНОСТИ",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Добро пожаловать в Colorbit! Выберите режим сложности для начала карьеры майнера:",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        com.example.model.GameDifficulty.values().forEach { diff ->
                            val isSelected = selectedStartDiff == diff
                            val color = when (diff) {
                                com.example.model.GameDifficulty.EASY -> NeonGreen
                                com.example.model.GameDifficulty.NORMAL -> NeonOrange
                                com.example.model.GameDifficulty.HARD -> Color(0xFFFF5252)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.16f) else DarkCyberCardElevated)
                                    .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) color else DarkCyberBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedStartDiff = diff }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            diff.title,
                                            color = color,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(color)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("ВЫБРАНО", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        when (diff) {
                                            com.example.model.GameDifficulty.EASY -> "Боты на Авито включены, выкуп лотов каждые 5 минут. Сетевые игроки скрыты (синтетический режим)."
                                            com.example.model.GameDifficulty.NORMAL -> "Без ботов и автопродажи. Честная P2P-сеть с реальными майнерами по Wi-Fi / LAN или скупка на радиорынке."
                                            com.example.model.GameDifficulty.HARD -> "Хардкор. Без ботов и автопродажи. Полная зависимость от реальных P2P майнеров."
                                        },
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(14.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Сложность фиксируется на всю игру. Сменить её на лету нельзя, только при сбросе прогресса.",
                                color = TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { vm.selectInitialDifficulty(selectedStartDiff) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Начать игру (${selectedStartDiff.title})", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Диалог Настроек и Профиля
        if (showSettingsDialog) {
            Dialog(onDismissRequest = { showSettingsDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCyberBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Настройки & Профиль",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Блок текущей зафиксированной сложности
                        val curDiffColor = when (stats.difficulty) {
                            com.example.model.GameDifficulty.EASY -> NeonGreen
                            com.example.model.GameDifficulty.NORMAL -> NeonOrange
                            com.example.model.GameDifficulty.HARD -> Color(0xFFFF5252)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCyberCardElevated)
                                .border(1.dp, curDiffColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Текущая сложность:", color = TextSecondary, fontSize = 11.sp)
                                    Text(stats.difficulty.title.uppercase(), color = curDiffColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = curDiffColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        "Смена сложности на лету отключена (защита от накрутки). Изменить сложность можно только при полном сбросе прогресса.",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Блок изменения ника
                        Text("Имя майнера в P2P сети:", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = editPlayerName,
                                onValueChange = { editPlayerName = it.take(20) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkCyberCardElevated,
                                    unfocusedContainerColor = DarkCyberCardElevated,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedIndicatorColor = NeonCyan,
                                    unfocusedIndicatorColor = DarkCyberBorder
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { vm.setPlayerName(editPlayerName) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ОК", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Блок 3-месячного сезонного вайпа
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCyberCardElevated)
                                .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("СЕЗОН ${seasonInfo.seasonNumber}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                    Text(seasonInfo.formattedRemaining, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Каждые 3 месяца (90 дней) происходит полный сброс всех аккаунтов, ферм, балансов и рынка Авито (как в оригинале), чтобы игра оставалась динамичной.",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Кнопка сброса игры
                        Button(
                            onClick = {
                                selectedResetDifficulty = stats.difficulty
                                showResetConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381216)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Сбросить прогресс и начать заново", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showSettingsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCyberCardElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Закрыть", color = TextPrimary)
                        }
                    }
                }
            }
        }

        // 3. Диалог подтверждения СБРОСА и ВЫБОРА сложности новой игры
        if (showResetConfirmDialog) {
            Dialog(onDismissRequest = { showResetConfirmDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF5252))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "СБРОС ПРОГРЕССА",
                                color = Color(0xFFFF5252),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Внимание! Весь текущий баланс USD, криптовалюта, фермы, недвижимость и сюжетные задания будут удалены. Текущий Сезон ${seasonInfo.seasonNumber} и таймер до глобального вайпа сохраняются без изменений.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Выберите сложность для новой игры:",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        com.example.model.GameDifficulty.values().forEach { diff ->
                            val isSelected = selectedResetDifficulty == diff
                            val color = when (diff) {
                                com.example.model.GameDifficulty.EASY -> NeonGreen
                                com.example.model.GameDifficulty.NORMAL -> NeonOrange
                                com.example.model.GameDifficulty.HARD -> Color(0xFFFF5252)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.16f) else DarkCyberCardElevated)
                                    .border(1.dp, if (isSelected) color else DarkCyberBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedResetDifficulty = diff }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(diff.title, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            when (diff) {
                                                com.example.model.GameDifficulty.EASY -> "Боты на Авито включены, выкуп лота каждые 5 мин."
                                                com.example.model.GameDifficulty.NORMAL -> "Без ботов и автопродажи. P2P сеть с реальными игроками."
                                                com.example.model.GameDifficulty.HARD -> "Хардкор. Без ботов. Только реальные майнеры."
                                            },
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                vm.resetGameProgress(selectedResetDifficulty)
                                showResetConfirmDialog = false
                                showSettingsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Сбросить всё и начать новую игру", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showResetConfirmDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCyberCardElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Отмена", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 4. Диалог автоматического СЕЗОННОГО ВАЙПА (каждые 3 месяца)
        if (showSeasonalWipeDialog) {
            var selectedSeasonWipeDiff by remember { mutableStateOf(stats.difficulty) }
            Dialog(
                onDismissRequest = { /* Нельзя закрыть без выбора сложности для нового сезона */ },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "3-МЕСЯЧНЫЙ ВАЙП СЕЗОНА",
                                color = Color(0xFFFFD700),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Прошло 3 месяца! Как в оригинале Colorbit, наступил плановый сезонный вайп. Все аккаунты, фермы, криптовалюты, балансы и весь рынок Авито полностью обнулены.",
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Добро пожаловать в СЕЗОН ${seasonInfo.seasonNumber + 1}! Выберите уровень сложности для старта:",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        com.example.model.GameDifficulty.values().forEach { diff ->
                            val isSelected = selectedSeasonWipeDiff == diff
                            val color = when (diff) {
                                com.example.model.GameDifficulty.EASY -> NeonGreen
                                com.example.model.GameDifficulty.NORMAL -> NeonOrange
                                com.example.model.GameDifficulty.HARD -> Color(0xFFFF5252)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.16f) else DarkCyberCardElevated)
                                    .border(1.dp, if (isSelected) color else DarkCyberBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedSeasonWipeDiff = diff }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(diff.title, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            when (diff) {
                                                com.example.model.GameDifficulty.EASY -> "Боты на Авито включены, выкуп лота каждые 5 мин."
                                                com.example.model.GameDifficulty.NORMAL -> "Без ботов и автопродажи. Честная P2P-сеть с реальными игроками."
                                                com.example.model.GameDifficulty.HARD -> "Хардкор. Без ботов. Только реальные майнеры."
                                            },
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                vm.performSeasonalWipe(selectedSeasonWipeDiff)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Начать Сезон ${seasonInfo.seasonNumber + 1} (${selectedSeasonWipeDiff.title})",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
