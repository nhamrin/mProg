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
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YouTubeLauncherApp()
        }
    }
}

@Composable
fun YouTubeLauncherApp() {
    val context = LocalContext.current

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
                onClick = { openYouTube(context, link) },  //Pass context to function
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text(text = "Play Video ${index + 1}")
            }
        }
    }
}

fun openYouTube(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)  //Use context to start activity
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    YouTubeLauncherApp()
}
