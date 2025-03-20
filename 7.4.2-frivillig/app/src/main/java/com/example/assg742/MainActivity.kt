package com.example.assg742

import android.content.Context
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * The class MainActivity
 *
 * This class starts the program by calling the SensorDataDisplay function
 *
 * This program has a simple UI that showcases the light level, ambient temperature and relative
 * humidity with data gathered from the device's sensors
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SensorDataDisplay(sensorViewModel = SensorViewModel(applicationContext))
            }
        }
    }
}

/**
 * The class SensorViewModel
 *
 * This class houses the view model for the program. It initialises the used variables and sensors
 * and assigns them to the sensor manager. When some value from one of the sensor changes, this
 * will update in the onSensorChanged function.
 */
class SensorViewModel(context: Context) : ViewModel(), SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val _lightLevel = mutableStateOf(0f)
    private val _temperature = mutableStateOf<Float?>(null)
    private val _humidityLevel = mutableStateOf<Float?>(null)

    val lightLevel: State<Float> = _lightLevel
    val temperature: MutableState<Float?> = _temperature
    val humidityLevel: MutableState<Float?> = _humidityLevel

    private var lightSensor: Sensor? = null
    private var temperatureSensor: Sensor? = null
    private var humiditySensor: Sensor? = null

    init {
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)

        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        temperatureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        humiditySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                _lightLevel.value = event.values[0]
            }
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                _temperature.value = event.values[0]
            }
            Sensor.TYPE_RELATIVE_HUMIDITY -> {
                _humidityLevel.value = event.values[0]
            }
        }
    }

    //Not needed but will throw error unless this is here...
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}

/**
 * The function SensorDataDisplay
 *
 * This function showcases the gathered data in a simple visual interface. It also checks if the
 * app has registered any input from the specific sensors. If not, instead of displaying an
 * arbitrary value, it will say that that specific metric wasn't available. When testing, this
 * could be the case for both temperature and humidity.
 */
@Composable
fun SensorDataDisplay(sensorViewModel: SensorViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Light Level: ${sensorViewModel.lightLevel.value} Lux", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        sensorViewModel.temperature.value?.let {
            Text(text = "Temperature: $it °C", style = MaterialTheme.typography.titleLarge)
        } ?: run {
            Text(text = "Temperature: Not Available", style = MaterialTheme.typography.titleLarge)
        }

        sensorViewModel.humidityLevel.value?.let {
            Text(text = "Humidity Level: $it%", style = MaterialTheme.typography.titleLarge)
        } ?: run {
            Text(text = "Humidity: Not Available", style = MaterialTheme.typography.titleLarge)
        }
    }
}
