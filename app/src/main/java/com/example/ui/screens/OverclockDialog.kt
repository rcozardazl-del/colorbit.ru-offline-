package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.model.MiningRig
import com.example.ui.theme.DarkCyberBackground
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
import java.util.Locale

@Composable
fun OverclockDialog(
    rig: MiningRig,
    onDismiss: () -> Unit,
    onApply: (coreMhz: Int, memMhz: Int, powerLimit: Int, fanSpeed: Int) -> Unit
) {
    var memOffset by remember { mutableFloatStateOf(rig.memoryClockOffsetMhz.toFloat()) }
    var coreOffset by remember { mutableFloatStateOf(rig.coreClockOffsetMhz.toFloat()) }
    var powerLimit by remember { mutableFloatStateOf(rig.powerLimitPercent.toFloat()) }
    var fanSpeed by remember { mutableFloatStateOf(rig.fanSpeedPercent.toFloat()) }

    // Расчёт прогнозируемого хэшрейта и потребления
    val baseHash = rig.installedComponents.sumOf { it.component.hashRateMh * (it.durabilityPercent / 100.0) }
    val ocMultiplier = 1.0 + (memOffset.toInt() * 0.00028) + (coreOffset.toInt() * 0.00012)
    val predictedHash = (baseHash * ocMultiplier).coerceAtLeast(0.0)

    val baseWatts = rig.installedComponents.sumOf { it.component.powerWatts } + 25
    val predictedWatts = (baseWatts * (powerLimit.toInt() / 100.0)).toInt().coerceAtLeast(20)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("overclock_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCyberCardElevated),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonOrange.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Шапка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = "Разгон",
                                tint = NeonOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "РАЗГОН ВИДЕОКАРТ (OC)",
                                color = NeonOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                rig.name,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Прогноз производительности
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCyberBackground)
                        .border(1.dp, DarkCyberBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Хэшрейт", color = TextMuted, fontSize = 11.sp)
                        Text(
                            String.format(Locale.US, "%.1f MH/s", predictedHash),
                            color = NeonCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val hashDiff = (ocMultiplier - 1.0) * 100
                        Text(
                            "${if (hashDiff >= 0) "+" else ""}${String.format(Locale.US, "%.1f", hashDiff)}%",
                            color = if (hashDiff >= 0) NeonGreen else HeatRed,
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Мощность", color = TextMuted, fontSize = 11.sp)
                        Text(
                            "${predictedWatts}W",
                            color = if (predictedWatts > rig.psuCapacityWatts && rig.psuCapacityWatts > 0) HeatRed else TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Лимит: ${powerLimit.toInt()}%",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Кулеры", color = TextMuted, fontSize = 11.sp)
                        Text(
                            "${fanSpeed.toInt()}%",
                            color = NeonOrange,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Обороты", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Частота памяти (Memory Clock)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Частота памяти (Memory Clock)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${if (memOffset >= 0) "+" else ""}${memOffset.toInt()} MHz",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = memOffset,
                    onValueChange = { memOffset = it },
                    valueRange = -500f..1500f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = DarkCyberBorder
                    )
                )

                // 2. Частота ядра (Core Clock)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Частота ядра (Core Clock)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${if (coreOffset >= 0) "+" else ""}${coreOffset.toInt()} MHz",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = coreOffset,
                    onValueChange = { coreOffset = it },
                    valueRange = -200f..300f,
                    steps = 24,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = DarkCyberBorder
                    )
                )

                // 3. Power Limit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Лимит мощности (Power Limit)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${powerLimit.toInt()}%",
                        color = if (powerLimit > 105f) HeatRed else NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = powerLimit,
                    onValueChange = { powerLimit = it },
                    valueRange = 60f..125f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = if (powerLimit > 105f) HeatRed else NeonGreen,
                        activeTrackColor = if (powerLimit > 105f) HeatRed else NeonGreen,
                        inactiveTrackColor = DarkCyberBorder
                    )
                )

                // 4. Скорость вентиляторов
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Скорость кулеров (Fan Speed)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${fanSpeed.toInt()}%", color = NeonOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = fanSpeed,
                    onValueChange = { fanSpeed = it },
                    valueRange = 30f..100f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonOrange,
                        activeTrackColor = NeonOrange,
                        inactiveTrackColor = DarkCyberBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            memOffset = 0f
                            coreOffset = 0f
                            powerLimit = 100f
                            fanSpeed = 75f
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Сток (Reset)", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onApply(
                                coreOffset.toInt(),
                                memOffset.toInt(),
                                powerLimit.toInt(),
                                fanSpeed.toInt()
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("apply_overclock_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Применить", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
