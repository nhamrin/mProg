package com.example.assg722

import android.content.Context
import android.os.VibratorManager
import android.os.Bundle
import android.os.VibrationEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.assg722.ui.theme.Assg722Theme

/**
 * The class MainActivity
 *
 * This class starts the program by calling the VibrationButtons function
 *
 * The program has a very simple UI with two buttons that each make the phone vibrate. One creates
 * a short vibration and he other a longer vibration.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Assg722Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VibrationButtons()
                }
            }
        }
    }
}

/**
 * The function VibrationButtons
 *
 * This function uses the compose elements to create two simple buttons with descriptive text. Once
 * the user presses any of these two buttons, the phone will vibrate. The length of the vibration
 * is decided depending on which button is pressed.
 */
@Composable
fun VibrationButtons() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { vibrate(context, 100) }, //100ms
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Short vibrate")
        }
        Button(
            onClick = { vibrate(context, 2000) }, //2000ms
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Long vibrate")
        }
    }
}

/**
 * The function vibrate
 *
 * This function controls the vibration using a vibration manager. The context and duration is
 * passed into this function from the VibrationButtons function.
 */
private fun vibrate(context: Context, durationMs: Long) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    val vibrator = vibratorManager.defaultVibrator
    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
}