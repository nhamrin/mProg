package com.example.assg412

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.systemBarsPadding

/**
 * The class MainActivity
 *
 * This class starts the program by calling the WebViewApp function
 *
 * The program presents the user with five different website links. Once pressed, the app opens the
 * website and shows the content.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebViewApp()
        }
    }
}

/**
 * The function WebViewApp
 *
 * This function specifies which websites to showcase and creates five interactable buttons. Once
 * pressed, the button passes the correct URL to the WebViewScreen function.
 */
@Composable
fun WebViewApp() {
    var currentUrl by remember { mutableStateOf<String?>(null) }

    val websiteLinks = listOf(
        "https://www.google.com",
        "https://www.binarypiano.com",
        "https://www.svt.se/",
        "https://developer.android.com",
        "https://en.wikipedia.org/wiki/Main_Page"
    )

    if (currentUrl == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .systemBarsPadding(), //Add so that the app doesn't interfere with camera notch or system bar
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            websiteLinks.forEachIndexed { index, link ->
                Button(
                    onClick = { currentUrl = link },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(text = "Open Website ${index + 1}")
                }
            }
        }
    } else {
        WebViewScreen(url = currentUrl!!)
    }
}

/**
 * The function WebViewScreen
 *
 * This function uses the WebView application to display the chosen URL inside the app. There are
 * some extra settings used in an attempt to make certain websites more mobile friendly. The
 * function also includes some extra modifiers to ensure that no content on the website is blocked
 * by neither the status or navigation bar.
 */
@Composable
fun WebViewScreen(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true //Enable JavaScript, could be needed for some websites
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            loadUrl(url)
        }
    }, modifier = Modifier
        .fillMaxSize()
        //Also ensure that the web view doesn't interfere
        .statusBarsPadding()
        .navigationBarsPadding())
}
