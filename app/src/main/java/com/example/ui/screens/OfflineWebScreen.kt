package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File
import java.io.InputStream

data class OfflineWebShop(
    val name: String,
    val slug: String,
    val assetPath: String,
    val description: String
)

val OFFLINE_WEB_SHOPS = listOf(
    OfflineWebShop("DHS (DNS)", "dhs", "file:///android_asset/web/shops/dhs.html", "293+ новых комплектующих"),
    OfflineWebShop("Aliexprezz", "aliexprezz", "file:///android_asset/web/shops/aliexprezz.html", "Товары из Китая (118+)"),
    OfflineWebShop("SoftPortal", "softportalcom", "file:///android_asset/web/shops/softportalcom.html", "34 ОС и майнинг софта"),
    OfflineWebShop("Гертруда", "gertruda-karkas", "file:///android_asset/web/shops/gertruda-karkas.html", "Каркасы и стойки"),
    OfflineWebShop("OnlyFans", "onlyfans", "file:///android_asset/web/shops/onlyfans.html", "Кулеры и охлаждение"),
    OfflineWebShop("ДомКлик", "domklik", "file:///android_asset/web/shops/domklik.html", "Помещения и аренда"),
    OfflineWebShop("Все Магазины", "shops", "file:///android_asset/web/shops/shops.html", "Каталог магазинов"),
    OfflineWebShop("Локации", "locations", "file:///android_asset/web/pages/locations.html", "Недвижимость для ферм"),
    OfflineWebShop("Ферма", "farm", "file:///android_asset/web/pages/farm.html", "Вид фермы"),
    OfflineWebShop("Главная", "index", "file:///android_asset/web/index.html", "Главная страница")
)

/**
 * Офлайн просмотр оригинального HTML + JavaScript кода Colorbit:
 * Открывает подлинные скачанные страницы магазинов, подгружает все скрипты Vue/Inertia,
 * CSS стили, манифесты и изображения локально без необходимости интернета.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OfflineWebScreen(
    initialShopSlug: String = "dhs",
    onClose: () -> Unit,
    onOpenNativeAvito: () -> Unit
) {
    val context = LocalContext.current
    val initialShop = OFFLINE_WEB_SHOPS.find { it.slug == initialShopSlug } ?: OFFLINE_WEB_SHOPS.first()
    var currentUrl by remember { mutableStateOf(initialShop.assetPath) }
    var currentTitle by remember { mutableStateOf(initialShop.name) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorbitBg)
            .testTag("offline_web_screen")
    ) {
        // WebView с перехватом локальных ассетов
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
                        allowFileAccess = true
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            if (url != null) {
                                currentUrl = url
                                val matched = OFFLINE_WEB_SHOPS.find { it.assetPath == url }
                                if (matched != null) currentTitle = matched.name
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            if (url != null) {
                                currentUrl = url
                            }
                        }

                        // Умный перехватчик: перенаправляет любые запросы на локальные скрипты, css и страницы
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString() ?: return null

                            // 1. Если запрашивается внешний скрипт app / vendor / chunk
                            if (reqUrl.contains("/build/assets/") || reqUrl.contains("/scripts/")) {
                                val fileName = reqUrl.substringAfterLast("/").substringBefore("?")
                                try {
                                    val assetStream: InputStream = if (fileName.endsWith(".css")) {
                                        ctx.assets.open("web/styles/$fileName")
                                    } else {
                                        ctx.assets.open("web/scripts/$fileName")
                                    }
                                    val mime = if (fileName.endsWith(".css")) "text/css" else "application/javascript"
                                    return WebResourceResponse(mime, "UTF-8", assetStream)
                                } catch (e: Exception) {
                                    // Попробуем поискать в сохраненной папке пользователя
                                    val userDir = ColorbitDownloader.getTargetDirectory(ctx)
                                    val localF = if (fileName.endsWith(".css")) File(userDir, "styles/$fileName") else File(userDir, "scripts/$fileName")
                                    if (localF.exists()) {
                                        val mime = if (fileName.endsWith(".css")) "text/css" else "application/javascript"
                                        return WebResourceResponse(mime, "UTF-8", localF.inputStream())
                                    }
                                }
                            }

                            // 2. Если запрашивается переход на colorbit.ru/shops/<slug>
                            if (reqUrl.contains("colorbit.ru/shops/")) {
                                val slug = reqUrl.substringAfter("colorbit.ru/shops/").substringBefore("?").substringBefore("/")
                                if (slug.isNotEmpty()) {
                                    try {
                                        val assetStream = ctx.assets.open("web/shops/$slug.html")
                                        return WebResourceResponse("text/html", "UTF-8", assetStream)
                                    } catch (e: Exception) {
                                        // fallback
                                    }
                                }
                            }

                            return super.shouldInterceptRequest(view, request)
                        }
                    }
                    loadUrl(currentUrl)
                    webViewInstance = this
                }
            }
        )

        // Верхняя панель навигации по оригинальным магазинам и страницам HTML
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color(0xFF1E1E1E).copy(alpha = 0.96f))
                .border(1.dp, ColorbitBorder)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .border(1.dp, NeonGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "ОФЛАЙН HTML + JS",
                            color = NeonGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        currentTitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Кнопка перехода в наш интерактивный Авито
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ColorbitOrange.copy(alpha = 0.25f))
                            .border(1.dp, ColorbitOrange, RoundedCornerShape(6.dp))
                            .clickable { onOpenNativeAvito() }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Авито P2P",
                            color = ColorbitOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            webViewInstance?.reload()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorbitLime),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            "В игру",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Горизонтальный скролл со всеми офлайн-страницами и магазинами
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HTML Сорцы:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                OFFLINE_WEB_SHOPS.forEach { shop ->
                    val isCurrent = currentUrl == shop.assetPath || (currentUrl.endsWith("/${shop.slug}.html"))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCurrent) ColorbitLime.copy(alpha = 0.25f) else Color(0xFF2B2B2B))
                            .border(1.dp, if (isCurrent) ColorbitLime else ColorbitBorder, RoundedCornerShape(4.dp))
                            .clickable {
                                currentUrl = shop.assetPath
                                currentTitle = shop.name
                                webViewInstance?.loadUrl(shop.assetPath)
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
        }

        // Загрузка страницы
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorbitBg.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Загрузка HTML & JS сорцов...",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
