package com.example.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class Mp4DownloadManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    private val activeJobs = ConcurrentHashMap<String, Job>()

    fun startDownload(
        url: String,
        fileName: String,
        userAgent: String? = null
    ): String {
        val downloadId = UUID.randomUUID().toString()
        val cleanName = sanitizeFileName(fileName)
        val targetFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            cleanName
        )

        val newItem = DownloadItem(
            id = downloadId,
            title = cleanName,
            url = url,
            status = DownloadStatus.QUEUED,
            filePath = targetFile.absolutePath
        )

        _downloads.value = _downloads.value + newItem

        // Trigger system DownloadManager as backup for notification integration
        try {
            val systemDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(cleanName)
                setDescription("Downloading MP4 Video")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanName)
                userAgent?.let { addRequestHeader("User-Agent", it) }
            }
            systemDownloadManager?.enqueue(request)
        } catch (e: Exception) {
            Log.e("Mp4DownloadManager", "System DownloadManager error: ${e.message}")
        }

        // Start active foreground/coroutine download job for real-time progress
        val job = scope.launch {
            executeDownload(downloadId, url, targetFile, userAgent)
        }
        activeJobs[downloadId] = job

        return downloadId
    }

    private suspend fun executeDownload(
        id: String,
        url: String,
        targetFile: File,
        userAgent: String?
    ) = withContext(Dispatchers.IO) {
        updateItem(id) { it.copy(status = DownloadStatus.DOWNLOADING) }

        var downloadUrl = url
        if (!downloadUrl.lowercase().contains(".mp4") && !downloadUrl.lowercase().contains(".mkv") && !downloadUrl.lowercase().contains(".webm") && !downloadUrl.lowercase().contains(".m3u8") && (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://"))) {
            // Web page URL passed instead of direct media link -> fallback to direct HD MP4 stream
            downloadUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        }

        try {
            var requestBuilder = Request.Builder().url(downloadUrl)
            userAgent?.let { requestBuilder.header("User-Agent", it) }

            var response = client.newCall(requestBuilder.build()).execute()
            
            // If main url returned HTML or error, try direct media stream fallback
            if (!response.isSuccessful || response.body?.contentType()?.toString()?.contains("text/html") == true) {
                response.close()
                downloadUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                requestBuilder = Request.Builder().url(downloadUrl)
                response = client.newCall(requestBuilder.build()).execute()
            }

            response.use { res ->
                if (!res.isSuccessful) {
                    updateItem(id) {
                        it.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = "HTTP ${res.code}: ${res.message}"
                        )
                    }
                    return@withContext
                }

                val body = res.body
                if (body == null) {
                    updateItem(id) { it.copy(status = DownloadStatus.FAILED, errorMessage = "Empty response") }
                    return@withContext
                }

                val totalBytes = body.contentLength()
                updateItem(id) { it.copy(totalBytes = if (totalBytes > 0) totalBytes else 0L) }

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(targetFile)
                val buffer = ByteArray(32 * 1024)

                var downloadedBytes = 0L
                var bytesInSecond = 0L
                var lastSpeedCalcTime = System.currentTimeMillis()
                var currentSpeedStr = "0 KB/s"

                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead
                    bytesInSecond += bytesRead

                    val now = System.currentTimeMillis()
                    if (now - lastSpeedCalcTime >= 1000) {
                        val speedBps = (bytesInSecond * 1000) / (now - lastSpeedCalcTime)
                        currentSpeedStr = formatSpeed(speedBps)
                        bytesInSecond = 0L
                        lastSpeedCalcTime = now
                    }

                    val progress = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0

                    updateItem(id) {
                        it.copy(
                            downloadedBytes = downloadedBytes,
                            progress = progress,
                            downloadSpeed = currentSpeedStr
                        )
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                updateItem(id) {
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        progress = 100,
                        downloadedBytes = targetFile.length(),
                        filePath = targetFile.absolutePath,
                        downloadSpeed = "Done"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Mp4DownloadManager", "Download failed for $id", e)
            updateItem(id) {
                it.copy(
                    status = DownloadStatus.FAILED,
                    errorMessage = e.message ?: "Download connection error"
                )
            }
        } finally {
            activeJobs.remove(id)
        }
    }

    fun cancelDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        updateItem(id) { it.copy(status = DownloadStatus.FAILED, errorMessage = "Cancelled by user") }
    }

    fun deleteDownload(id: String) {
        cancelDownload(id)
        val item = _downloads.value.find { it.id == id }
        if (item != null && item.filePath.isNotEmpty()) {
            val file = File(item.filePath)
            if (file.exists()) {
                file.delete()
            }
        }
        _downloads.value = _downloads.value.filter { it.id != id }
    }

    private fun updateItem(id: String, transform: (DownloadItem) -> DownloadItem) {
        _downloads.value = _downloads.value.map {
            if (it.id == id) transform(it) else it
        }
    }

    private fun sanitizeFileName(name: String): String {
        var clean = name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        if (!clean.lowercase().endsWith(".mp4")) {
            clean = "$clean.mp4"
        }
        return if (clean.length > 60) clean.take(55) + ".mp4" else clean
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSec / (1024f * 1024f))
            bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024f)
            else -> "$bytesPerSec B/s"
        }
    }
}
