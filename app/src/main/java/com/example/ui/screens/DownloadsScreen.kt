package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.download.DownloadItem
import com.example.download.DownloadStatus
import com.example.ui.theme.CinemaAdGreen
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.viewmodel.MovieViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: MovieViewModel,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloads.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var customNameInput by remember { mutableStateOf("") }
    var playingFile by remember { mutableStateOf<DownloadItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0E))
            .padding(top = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = CinemaRed,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "MP4 Downloader",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${downloads.size} Files • High Speed MP4 Engine",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Link",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Link",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (downloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No MP4 Downloads Yet",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Browse live stream site to detect MP4 videos, tap Download on any movie, or paste a video URL directly to start downloading.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(downloads, key = { it.id }) { item ->
                    DownloadCard(
                        item = item,
                        onPlay = { playingFile = item },
                        onCancel = { viewModel.cancelDownload(item.id) },
                        onDelete = { viewModel.deleteDownload(item.id) }
                    )
                }
            }
        }
    }

    // Custom URL Download Dialog Sheet
    if (showAddDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF141620)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Download MP4 Video URL",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customUrlInput,
                    onValueChange = { customUrlInput = it },
                    placeholder = { Text("https://example.com/movie.mp4", color = Color.Gray, fontSize = 13.sp) },
                    label = { Text("MP4 / Video URL", color = CinemaRed) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CinemaRed,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customNameInput,
                    onValueChange = { customNameInput = it },
                    placeholder = { Text("Movie Title (Optional)", color = Color.Gray, fontSize = 13.sp) },
                    label = { Text("File Name", color = CinemaRed) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CinemaRed,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            val name = if (customNameInput.isNotBlank()) customNameInput else "HD_Movie_${System.currentTimeMillis()}.mp4"
                            viewModel.startMp4Download(customUrlInput.trim(), name)
                            showAddDialog = false
                            customUrlInput = ""
                            customNameInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("START MP4 DOWNLOAD", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Built-in Local MP4 Video Player Dialog
    playingFile?.let { item ->
        val file = File(item.filePath)
        ModalBottomSheet(
            onDismissRequest = { playingFile = null },
            containerColor = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(Color.Black)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { playingFile = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                if (file.exists()) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(Uri.fromFile(file))
                                setMediaController(android.widget.MediaController(ctx).apply {
                                    setAnchorView(this@apply)
                                })
                                start()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("File not found on storage", color = Color.Red, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadCard(
    item: DownloadItem,
    onPlay: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag("download_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141620))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.url,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Status Badge
                Surface(
                    color = when (item.status) {
                        DownloadStatus.COMPLETED -> CinemaAdGreen.copy(alpha = 0.2f)
                        DownloadStatus.DOWNLOADING -> CinemaRed.copy(alpha = 0.2f)
                        DownloadStatus.FAILED -> Color.Red.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.status.name,
                        color = when (item.status) {
                            DownloadStatus.COMPLETED -> CinemaAdGreen
                            DownloadStatus.DOWNLOADING -> CinemaRed
                            DownloadStatus.FAILED -> Color.Red
                            else -> Color.LightGray
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { item.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (item.status == DownloadStatus.COMPLETED) CinemaAdGreen else CinemaRed,
                trackColor = Color(0xFF242738)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.progress}% • ${formatBytes(item.downloadedBytes)} / ${formatBytes(item.totalBytes)}",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                if (item.status == DownloadStatus.DOWNLOADING) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.downloadSpeed,
                            color = CinemaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (item.errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Error: ${item.errorMessage}",
                    color = Color.Red,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.status == DownloadStatus.COMPLETED) {
                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaAdGreen),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play MP4",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY MP4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (item.status == DownloadStatus.DOWNLOADING) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32364A)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Cancel", fontSize = 11.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes / (1024f * 1024f)
    return if (mb >= 1000) {
        String.format("%.2f GB", mb / 1024f)
    } else {
        String.format("%.1f MB", mb)
    }
}
