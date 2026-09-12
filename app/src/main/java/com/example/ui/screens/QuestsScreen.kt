package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.PlayerStats
import com.example.model.StoryChapter
import com.example.model.StoryQuest
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

enum class QuestTab(val title: String) {
    STORY_CAMPAIGN("Сюжет Colorbit"),
    DAILY_TASKS("Задания")
}

@Composable
fun QuestsScreen(
    quests: List<StoryQuest>,
    stats: PlayerStats,
    storyChapters: List<StoryChapter>,
    onClaimReward: (String) -> Unit,
    onOpenStoryDialogue: (StoryChapter) -> Unit,
    onClaimChapterReward: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(QuestTab.STORY_CAMPAIGN) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quests_screen")
    ) {
        // Переключатель вкладок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuestTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val tabColor = if (tab == QuestTab.STORY_CAMPAIGN) NeonPurple else NeonCyan

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) tabColor.copy(alpha = 0.2f) else DarkCyberCard)
                        .border(1.dp, if (isSelected) tabColor else DarkCyberBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (tab == QuestTab.STORY_CAMPAIGN) Icons.Default.AutoAwesome else Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (isSelected) tabColor else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            tab.title,
                            color = if (isSelected) tabColor else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Уровень и XP игрока
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCyberCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Уровень майнера", color = TextSecondary, fontSize = 12.sp)
                                    Text("Мастер фермы (Ур. ${stats.level})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            Text("${stats.xp} XP", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val nextLvlXp = stats.level * 500
                        val progress = (stats.xp % nextLvlXp).toFloat() / nextLvlXp.toFloat()
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonCyan,
                            trackColor = DarkCyberCard
                        )
                    }
                }
            }

            if (selectedTab == QuestTab.STORY_CAMPAIGN) {
                item {
                    Text(
                        "СЮЖЕТНАЯ ЛИНИЯ COLORBIT (7 ГЛАВ):",
                        color = NeonPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(storyChapters, key = { it.chapterNumber }) { chapter ->
                    StoryChapterCard(
                        chapter = chapter,
                        isCurrent = stats.currentStoryChapter == chapter.chapterNumber,
                        onOpenDialogue = { onOpenStoryDialogue(chapter) },
                        onComplete = { onClaimChapterReward(chapter.chapterNumber) }
                    )
                }
            } else {
                item {
                    Text(
                        "ЕЖЕДНЕВНЫЕ ЗАДАНИЯ:",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(quests, key = { it.id }) { quest ->
                    QuestCard(
                        quest = quest,
                        onClaim = { onClaimReward(quest.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryChapterCard(
    chapter: StoryChapter,
    isCurrent: Boolean,
    onOpenDialogue: () -> Unit,
    onComplete: () -> Unit
) {
    val borderColor = when {
        chapter.isCompleted -> NeonGreen.copy(alpha = 0.6f)
        isCurrent -> NeonPurple
        chapter.isUnlocked -> NeonCyan.copy(alpha = 0.5f)
        else -> DarkCyberBorder
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
        border = androidx.compose.foundation.BorderStroke(if (isCurrent) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (chapter.isCompleted) NeonGreen.copy(alpha = 0.2f) else NeonPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${chapter.chapterNumber}",
                            color = if (chapter.isCompleted) NeonGreen else NeonPurple,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            chapter.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            chapter.subtitle,
                            color = NeonOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                when {
                    chapter.isCompleted -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ПРОЙДЕНО", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    isCurrent -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonPurple.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ТЕКУЩАЯ", color = NeonPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    !chapter.isUnlocked -> {
                        Icon(Icons.Default.Lock, contentDescription = "Заблокировано", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                chapter.synopsis,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Требование и цель
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCyberCardElevated)
                    .padding(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Цель: ${chapter.objectiveText}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Локация: ${chapter.requiredFacilityName}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            "Награда: +$${chapter.rewardUsd.toInt()} • +${chapter.rewardXp} XP",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки действий главы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenDialogue,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Диалог", color = NeonPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (isCurrent && !chapter.isCompleted) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Text("Сдать главу", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestCard(
    quest: StoryQuest,
    onClaim: () -> Unit
) {
    val progress = (quest.currentValue / quest.targetValue).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (quest.isCompleted && !quest.isClaimed) NeonGreen else DarkCyberBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        quest.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        quest.description,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+$${quest.rewardUsd.toInt()}", color = NeonOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Прогресс", color = TextMuted, fontSize = 11.sp)
                Text(
                    "%.3f / %.3f".format(quest.currentValue.coerceAtMost(quest.targetValue), quest.targetValue),
                    color = TextPrimary,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (quest.isCompleted) NeonGreen else NeonCyan,
                trackColor = DarkCyberCardElevated
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                quest.isClaimed -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Награда получена", color = TextMuted, fontSize = 12.sp)
                    }
                }
                quest.isCompleted -> {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Забрать награду (+$${quest.rewardUsd.toInt()})", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Text(
                        "В процессе выполнения...",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
