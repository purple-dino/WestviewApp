package compose.wvhs.wvhsapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIApplication
import platform.UIKit.UIScreen
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject


@OptIn(ExperimentalComposeUiApi::class)
@kotlinx.cinterop.ExperimentalForeignApi
@Composable
actual fun DisplayWebPage(webPage: String) {
    val webViewDelegate = remember { rememberWebViewDelegate() }

    UIKitView(
        factory = {
            val url = NSURL(string = webPage)
            val request = NSURLRequest(uRL = url)

            // Create the web view configuration
            val webViewConfiguration = WKWebViewConfiguration().apply {
                allowsInlineMediaPlayback = true
                allowsAirPlayForMediaPlayback = true
                allowsPictureInPictureMediaPlayback = true
            }

            // Initialize the web view with the configuration
            val webView = WKWebView(frame = UIScreen.mainScreen.bounds, configuration = webViewConfiguration)
            webView.navigationDelegate = webViewDelegate

            webView.apply {
                scrollView.scrollEnabled = true // Enable scrolling
                scrollView.delaysContentTouches = false // Set to false to register touches immediately
                loadRequest(request)
            }

            webView
        },
        modifier = Modifier.fillMaxSize(),
        properties = UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative)
    )
}

private fun rememberWebViewDelegate(): WKNavigationDelegateProtocol {
    return object : NSObject(), WKNavigationDelegateProtocol {
        override fun webView(
            webView: WKWebView,
            decidePolicyForNavigationAction: WKNavigationAction,
            decisionHandler: (WKNavigationActionPolicy) -> Unit
        ) {
            val navigationType = decidePolicyForNavigationAction.navigationType
            val request = decidePolicyForNavigationAction.request

            when (navigationType) {
                WKNavigationTypeLinkActivated -> {
                    // Check if the URL is not a local URL (i.e., it's an external link)
                    val urlString = request.URL?.absoluteString
                    if (urlString?.startsWith("http") == true) {
                        // Open the URL in the default browser
                        val url = NSURL(string = urlString)
                        runBlocking {
                            UIApplication.sharedApplication.openURL(url, options = mapOf<Any?, Any?>(), completionHandler = null)
                        }
                        return decisionHandler(platform.WebKit.WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    } else {
                        return decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                }
                else -> {
                    return decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                }
            }
        }
    }
}

