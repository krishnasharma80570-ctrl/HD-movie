package com.example.adblock

import android.net.Uri
import android.util.Log

object AdBlockEngine {

    private val blockedDomains = setOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "exoclick.com",
        "propellerads.com",
        "adsterra.com",
        "popads.net",
        "popcash.net",
        "bet365.com",
        "1xbet.com",
        "stake.com",
        "juicyads.com",
        "onclickads.net",
        "trafficjunky.com",
        "adroll.com",
        "outbrain.com",
        "taboola.com",
        "zeroredirect.com",
        "criteo.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "adnxs.com",
        "smartadserver.com",
        "amazon-adsystem.com",
        "adform.net",
        "bidswitch.net",
        "scorecardresearch.com",
        "quantserve.com",
        "adsystem",
        "pagead",
        "adserver",
        "popunder",
        "pop_under",
        "betting",
        "casino"
    )

    fun isAdUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lowerUrl = url.lowercase()

        // Check if URL contains known ad keywords
        if (lowerUrl.contains("/ad/") || lowerUrl.contains("/ads/") || 
            lowerUrl.contains("popunder") || lowerUrl.contains("popup") ||
            lowerUrl.contains("banner") || lowerUrl.contains("adsterra") ||
            lowerUrl.contains("exoclick") || lowerUrl.contains("bet365")
        ) {
            return true
        }

        return try {
            val host = Uri.parse(lowerUrl).host ?: return false
            blockedDomains.any { domain ->
                host.equals(domain, ignoreCase = true) || host.endsWith(".$domain", ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
    }

    val adBlockScript: String = """
        (function() {
            // Prevent window.open popups
            window.open = function(url, name, features) {
                console.log('Blocked popup window open to:', url);
                if (window.AndroidAdBlock) {
                    window.AndroidAdBlock.onAdBlocked('Popup Window');
                }
                return null;
            };

            // Remove target="_blank" to prevent opening ad tabs
            function neutralizeLinks() {
                var links = document.querySelectorAll('a[target="_blank"]');
                for (var i = 0; i < links.length; i++) {
                    links[i].removeAttribute('target');
                }
            }

            // Remove common ad containers and overlay banners
            function removeAdElements() {
                var selectors = [
                    '.popunder', '#popunder', '.ad-box', '#ad-box',
                    '.vignette', '#vignette', '.floating-ad', '#overlay-ad',
                    'iframe[src*="adsterra"]', 'iframe[src*="exoclick"]', 'iframe[src*="popads"]',
                    'div[id*="popunder"]', 'div[class*="popunder"]',
                    'div[style*="z-index: 99999"]', 'div[style*="z-index:2147483647"]'
                ];
                
                var removedCount = 0;
                selectors.forEach(function(selector) {
                    try {
                        var elements = document.querySelectorAll(selector);
                        elements.forEach(function(el) {
                            // Don't remove main video player containers or embeds
                            if (!el.querySelector('video') && !el.querySelector('iframe') && !el.id.includes('player') && !el.className.includes('player')) {
                                el.remove();
                                removedCount++;
                            }
                        });
                    } catch(e) {}
                });

                if (removedCount > 0 && window.AndroidAdBlock) {
                    window.AndroidAdBlock.onAdElementsRemoved(removedCount);
                }
            }

            // Auto-unmute videos if ad-block caused mute
            function unmuteVideo() {
                var videos = document.querySelectorAll('video');
                videos.forEach(function(v) {
                    v.muted = false;
                    v.playsInline = true;
                });
            }

            // Sniff MP4 and stream video elements
            function sniffVideos() {
                var mediaElements = document.querySelectorAll('video, source, a[href*=".mp4"], a[href*="download"]');
                mediaElements.forEach(function(el) {
                    var src = el.src || el.href;
                    if (src && src.startsWith('http')) {
                        if (src.includes('.mp4') || src.includes('.m3u8') || src.includes('.mkv') || src.includes('video')) {
                            if (window.AndroidAdBlock && window.AndroidAdBlock.onVideoFound) {
                                window.AndroidAdBlock.onVideoFound(src, document.title || 'HD Video');
                            }
                        }
                    }
                });
            }

            // Execute immediately and periodically
            neutralizeLinks();
            removeAdElements();
            sniffVideos();
            setInterval(function() {
                neutralizeLinks();
                removeAdElements();
                sniffVideos();
                unmuteVideo();
            }, 800);
        })();
    """.trimIndent()
}
