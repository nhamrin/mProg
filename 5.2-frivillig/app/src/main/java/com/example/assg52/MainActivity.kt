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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assg52.ui.theme.Assg52Theme

/**
 * The class MainActivity
 *
 * This class initialises variables and starts the program by calling the function CompassDisplay.
 * It also sets up the sensors and registers them to the sensor manager.
 *
 * This program uses a simple UI to show the user what direction the device is facing, along with
 * information such as pitch and yaw. This resembles a real life compass and the data is gathered
 * from the device's compass using the accelerometer and magnetic field sensors.
 */
class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var magneticSensor: Sensor
    private lateinit var accelerometerSensor: Sensor

    private lateinit var accelerometerData: FloatArray
    private lateinit var magneticData: FloatArray

    private var azimuth by mutableStateOf(0f)
    private var pitch by mutableStateOf(0f)
    private var roll by mutableStateOf(0f)
    private var direction by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Assg52Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CompassDisplay(azimuth, pitch, roll, direction)
                }
            }
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!

        accelerometerData = FloatArray(3)
        magneticData = FloatArray(3)

        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener, magneticSensor, SensorManager.SENSOR_DELAY_UI)
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            //Update accelerometer and magnetic data
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accelerometerData = event.values
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magneticData = event.values
                }
            }

            //Calculate azimuth, pitch, and roll when both sensor data are available
            if (accelerometerData != null && magneticData != null) {
                val r = FloatArray(9)
                val i = FloatArray(9)

                if (SensorManager.getRotationMatrix(r, i, accelerometerData, magneticData)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)

                    azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                    azimuth = (azimuth + 360) % 360 //This might not be necessary?

                    direction = getDirectionFromAzimuth(azimuth)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }

    /**
     * The function getDirectionFromAzimuth
     *
     * This function uses the degree from azimuth and translates it into a direction
     */
    private fun getDirectionFromAzimuth(azimuth: Float): String {
        return when {
            azimuth >= 0 && azimuth < 22.5 -> "North"
            azimuth >= 22.5 && azimuth < 67.5 -> "North-East"
            azimuth >= 67.5 && azimuth < 112.5 -> "East"
            azimuth >= 112.5 && azimuth < 157.5 -> "South-East"
            azimuth >= 157.5 && azimuth < 202.5 -> "South"
            azimuth >= 202.5 && azimuth < 247.5 -> "South-West"
            azimuth >= 247.5 && azimuth < 292.5 -> "West"
            azimuth >= 292.5 && azimuth < 337.5 -> "North-West"
            else -> "North"
        }
    }
}

/**
 * The function CompassDisplay
 *
 * This function uses the built-in compose library to showcase four different text boxes with
 * information from the compass application
 */
@Composable
fun CompassDisplay(azimuth: Float, pitch: Float, roll: Float, direction: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Azimuth (Direction): %.2f°".format(azimuth))
        Text("Pitch: %.2f°".format(pitch))
        Text("Roll: %.2f°".format(roll))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Direction: $direction", style = MaterialTheme.typography.headlineMedium)
    }
}

