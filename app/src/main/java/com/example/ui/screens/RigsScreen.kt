package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.Dialog
import com.example.model.CryptoCurrency
import com.example.model.MiningRig
import com.example.ui.theme.ColorbitBorder
import com.example.ui.theme.ColorbitCard
import com.example.ui.theme.ColorbitLime
import com.example.ui.theme.ColorbitOrange
import com.example.ui.theme.DarkCyberBorder
import com.example.ui.theme.DarkCyberCard
import com.example.ui.theme.DarkCyberCardElevated
import com.example.ui.theme.HeatRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RigsScreen(
    rigs: List<MiningRig>,
    cryptos: List<CryptoCurrency>,
    onTogglePower: (String) -> Unit,
    onSwitchCoin: (String, String) -> Unit,
    onCleanDust: (String) -> Unit,
    onReplacePaste: (String) -> Unit,
    onAddNewRig: (String) -> Unit,
    onApplyOverclock: (String, Int, Int, Int, Int) -> Unit
) {
    var showAddRigDialog by remember { mutableStateOf(false) }
    var newRigName by remember { mutableStateOf("") }
    var overclockingRig by remember { mutableStateOf<MiningRig?>(null) }

    val totalHashRate = rigs.sumOf { it.totalHashRateMh }
    val totalWatts = rigs.sumOf { it.totalPowerWatts }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rigs_screen_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Общая сводка фермы в стиле панели Colorbit (app-bg, #2B2B2B)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorbitCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Суммарный хэшрейт", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            "%.1f MH/s".format(totalHashRate),
                            color = NeonGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Потребление: $totalWatts W", color = TextPrimary, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { showAddRigDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_new_rig_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Новый риг", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Список ригов
        items(rigs, key = { it.id }) { rig ->
            RigItemCard(
                rig = rig,
                cryptos = cryptos,
                onTogglePower = { onTogglePower(rig.id) },
                onSwitchCoin = { coin -> onSwitchCoin(rig.id, coin) },
                onCleanDust = { onCleanDust(rig.id) },
                onReplacePaste = { onReplacePaste(rig.id) },
                onOpenOverclock = { overclockingRig = rig }
            )
        }
    }

    if (overclockingRig != null) {
        val rig = overclockingRig!!
        OverclockDialog(
            rig = rig,
            onDismiss = { overclockingRig = null },
            onApply = { coreMhz, memMhz, powerLimit, fanSpeed ->
                onApplyOverclock(rig.id, coreMhz, memMhz, powerLimit, fanSpeed)
            }
        )
    }

    if (showAddRigDialog) {
        Dialog(onDismissRequest = { showAddRigDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Собрать новый каркас рига",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Стоимость алюминиевого каркаса и базовой платформы: $45",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newRigName,
                        onValueChange = { newRigName = it },
                        label = { Text("Название рига") },
                        placeholder = { Text("Например: Риг #2 RTX") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showAddRigDialog = false }) {
                            Text("Отмена")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onAddNewRig(newRigName)
                                newRigName = ""
                                showAddRigDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Купить каркас ($45)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RigItemCard(
    rig: MiningRig,
    cryptos: List<CryptoCurrency>,
    onTogglePower: () -> Unit,
    onSwitchCoin: (String) -> Unit,
    onCleanDust: () -> Unit,
    onReplacePaste: () -> Unit,
    onOpenOverclock: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasActiveOc = rig.memoryClockOffsetMhz != 0 || rig.coreClockOffsetMhz != 0 || rig.powerLimitPercent != 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rig_card_${rig.id}"),
        colors = CardDefaults.cardColors(containerColor = ColorbitCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rig.isOverheated) HeatRed else if (rig.isPoweredOn) ColorbitLime else ColorbitBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Верхняя шапка рига в стилистике Colorbit (rigs-list__item)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(
                                if (!rig.isPoweredOn) TextMuted
                                else if (rig.isOverheated) HeatRed
                                else NeonGreen
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rig.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (hasActiveOc) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonOrange.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("OC", color = NeonOrange, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Text(
                            "Локация: ${rig.location}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onTogglePower,
                    modifier = Modifier.testTag("power_btn_${rig.id}")
                ) {
                    Icon(
                        Icons.Default.PowerSettingsNew,
                        contentDescription = "Питание",
                        tint = if (rig.isPoweredOn) NeonGreen else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Показатели: Хэшрейт, Ватты, Температура (стиль плашки stats в Colorbit)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Хэшрейт", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "%.1f MH/s".format(rig.totalHashRateMh),
                        color = if (rig.isPoweredOn) NeonGreen else TextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("Питание", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "${rig.totalPowerWatts}W / ${rig.psuCapacityWatts}W",
                        color = if (rig.isPsuOverloaded) HeatRed else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Температура", color = TextSecondary, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = when {
                                rig.isOverheated -> HeatRed
                                rig.isWarningTemp -> NeonOrange
                                else -> NeonCyan
                            },
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            "%.1f°C".format(rig.currentTemperatureC),
                            color = when {
                                rig.isOverheated -> HeatRed
                                rig.isWarningTemp -> NeonOrange
                                else -> TextPrimary
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (rig.isOverheated) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeatRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = HeatRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Критический перегрев (>92°C)! Майнинг остановлен для защиты.",
                        color = HeatRed,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Выбор монеты для майнинга
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Майнинг монеты:", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cryptos.forEach { coin ->
                        val isSelected = rig.currentCryptoId == coin.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else DarkCyberCardElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSwitchCoin(coin.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                coin.symbol,
                                color = if (isSelected) NeonCyan else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Состояние железа (пыль и термопаста)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Пыль", color = TextMuted, fontSize = 11.sp)
                        Text("%.0f%%".format(rig.dustLevelPercent), color = TextPrimary, fontSize = 11.sp)
                    }
                    LinearProgressIndicator(
                        progress = { (rig.dustLevelPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (rig.dustLevelPercent > 60f) NeonOrange else NeonCyan,
                        trackColor = DarkCyberCardElevated,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Термопаста", color = TextMuted, fontSize = 11.sp)
                        Text("%.0f%%".format(rig.thermalPasteCondition), color = TextPrimary, fontSize = 11.sp)
                    }
                    LinearProgressIndicator(
                        progress = { (rig.thermalPasteCondition / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (rig.thermalPasteCondition < 30f) HeatRed else NeonGreen,
                        trackColor = DarkCyberCardElevated,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки обслуживания и Разгон (стилистика кнопок Colorbit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCleanDust,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitBorder)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Пыль ($5)", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onReplacePaste,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitBorder)
                ) {
                    Text("Паста ($12)", fontSize = 11.sp)
                }

                Button(
                    onClick = onOpenOverclock,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorbitOrange),
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Разгон", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            // Раскрывающийся список установленных деталей (в стилистике слотов Colorbit RigSlot)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isExpanded) "Скрыть слоты (${rig.installedComponents.size})"
                    else "Комплектующие и слоты (${rig.installedComponents.size})",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rig.installedComponents.forEach { comp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF212121), RoundedCornerShape(6.dp))
                                .border(1.dp, ColorbitBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(comp.component.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${comp.component.type.title} | ${comp.component.powerWatts}W" +
                                                if (comp.component.hashRateMh > 0) " | ${comp.component.hashRateMh} MH/s" else "",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "%.0f%%".format(comp.durabilityPercent),
                                    color = if (comp.durabilityPercent > 70f) NeonGreen else NeonOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text("состояние", color = TextMuted, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
