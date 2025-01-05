package compose.wvhs.wvhsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import compose.wvhs.wvhsapp.Utils.ContextHolder
import kotlinx.coroutines.launch

@Composable
actual fun DisplayWebPage(webPage: String) {
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val webView = remember { WebView(ContextHolder.contextProvider.getApplicationContext() as Context).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isLoading = true // Set loading to true when page starts loading
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                scope.launch {
                    isLoading = false // Set loading to false after the delay
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Check if the URL is not a local URL (i.e., it's an external link)
                if (request?.url?.toString()?.startsWith("http") == true) {
                    // Open the URL in the default browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.url.toString()))
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view?.context?.startActivity(intent)
                    return true // Prevent the WebView from loading the URL
                }
                return false // Let the WebView handle the URL loading
            }
        }
        settings.displayZoomControls = false
        settings.builtInZoomControls = true
        settings.useWideViewPort = true
    }}

    AndroidView(factory = { webView }) {
        it.loadUrl(webPage)
    }

    // Display loading indicator based on isLoading state
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(50.dp)
            )
        }
    }
}
