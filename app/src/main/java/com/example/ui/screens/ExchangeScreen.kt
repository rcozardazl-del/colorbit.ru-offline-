package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CryptoCurrency
import com.example.ui.theme.DarkCyberBorder
import com.example.ui.theme.DarkCyberCard
import com.example.ui.theme.DarkCyberCardElevated
import com.example.ui.theme.HeatRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExchangeScreen(
    cryptos: List<CryptoCurrency>,
    balances: Map<String, Double>,
    onSellCrypto: (String, Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("exchange_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCyberCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Внутриигровая P2P Биржа",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Курсы обновляются динамически в реальном времени",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        items(cryptos, key = { it.id }) { coin ->
            val userBalance = balances[coin.id] ?: 0.0
            CryptoExchangeCard(
                coin = coin,
                userBalance = userBalance,
                onSellAll = { onSellCrypto(coin.id, userBalance) },
                onSellHalf = { onSellCrypto(coin.id, userBalance / 2.0) }
            )
        }
    }
}

@Composable
private fun CryptoExchangeCard(
    coin: CryptoCurrency,
    userBalance: Double,
    onSellAll: () -> Unit,
    onSellHalf: () -> Unit
) {
    val totalUsdValue = userBalance * coin.priceUsd
    val isPriceUp = if (coin.priceHistory.size >= 2) {
        coin.priceHistory.last() >= coin.priceHistory[coin.priceHistory.size - 2]
    } else true

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCyberCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCyberBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок монеты
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(coin.colorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            coin.symbol.take(2),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(coin.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${coin.algo} • Сложность: ${coin.difficulty}", color = TextMuted, fontSize = 11.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (coin.priceUsd > 10.0) "$%.2f".format(coin.priceUsd) else "$%.4f".format(coin.priceUsd),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isPriceUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPriceUp) NeonGreen else HeatRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            if (isPriceUp) "+Вверх" else "-Вниз",
                            color = if (isPriceUp) NeonGreen else HeatRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // График свечей/колебаний (Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(DarkCyberCardElevated, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                SparklineChart(
                    history = coin.priceHistory,
                    lineColor = if (isPriceUp) NeonGreen else HeatRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Баланс пользователя
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCyberCardElevated, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Добыто на кошельке", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "%.6f %s".format(userBalance, coin.symbol),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Оценка в USD", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "≈ $%.2f".format(totalUsdValue),
                        color = NeonGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки быстрой продажи
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSellHalf,
                    enabled = userBalance > 0.000001,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Продать 50%", fontSize = 12.sp)
                }

                Button(
                    onClick = onSellAll,
                    enabled = userBalance > 0.000001,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Продать всё", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SparklineChart(
    history: List<Double>,
    lineColor: Color
) {
    if (history.size < 2) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val min = history.minOrNull() ?: 0.0
        val max = history.maxOrNull() ?: 1.0
        val range = (max - min).coerceAtLeast(0.0001)

        val width = size.width
        val height = size.height
        val stepX = width / (history.size - 1)

        val path = Path()
        history.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = ((value - min) / range).toFloat()
            val y = height - (normalizedY * (height - 8f)) - 4f

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
