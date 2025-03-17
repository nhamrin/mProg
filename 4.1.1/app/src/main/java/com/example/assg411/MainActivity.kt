package com.example.assg411

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * The class MainActivity
 *
 * This class starts the program by calling the YouTubeLauncherApp function
 *
 * The program presents the user with five different YouTube links. Once pressed, the app opens the
 * YouTube app and plays the specified video.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YouTubeLauncherApp()
        }
    }
}

/**
 * The function YouTubeLauncherApp
 *
 * This function specifies which videos to showcase and creates the five interactable buttons. Once
 * pressed, the button passes the context created in this function to the openYouTube function.
 */
@Composable
fun YouTubeLauncherApp() {
    val context = LocalContext.current

    //Choose what videos and in what order
    val videoLinks = listOf(
        "https://www.youtube.com/watch?v=QIvtyzCYJDg",
        "https://www.youtube.com/watch?v=DHfRfU3XUEo",
        "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
        "https://www.youtube.com/watch?v=DQjlcbLdWio",
        "https://www.youtube.com/watch?v=8C8j63R7alQ"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        videoLinks.forEachIndexed { index, link ->
            Button(
                onClick = { openYouTube(context, link) }, //Pass context to function
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text(text = "Play Video ${index + 1}")
            }
        }
    }
}

/**
 * The function openYouTube
 *
 * This function uses a Intent to open the YouTube app and play the requested video
 */
fun openYouTube(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent) //Use context to start activity
}
