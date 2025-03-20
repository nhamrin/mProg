package com.example.assg52

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompassApp()
        }
    }
}

@Composable
fun CompassApp() {
    var pitch by remember { mutableStateOf("-") }
    var roll by remember { mutableStateOf("-") }
    var direction by remember { mutableStateOf("-") }
    val context = LocalContext.current

    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
        if (sensorManager == null) return //This seems to help with the application crashing at start
    val accelerometer = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magnetometer = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    val gravity = FloatArray(3) { 0f }
    val geomagnetic = FloatArray(3) { 0f }
    val rotationMatrix = FloatArray(9) { 0f }
    val orientationAngles = FloatArray(3) { 0f }

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                        }
                    }

                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat().toString()
                        roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat().toString()
                        direction = getDirectionFromAzimuth(azimuthDegrees)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(accelerometer, magnetometer) {
        accelerometer?.let { sensorManager?.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager?.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager?.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Direction: $direction", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Pitch: $pitch°", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Roll: $roll°", style = MaterialTheme.typography.headlineSmall)
    }
}

fun getDirectionFromAzimuth(azimuth: Float): String {
    val directions = listOf(
        "North", "North-East", "East", "South-East", "South", "South-West", "West", "North-West"
    )
    val index = ((azimuth + 22.5) / 45).toInt() % 8
    return "${directions[index]} ${azimuth.roundToInt()}°"
}
