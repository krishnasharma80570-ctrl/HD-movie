package com.example.adblock

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Message
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

import android.webkit.URLUtil
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults

class AdBlockJsBridge(
    private val onAdBlocked: () -> Unit,
    private val onElementsRemoved: (Int) -> Unit,
    private val onVideoFound: (String, String) -> Unit
) {
    @JavascriptInterface
    fun onAdBlocked(type: String) {
        onAdBlocked()
    }

    @JavascriptInterface
    fun onAdElementsRemoved(count: Int) {
        onElementsRemoved(count)
    }

    @JavascriptInterface
    fun onVideoFound(url: String, title: String) {
        onVideoFound(url, title)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdBlockWebView(
    url: String,
    modifier: Modifier = Modifier,
    showAddressBar: Boolean = false,
    onTitleChanged: (String) -> Unit = {},
    onAdCountUpdated: (Int) -> Unit = {},
    onDownloadRequested: (url: String, fileName: String, userAgent: String?) -> Unit = { _, _, _ -> },
    onVideoDetected: (url: String, title: String) -> Unit = { _, _ -> }
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf(url) }
    var lastLoadedUrl by remember { mutableStateOf("") }
    var blockedAdCount by remember { mutableIntStateOf(0) }
    var pageTitle by remember { mutableStateOf("HD Stream Player") }
    var detectedMp4Url by remember { mutableStateOf<String?>(null) }

    fun loadStreamUrl(targetWebView: WebView, targetUrl: String) {
        targetWebView.loadUrl(targetUrl)
    }

    fun incrementAdCount(count: Int = 1) {
        blockedAdCount += count
        onAdCountUpdated(blockedAdCount)
    }

    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        if (showAddressBar) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF181920),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { webViewRef?.goBack() },
                        enabled = webViewRef?.canGoBack() == true
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (webViewRef?.canGoBack() == true) Color.White else Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { webViewRef?.goForward() },
                        enabled = webViewRef?.canGoForward() == true
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Forward",
                            tint = if (webViewRef?.canGoForward() == true) Color.White else Color.Gray
                        )
                    }

                    IconButton(onClick = { webViewRef?.reload() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF232530)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "AdBlock Protection",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.height(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HD Cinema Stream",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Ad counter pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.height(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$blockedAdCount Ads",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                progress = { loadingProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Color(0xFFE50914),
                trackColor = Color(0xFF232530)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = false
                            allowContentAccess = false
                            setSupportMultipleWindows(false)
                            javaScriptCanOpenWindowsAutomatically = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        }

                        addJavascriptInterface(
                            AdBlockJsBridge(
                                onAdBlocked = { incrementAdCount(1) },
                                onElementsRemoved = { count -> incrementAdCount(count) },
                                onVideoFound = { videoUrl, videoTitle ->
                                    detectedMp4Url = videoUrl
                                    onVideoDetected(videoUrl, videoTitle)
                                }
                            ),
                            "AndroidAdBlock"
                        )

                        setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
                            var fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
                            if (fileName.isBlank() || fileName == "downloadfile" || !fileName.endsWith(".mp4")) {
                                fileName = "${pageTitle.replace("[^a-zA-Z0-9]".toRegex(), "_")}.mp4"
                            }
                            onDownloadRequested(downloadUrl, fileName, userAgent)
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val requestUrl = request?.url?.toString()
                                if (AdBlockEngine.isAdUrl(requestUrl)) {
                                    incrementAdCount(1)
                                    // Return empty response to block the ad resource
                                    return WebResourceResponse(
                                        "text/plain",
                                        "utf-8",
                                        ByteArrayInputStream(ByteArray(0))
                                    )
                                }

                                if (requestUrl != null) {
                                    val lower = requestUrl.lowercase()
                                    if (lower.contains(".mp4") || lower.contains(".mkv") || lower.contains(".webm")) {
                                        detectedMp4Url = requestUrl
                                        onVideoDetected(requestUrl, pageTitle)
                                    }
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val targetUrl = request?.url?.toString() ?: return false
                                if (AdBlockEngine.isAdUrl(targetUrl)) {
                                    incrementAdCount(1)
                                    return true // Block redirect
                                }
                                currentUrl = targetUrl
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                url?.let { currentUrl = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                // Inject ad blocking JavaScript
                                view?.evaluateJavascript(AdBlockEngine.adBlockScript, null)
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadingProgress = newProgress
                                if (newProgress > 50) {
                                    view?.evaluateJavascript(AdBlockEngine.adBlockScript, null)
                                }
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                title?.let {
                                    pageTitle = it
                                    onTitleChanged(it)
                                }
                            }

                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: Message?
                            ): Boolean {
                                // Block popup windows!
                                incrementAdCount(1)
                                return false
                            }
                        }

                        lastLoadedUrl = url
                        currentUrl = url
                        loadStreamUrl(this, url)
                    }
                },
                update = { view ->
                    if (lastLoadedUrl != url && !url.isBlank()) {
                        lastLoadedUrl = url
                        currentUrl = url
                        loadStreamUrl(view, url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            detectedMp4Url?.let { mediaUrl ->
                FloatingActionButton(
                    onClick = {
                        val name = "${pageTitle.replace("[^a-zA-Z0-9]".toRegex(), "_")}.mp4"
                        onDownloadRequested(mediaUrl, name, webViewRef?.settings?.userAgentString)
                    },
                    containerColor = Color(0xFFE50914),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("floating_download_mp4_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download MP4 Video",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download MP4",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
            webViewRef?.destroy()
        }
    }
}
