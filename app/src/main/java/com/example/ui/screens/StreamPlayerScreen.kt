package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adblock.AdBlockWebView
import com.example.data.MovieItem
import com.example.ui.theme.CinemaAdGreen
import com.example.ui.theme.CinemaRed

@Composable
fun StreamPlayerScreen(
    movie: MovieItem,
    streamUrl: String,
    onClose: () -> Unit,
    onAdCountUpdated: (Int) -> Unit,
    onDownloadRequested: (url: String, fileName: String, userAgent: String?) -> Unit = { _, _, _ -> }
) {
    var blockedCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("stream_player_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Player Top Header Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F1017)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF20222E))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Stream",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = movie.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "HD Stream Engine • AdBlock ACTIVE",
                                color = CinemaAdGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // AdBlock Counter Pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CinemaAdGreen.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Blocked Ads",
                                tint = CinemaAdGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$blockedCount Ads Blocked",
                                color = Color(0xFF81C784),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // WebView Stream Player Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AdBlockWebView(
                    url = streamUrl,
                    showAddressBar = false,
                    onAdCountUpdated = { count ->
                        blockedCount = count
                        onAdCountUpdated(count)
                    },
                    onDownloadRequested = onDownloadRequested,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Player Bottom Status Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F1017)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = CinemaRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = movie.quality,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Source: Cinema HD Engine",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "Popups & Redirects Suppressed",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
