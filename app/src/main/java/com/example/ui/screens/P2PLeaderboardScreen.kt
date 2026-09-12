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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameDifficulty
import com.example.model.P2PLeaderboardEntry
import com.example.model.P2PSwarmState
import com.example.ui.theme.DarkCyberBackground
import com.example.ui.theme.DarkCyberBorder
import com.example.ui.theme.DarkCyberCard
import com.example.ui.theme.DarkCyberCardElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class LeaderboardSort {
    BY_HASHRATE,
    BY_BALANCE
}

@Composable
fun P2PLeaderboardScreen(
    leaderboardEntries: List<P2PLeaderboardEntry>,
    swarmState: P2PSwarmState,
    difficulty: GameDifficulty,
    seasonInfo: com.example.model.SeasonInfo = com.example.model.SeasonInfo(),
    onScanNetwork: () -> Unit,
    onChangePlayerName: (String) -> Unit
) {
    var sortBy by remember { mutableStateOf(LeaderboardSort.BY_HASHRATE) }
    var showNameEditDialog by remember { mutableStateOf(false) }

    val sortedList = remember(leaderboardEntries, sortBy) {
        when (sortBy) {
            LeaderboardSort.BY_HASHRATE -> leaderboardEntries.sortedByDescending { it.hashRateMh }
            LeaderboardSort.BY_BALANCE -> leaderboardEntries.sortedByDescending { it.balanceUsd }
        }
    }

    val myIndex = sortedList.indexOfFirst { it.isLocalPlayer }
    val myRank = if (myIndex >= 0) myIndex + 1 else 1
    val myEntry = sortedList.find { it.isLocalPlayer }

    val infiniteTransition = rememberInfiniteTransition(label = "p2p_leader_rotation")
    val scanAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "angle"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCyberBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок и P2P Сетевая панель
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "P2P ТАБЛИЦА ЛИДЕРОВ",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "СЕЗОН ${seasonInfo.seasonNumber} • Вайп через: ${seasonInfo.formattedRemaining}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Кнопка P2P сканирования
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCyberCardElevated)
                                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable(enabled = !swarmState.isScanning, onClick = onScanNetwork)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "P2P Сканирование",
                                    tint = NeonCyan,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .rotate(if (swarmState.isScanning) scanAngle else 0f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (swarmState.isScanning) "Поиск пиров..." else "Сканировать",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Метрики P2P Swarm
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkCyberCardElevated, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("P2P СЕТЬ", color = TextMuted, fontSize = 9.sp)
                            Text(
                                if (difficulty == GameDifficulty.EASY) "Mesh (Синтетика)" else "LAN & DHT Swarm",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("ПИРЫ В СЕТИ", color = TextMuted, fontSize = 9.sp)
                            Text("${sortedList.size} нод", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("ВАШ РАНГ", color = TextMuted, fontSize = 9.sp)
                            Text("#$myRank из ${sortedList.size}", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Column {
                            Text("ПИНГ", color = TextMuted, fontSize = 9.sp)
                            Text("${swarmState.averagePingMs} ms", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Статус режима сложности для P2P
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(difficulty.colorHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (difficulty) {
                                GameDifficulty.EASY -> "Режим сложности: ЛЁГКАЯ (Синтетическая сеть, без реальных людей)"
                                GameDifficulty.NORMAL -> "Режим сложности: НОРМАЛЬНАЯ (Реальные P2P игроки по Wi-Fi / LAN)"
                                GameDifficulty.HARD -> "Режим сложности: СЛОЖНАЯ (Хардкор P2P, реальные игроки)"
                            },
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Карточка профиля локального игрока
        if (myEntry != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonGreen)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen.copy(alpha = 0.2f))
                                        .border(1.dp, NeonGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "#$myRank",
                                        color = NeonGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            myEntry.playerName,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(NeonGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text("ВЫ", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(myEntry.peerId, color = TextMuted, fontSize = 10.sp)
                                }
                            }

                            // Кнопка смены имени
                            IconButton(
                                onClick = { showNameEditDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Сменить имя",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Ваш Хэшрейт", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    "${"%.1f".format(myEntry.hashRateMh)} MH/s",
                                    color = NeonGreen,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column {
                                Text("Баланс", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    "$${myEntry.balanceUsd.toInt()} USD",
                                    color = NeonOrange,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column {
                                Text("Риги / Фермы", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    "${myEntry.rigsCount} шт.",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Фильтры сортировки
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sortBy == LeaderboardSort.BY_HASHRATE) NeonGreen.copy(alpha = 0.2f) else DarkCyberCard)
                        .border(
                            1.dp,
                            if (sortBy == LeaderboardSort.BY_HASHRATE) NeonGreen else DarkCyberBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { sortBy = LeaderboardSort.BY_HASHRATE }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = if (sortBy == LeaderboardSort.BY_HASHRATE) NeonGreen else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "По хэшрейту",
                            color = if (sortBy == LeaderboardSort.BY_HASHRATE) NeonGreen else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sortBy == LeaderboardSort.BY_BALANCE) NeonOrange.copy(alpha = 0.2f) else DarkCyberCard)
                        .border(
                            1.dp,
                            if (sortBy == LeaderboardSort.BY_BALANCE) NeonOrange else DarkCyberBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { sortBy = LeaderboardSort.BY_BALANCE }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (sortBy == LeaderboardSort.BY_BALANCE) NeonOrange else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "По балансу $",
                            color = if (sortBy == LeaderboardSort.BY_BALANCE) NeonOrange else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Список пиров в таблице лидеров
        itemsIndexed(sortedList, key = { _, item -> item.peerId }) { index, entry ->
            val rank = index + 1
            LeaderboardEntryCard(rank = rank, entry = entry)
        }
    }

    // Диалог смены P2P ника
    if (showNameEditDialog) {
        var inputName by remember { mutableStateOf(myEntry?.playerName ?: "Майнер-Игрок") }
        AlertDialog(
            onDismissRequest = { showNameEditDialog = false },
            containerColor = DarkCyberCard,
            title = {
                Text("Сменить P2P имя", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Ваше имя увидят другие игроки и ноды в P2P таблице лидеров и на Авито.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it.take(20) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onChangePlayerName(inputName)
                        showNameEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Сохранить", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEditDialog = false }) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun LeaderboardEntryCard(
    rank: Int,
    entry: P2PLeaderboardEntry
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Золото
        2 -> Color(0xFFC0C0C0) // Серебро
        3 -> Color(0xFFCD7F32) // Бронза
        else -> TextSecondary
    }

    val cardBorder = when {
        entry.isLocalPlayer -> NeonGreen
        entry.isLanPeer -> NeonCyan
        rank == 1 -> Color(0xFFFFD700).copy(alpha = 0.6f)
        else -> DarkCyberBorder
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isLocalPlayer) DarkCyberCardElevated else DarkCyberCard
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Левая часть: Место + Аватар + Имя + Бейдж
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Номер места
                Box(
                    modifier = Modifier.width(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#$rank",
                        color = rankColor,
                        fontSize = if (rank <= 3) 16.sp else 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Иконка / Аватар
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                entry.isLocalPlayer -> NeonGreen.copy(alpha = 0.15f)
                                entry.isLanPeer -> NeonCyan.copy(alpha = 0.15f)
                                else -> DarkCyberCardElevated
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                entry.isLocalPlayer -> NeonGreen
                                entry.isLanPeer -> NeonCyan
                                else -> rankColor.copy(alpha = 0.5f)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.isLanPeer) {
                        Icon(Icons.Default.Lan, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    } else if (entry.isLocalPlayer) {
                        Text("Я", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Public, contentDescription = null, tint = rankColor, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entry.playerName,
                            color = if (entry.isLocalPlayer) NeonGreen else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (entry.isLocalPlayer) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(NeonGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("ВЫ", color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (entry.isLanPeer) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("LAN Пир (${entry.pingMs}ms)", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("DHT Node (${entry.pingMs}ms)", color = TextMuted, fontSize = 9.sp)
                        }
                        Text(" • ", color = TextMuted, fontSize = 9.sp)
                        Text("${entry.rigsCount} риг.", color = TextMuted, fontSize = 9.sp)
                    }
                }
            }

            // Правая часть: Хэшрейт и Баланс
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${"%.1f".format(entry.hashRateMh)} MH/s",
                    color = NeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "$${entry.balanceUsd.toInt()} USD",
                    color = NeonOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
