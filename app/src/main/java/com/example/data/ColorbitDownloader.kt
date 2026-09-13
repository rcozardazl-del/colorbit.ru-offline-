package com.example.data

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class DownloadProgress(
    val isDownloading: Boolean = false,
    val currentUrl: String = "",
    val totalFiles: Int = 0,
    val completedFiles: Int = 0,
    val downloadedBytes: Long = 0,
    val statusMessage: String = "Готов к скачиванию",
    val saveDirectoryPath: String = "",
    val lastDownloadedFiles: List<String> = emptyList()
)

data class ColorbitShopLink(
    val name: String,
    val slug: String,
    val fullUrl: String,
    val description: String
)

object ColorbitDownloader {
    private const val TAG = "ColorbitDownloader"
    private const val BASE_URL = "https://colorbit.ru"

    // Все подтверждённые маршруты магазинов и разделов на сайте Colorbit.ru
    val KNOWN_SHOPS = listOf(
        ColorbitShopLink("DHS (DNS)", "dhs", "$BASE_URL/shops/dhs", "Магазин новых комплектующих (293+ товаров)"),
        ColorbitShopLink("Aliexprezz", "aliexprezz", "$BASE_URL/shops/aliexprezz", "Магазин из Китая с доставкой (118+ товаров)"),
        ColorbitShopLink("SoftPortal.com", "softportalcom", "$BASE_URL/shops/softportalcom", "Каталог операционных систем и ПО (34 товара)"),
        ColorbitShopLink("Гертруда-каркас", "gertruda-karkas", "$BASE_URL/shops/gertruda-karkas", "Каркасы и стойки для майнинг ферм (11 товаров)"),
        ColorbitShopLink("OnlyFans", "onlyfans", "$BASE_URL/shops/onlyfans", "Корпусные кулеры и охлаждение (32 товара)"),
        ColorbitShopLink("ДомКлик Недвижимость", "domklik", "$BASE_URL/shops/domklik", "Помещения, гаражи и ангары для ферм"),
        ColorbitShopLink("Avinto Б/У", "avinto", "$BASE_URL/shops/avinto", "Вторичный рынок и б/у комплектующие"),
        ColorbitShopLink("Все Магазины (Каталог)", "shops", "$BASE_URL/shops", "Главный каталог магазинов"),
        ColorbitShopLink("Локации / Недвижимость", "locations", "$BASE_URL/locations", "Каталог помещений для майнинга")
    )

    private val _progress = MutableStateFlow(DownloadProgress())
    val progress = _progress.asStateFlow()

    fun getExportFolder(context: Context): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetFolder = File(downloadsDir, "Colorbit_Full_Archive")
        if (!targetFolder.exists()) {
            targetFolder.mkdirs()
        }
        return targetFolder
    }

    suspend fun downloadAllSiteAndShops(context: Context) = withContext(Dispatchers.IO) {
        val targetDir = getExportFolder(context)
        val jsDir = File(targetDir, "scripts").apply { mkdirs() }
        val cssDir = File(targetDir, "styles").apply { mkdirs() }
        val htmlDir = File(targetDir, "pages").apply { mkdirs() }
        val shopsDir = File(targetDir, "shops").apply { mkdirs() }

        _progress.value = DownloadProgress(
            isDownloading = true,
            statusMessage = "Анализ colorbit.ru и поиск скриптов...",
            saveDirectoryPath = targetDir.absolutePath
        )

        val downloadedFiles = mutableListOf<String>()
        var totalBytes = 0L

        try {
            // 1. Скачиваем главную страницу
            _progress.value = _progress.value.copy(
                currentUrl = BASE_URL,
                statusMessage = "Скачивание главной страницы index.html..."
            )
            val homeHtml = fetchUrl(BASE_URL)
            if (homeHtml != null) {
                val indexFile = File(targetDir, "index.html")
                indexFile.writeText(homeHtml)
                downloadedFiles.add("index.html (${homeHtml.length / 1024} KB)")
                totalBytes += homeHtml.length
            }

            // 2. Ищем и качаем CSS и основные JS скрипты
            val cssUrls = extractRegexUrls(homeHtml ?: "", "href=[\"']([^\"']+\\.css[^\"']*)[\"']")
            val jsUrls = extractRegexUrls(homeHtml ?: "", "src=[\"']([^\"']+\\.js[^\"']*)[\"']")

            // Дополнительно гарантированные главные бандлы Colorbit
            val allJsUrls = (jsUrls + listOf(
                "$BASE_URL/build/assets/app-6658f0c3.js",
                "$BASE_URL/build/assets/vendor-227f998d.js",
                "$BASE_URL/build/assets/RigItem-99c122d1.js",
                "$BASE_URL/build/assets/GPUSlots-c25b6569.js",
                "$BASE_URL/build/assets/RigSlot-6e66ae03.js",
                "$BASE_URL/build/assets/RigPartInfo-579cb957.js",
                "$BASE_URL/build/assets/GPUOverclockMenu-99805706.js",
                "$BASE_URL/build/assets/StorageSoftware-7698dbc7.js",
                "$BASE_URL/build/assets/Shop-8af19f9e.js"
            )).distinct()

            val allCssUrls = (cssUrls + listOf(
                "$BASE_URL/build/assets/app-799f5a0e.css"
            )).distinct()

            val urlsToDownload = mutableListOf<Pair<String, File>>()
            allCssUrls.forEach { u ->
                val fileName = u.substringAfterLast("/").substringBefore("?")
                urlsToDownload.add(u to File(cssDir, fileName))
            }
            allJsUrls.forEach { u ->
                val fileName = u.substringAfterLast("/").substringBefore("?")
                urlsToDownload.add(u to File(jsDir, fileName))
            }

            // Добавляем страницы всех магазинов
            KNOWN_SHOPS.forEach { shop ->
                urlsToDownload.add(shop.fullUrl to File(shopsDir, "${shop.slug}.html"))
            }

            // Дополнительные важные игровые страницы
            listOf(
                "$BASE_URL/farm" to File(htmlDir, "farm.html"),
                "$BASE_URL/locations" to File(htmlDir, "locations.html"),
                "$BASE_URL/leaderboard" to File(htmlDir, "leaderboard.html"),
                "$BASE_URL/faq" to File(htmlDir, "faq.html"),
                "$BASE_URL/manifest.webmanifest" to File(targetDir, "manifest.json")
            ).forEach {
                urlsToDownload.add(it)
            }

            _progress.value = _progress.value.copy(
                totalFiles = urlsToDownload.size,
                completedFiles = 0
            )

            // Скачиваем каждый ресурс
            urlsToDownload.forEachIndexed { index, (urlStr, destination) ->
                _progress.value = _progress.value.copy(
                    currentUrl = urlStr,
                    completedFiles = index,
                    statusMessage = "Скачивание [${index + 1}/${urlsToDownload.size}]: ${destination.name}"
                )

                try {
                    val bytes = downloadFile(urlStr, destination)
                    if (bytes > 0) {
                        totalBytes += bytes
                        downloadedFiles.add("${destination.parentFile.name}/${destination.name} (${bytes / 1024} KB)")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed downloading $urlStr: ${e.message}")
                }
            }

            // Создаем информационный README
            val readmeFile = File(targetDir, "README_COLORBIT.txt")
            readmeFile.writeText(
                """
                АРХИВ СОРЦОВ И МАГАЗИНОВ COLORBIT.RU
                Дата выгрузки: 2026-09-13
                
                Скачанные магазины:
                ${KNOWN_SHOPS.joinToString("\n") { "- ${it.name}: ${it.slug}.html (${it.fullUrl})" }}
                
                Папки архива:
                - /scripts: Все скомпилированные Vue/Inertia/React JS бандлы (app, vendor, RigItem, GPUSlots, RigSlot, Overclock)
                - /styles: CSS стили Tailwind и темы оформления Colorbit (#2B2B2B, #121212, #8000d7)
                - /shops: HTML разметки страниц всех 7 магазинов (DHS, Aliexprezz, SoftPortal, Гертруда, OnlyFans, ДомКлик, Avinto)
                - /pages: Игровые страницы фермы, локаций, рейтинга и FAQ.
                
                Всего файлов сохранено: ${downloadedFiles.size}
                Общий объем: ${totalBytes / 1024} KB
                """.trimIndent()
            )

            _progress.value = _progress.value.copy(
                isDownloading = false,
                completedFiles = urlsToDownload.size,
                downloadedBytes = totalBytes,
                statusMessage = "Успешно скачано ${downloadedFiles.size} файлов в папку Загрузки!",
                lastDownloadedFiles = downloadedFiles.takeLast(15)
            )

        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            _progress.value = _progress.value.copy(
                isDownloading = false,
                statusMessage = "Ошибка скачивания: ${e.localizedMessage ?: "Сбой соединения"}"
            )
        }
    }

    private fun fetchUrl(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; ColorbitDownloader)")
            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun downloadFile(urlString: String, destinationFile: File): Long {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; ColorbitDownloader)")
        if (conn.responseCode in 200..299) {
            conn.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    return input.copyTo(output)
                }
            }
        }
        return 0L
    }

    private fun extractRegexUrls(content: String, regexPattern: String): List<String> {
        val regex = Regex(regexPattern, RegexOption.IGNORE_CASE)
        return regex.findAll(content).mapNotNull {
            val raw = it.groupValues[1]
            if (raw.startsWith("http")) raw
            else if (raw.startsWith("/")) "$BASE_URL$raw"
            else null
        }.toList()
    }
}
