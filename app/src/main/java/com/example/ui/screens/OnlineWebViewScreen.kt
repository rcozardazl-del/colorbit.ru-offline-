package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ColorbitDownloader
import com.example.ui.theme.ColorbitBg
import com.example.ui.theme.ColorbitBorder
import com.example.ui.theme.ColorbitCard
import com.example.ui.theme.ColorbitLime
import com.example.ui.theme.ColorbitOrange
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
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OnlineWebViewScreen(
    onSwitchToOffline: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val downloadProgress by ColorbitDownloader.progress.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf("https://colorbit.ru") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var showDownloaderSheet by remember { mutableStateOf(false) }

    // Автоматический старт скачивания разметки и JS при первом заходе в онлайн режим
    LaunchedEffect(Unit) {
        if (!downloadProgress.isDownloading && downloadProgress.totalFiles == 0) {
            ColorbitDownloader.downloadAllSiteAndShops(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorbitBg)
            .testTag("online_webview_container")
    ) {
        // Главный WebView контейнер
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = true
                        displayZoomControls = false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            hasError = false
                            if (url != null) currentUrl = url
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            if (url != null) currentUrl = url
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                isLoading = false
                                hasError = true
                            }
                        }
                    }
                    loadUrl(currentUrl)
                    webViewInstance = this
                }
            }
        )

        // Верхняя панель управления с переходом по магазинам и кнопкой загрузчика
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color(0xFF1E1E1E).copy(alpha = 0.95f))
                .border(1.dp, ColorbitBorder)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Строка 1: Статус сайта, Скачивание, Офлайн, Обновить
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
                            .background(if (hasError) HeatRed else ColorbitLime)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "colorbit.ru",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Кнопка статуса авто-скачивания сорцов/магазинов
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (downloadProgress.isDownloading) ColorbitOrange.copy(alpha = 0.2f) else Color(0xFF2B2B2B))
                            .border(1.dp, if (downloadProgress.isDownloading) ColorbitOrange else ColorbitBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                if (!downloadProgress.isDownloading) {
                                    coroutineScope.launch {
                                        Toast.makeText(context, "Скачивание скриптов и магазинов начато...", Toast.LENGTH_SHORT).show()
                                        ColorbitDownloader.downloadAllSiteAndShops(context)
                                    }
                                }
                                showDownloaderSheet = !showDownloaderSheet
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (downloadProgress.isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(11.dp),
                                    strokeWidth = 2.dp,
                                    color = ColorbitOrange
                                )
                            } else {
                                Icon(
                                    Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (downloadProgress.isDownloading) "Качаю..." else "Скачать код",
                                color = if (downloadProgress.isDownloading) ColorbitOrange else TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            hasError = false
                            isLoading = true
                            webViewInstance?.reload()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = NeonCyan,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onSwitchToOffline,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorbitLime),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Офлайн",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Строка 2: Быстрый переход по всем магазинам Colorbit
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Магазины:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                ColorbitDownloader.KNOWN_SHOPS.forEach { shop ->
                    val isCurrent = currentUrl == shop.fullUrl
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCurrent) ColorbitLime.copy(alpha = 0.25f) else Color(0xFF2B2B2B))
                            .border(1.dp, if (isCurrent) ColorbitLime else ColorbitBorder, RoundedCornerShape(4.dp))
                            .clickable {
                                currentUrl = shop.fullUrl
                                webViewInstance?.loadUrl(shop.fullUrl)
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            shop.name,
                            color = if (isCurrent) ColorbitLime else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Строка 3: Индикатор процесса скачивания скриптов в Загрузки
            if (downloadProgress.isDownloading || showDownloaderSheet) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Пакетная выгрузка JS & HTML в /Downloads",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${downloadProgress.completedFiles}/${downloadProgress.totalFiles}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (downloadProgress.totalFiles > 0) {
                            val frac = downloadProgress.completedFiles.toFloat() / downloadProgress.totalFiles.toFloat()
                            LinearProgressIndicator(
                                progress = { frac.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = NeonGreen,
                                trackColor = Color(0xFF1E1E1E)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            downloadProgress.statusMessage,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Индикатор загрузки страницы
        if (isLoading && !hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorbitBg.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Загрузка $currentUrl...",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Сообщение об ошибке (Белые списки или недоступность сети)
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorbitBg.copy(alpha = 0.95f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorbitCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitOrange)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorbitOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = ColorbitOrange,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "СЕРВЕР НЕДОСТУПЕН",
                            color = ColorbitOrange,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            "Сервер colorbit.ru недоступен по текущей ссылке.\nВсе скрипты и разметка магазинов уже сохраняются в папку Загрузки, а вы можете мгновенно играть в автономном офлайн-режиме!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onSwitchToOffline,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorbitLime),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Играть офлайн (Все магазины и фермы)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = {
                                hasError = false
                                isLoading = true
                                webViewInstance?.reload()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorbitBorder)
                        ) {
                            Text("Повторить подключение", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
