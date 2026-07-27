package com.example.download

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

data class DownloadItem(
    val id: String,
    val title: String,
    val url: String,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val filePath: String = "",
    val downloadSpeed: String = "0 KB/s",
    val mimeType: String = "video/mp4",
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
