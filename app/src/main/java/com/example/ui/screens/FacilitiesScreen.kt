package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MiningFacility
import com.example.model.RentPeriod
import com.example.ui.theme.DarkCyberBorder
import com.example.ui.theme.DarkCyberCard
import com.example.ui.theme.DarkCyberCardElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FacilitiesScreen(
    facilities: List<MiningFacility>,
    balanceUsd: Double,
    currentRigsCount: Int = 1,
    onRentFacility: (String, RentPeriod) -> Unit,
    onSetActiveFacility: (String) -> Unit,
    onUnlockFacility: (String) -> Unit = { id -> onRentFacility(id, RentPeriod.FOREVER) }
) {
    val activeFacility = facilities.firstOrNull { it.isActiveLocation && it.isUnlocked }
        ?: facilities.firstOrNull { it.isUnlocked }
        ?: facilities.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("facilities_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Шапка раздела: ДомКлик (Недвижимость Colorbit)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCyberCardElevated),
                border = BorderStroke(1.dp, NeonOrange.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(NeonOrange.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "ДомКлик — Недвижимость",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("COLORBIT", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text(
                                "Аренда на 2 дня, 7 дней, 30 дней или покупка навсегда",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (activeFacility != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCyberCard, RoundedCornerShape(10.dp))
                                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Текущая активная база:",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    activeFacility.name,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    if (activeFacility.isPermanent) "В собственности навсегда"
                                    else "Аренда: ${activeFacility.remainingTimeFormatted()}",
                                    color = if (activeFacility.isPermanent) NeonCyan else NeonOrange,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Занято: $currentRigsCount / ${activeFacility.maxRigs} ригов",
                                    color = if (currentRigsCount >= activeFacility.maxRigs) NeonOrange else NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    "$${activeFacility.electricityCostKwhUsd} / кВт⋅ч",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Список всех домов
        items(facilities, key = { it.id }) { facility ->
            FacilityCard(
                facility = facility,
                balanceUsd = balanceUsd,
                onRent = { period -> onRentFacility(facility.id, period) },
                onSetActive = { onSetActiveFacility(facility.id) }
            )
        }
    }
}

@Composable
private fun FacilityCard(
    facility: MiningFacility,
    balanceUsd: Double,
    onRent: (RentPeriod) -> Unit,
    onSetActive: () -> Unit
) {
    // Выбранный срок аренды: 2 дня, 7 дней, 30 дней или навсегда
    var selectedPeriod by rememberSaveable(facility.id) {
        mutableStateOf(if (facility.priceForeverUsd == 0.0) RentPeriod.FOREVER else RentPeriod.TWO_DAYS)
    }

    val selectedPrice = facility.getPrice(selectedPeriod)
    val canAfford = balanceUsd >= selectedPrice || selectedPrice == 0.0

    val icon: ImageVector = when {
        facility.id.contains("parents") -> Icons.Default.Home
        facility.id.contains("garage") -> Icons.Default.Warehouse
        facility.id.contains("apartment") -> Icons.Default.Apartment
        facility.id.contains("cottage") -> Icons.Default.Home
        facility.id.contains("warehouse") -> Icons.Default.Warehouse
        facility.id.contains("hydro") -> Icons.Default.AcUnit
        facility.id.contains("dubai") -> Icons.Default.Star
        else -> Icons.Default.LocationCity
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("facility_card_${facility.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
        border = BorderStroke(
            1.dp,
            when {
                facility.isActiveLocation -> NeonGreen
                facility.isUnlocked -> NeonCyan.copy(alpha = 0.5f)
                else -> DarkCyberBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок карточки + статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (facility.isActiveLocation) NeonGreen.copy(alpha = 0.2f)
                                else if (facility.isUnlocked) NeonCyan.copy(alpha = 0.15f)
                                else DarkCyberCardElevated,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = when {
                                facility.isActiveLocation -> NeonGreen
                                facility.isUnlocked -> NeonCyan
                                else -> TextMuted
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            facility.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            facility.category,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Статус метки
                if (facility.isActiveLocation) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("АКТИВНА", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (facility.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (facility.isPermanent) "КУПЛЕНО" else "АРЕНДОВАНО",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (facility.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    facility.description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Статус времени аренды
            if (facility.isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCyberCardElevated, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (facility.isPermanent) NeonGreen else NeonOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            facility.remainingTimeFormatted(),
                            color = if (facility.isPermanent) NeonGreen else NeonOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (!facility.isActiveLocation) {
                        Text(
                            "Доступно для переезда",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Сетка характеристик
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCyberCardElevated, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Свет", color = TextSecondary, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "$${facility.electricityCostKwhUsd}/кВт⋅ч",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Column {
                    Text("Среда", color = TextSecondary, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Thermostat, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "%.0f°C".format(facility.ambientTempC),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Column {
                    Text("Вместимость", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "${facility.maxRigs} ригов",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Пыль", color = TextSecondary, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "x${facility.dustAccumulationRate}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Если не выкуплено навсегда, показываем опции: 2 дня, 7 дней, 30 дней, Навсегда
            if (!facility.isPermanent) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    if (facility.isUnlocked) "Продлить срок или выкупить:" else "Выберите срок аренды или покупку:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Кнопки выбора: 2 дня, 7 дней, 30 дней, Навсегда
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RentPeriod.values().forEach { period ->
                        val price = facility.getPrice(period)
                        val isSelected = selectedPeriod == period
                        val isFree = price == 0.0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) NeonOrange.copy(alpha = 0.2f)
                                    else DarkCyberCardElevated
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NeonOrange else DarkCyberBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPeriod = period }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    period.title,
                                    color = if (isSelected) NeonOrange else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    if (isFree) "Беспл." else "$${price.toInt()}",
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Кнопка оплаты аренды / покупки
                val buttonText = when {
                    selectedPrice == 0.0 -> "Получить бесплатно"
                    facility.isUnlocked && selectedPeriod == RentPeriod.FOREVER -> "Выкупить навсегда за $${selectedPrice.toInt()}"
                    facility.isUnlocked -> "Продлить на ${selectedPeriod.title} за $${selectedPrice.toInt()}"
                    selectedPeriod == RentPeriod.FOREVER -> "Купить навсегда за $${selectedPrice.toInt()}"
                    else -> "Арендовать на ${selectedPeriod.title} за $${selectedPrice.toInt()}"
                }

                Button(
                    onClick = { onRent(selectedPeriod) },
                    enabled = canAfford,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rent_button_${facility.id}_${selectedPeriod.name}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonOrange,
                        disabledContainerColor = DarkCyberCardElevated
                    )
                ) {
                    if (!facility.isUnlocked && selectedPrice > 0.0) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        if (canAfford) buttonText else "Не хватает средств ($${selectedPrice.toInt()})",
                        color = if (canAfford) Color.Black else TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Кнопка переезда в это помещение (если оно открыто и не активно сейчас)
            if (facility.isUnlocked && !facility.isActiveLocation) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSetActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("set_active_button_${facility.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, NeonGreen)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Переехать сюда (Сделать активной базой)",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
