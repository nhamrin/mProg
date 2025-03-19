package com.example.assg36

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assg36.ui.theme.Assg36Theme

/**
 * The class MainActivity
 *
 * This class starts the program by calling on the function TextToSpeechScreen. When it calls this
 * function, it also passes an instance of ttsHelper which is created earlier from the file
 * TextToSpeechHelper.
 */
class MainActivity : ComponentActivity() {
    private lateinit var ttsHelper: TextToSpeechHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttsHelper = TextToSpeechHelper(this)

        setContent {
            Assg36Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TextToSpeechScreen(ttsHelper = ttsHelper)
                }
            }
        }
    }

    override fun onDestroy() {
        ttsHelper.release()
        super.onDestroy()
    }
}

/**
 * The function TextToSpeechScreen
 *
 * This function draws up a simple UI using compose functions. It has a text field, where the user
 * can write what is to be spoken back to them, with a label and a button which activates the
 * TTS function. This function is activated by calling the passed ttsHelper.
 */
@Composable
fun TextToSpeechScreen(ttsHelper: TextToSpeechHelper) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Skriv något på svenska") } //Prompt the user to write in Swedish
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { ttsHelper.speak(text) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tala")
        }
    }
}