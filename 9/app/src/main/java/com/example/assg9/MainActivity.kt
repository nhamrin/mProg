package com.example.assg9

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.io.File

/**
 * The class MainActivity
 *
 * This class starts the program by calling the GPSMapApp function
 *
 * This program is an app for outdoors people that want direction and environment information when
 * exploring on foot without explicit navigation help. The map which is displayed on the majority
 * of the screen shows the user what's around them, while the bottom portion showcases relevant
 * direction and environment information. There is also a simple profile function where the user
 * can enter and update their name.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSMapApp()
        }
    }
}

/**
 * The GPSMapApp function
 *
 * This function controls pretty much the whole app by using earlier techniques in the course,
 * combined with some new ones. Used techniques are: Sensors (temperature, light, acceleration and
 * magnetic field), GPS, map and a drawer along with Compose components such as box, column, text
 * and button. Documentation for the individual parts of the function is written as comments at
 * these specific parts.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GPSMapApp() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationState = remember { mutableStateOf(LatLng(59.326801791179804, 18.071768430989838)) } //Default location, royal castle in Stockholm
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(locationState.value, 16f) //Set camera position, 16 in zoom determined by user testing
    }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    //Set up the values that are used in the app
    val directionState = remember { mutableStateOf(0f) }
    val lightLevelState = remember { mutableStateOf(0f) }
    val temperatureState = remember { mutableStateOf<Float?>(null) }
    val profileNameState = remember { mutableStateOf("") }
    val newProfileNameState = remember { mutableStateOf("") }
    val isProfileDrawerOpen = remember { mutableStateOf(false) }

    //Read user name (if it exists)
    LaunchedEffect(Unit) {
        val file = File(context.filesDir, "profile.txt")
        if (file.exists()) {
            profileNameState.value = file.readText()
        }
    }

    /**
     * The function saveProfileName
     *
     * This function saves the profile name to the text file "profile.txt" which can later be read
     * from to display the saved profile name
     */
    fun saveProfileName(name: String) {
        val file = File(context.filesDir, "profile.txt")
        file.writeText(name)
        profileNameState.value = name
        newProfileNameState.value = ""
    }

    //Check if location permission has been granted
    if (locationPermissionState.status.isGranted) { //If it has, start fetching location updates
        LaunchedEffect(Unit) {
            startLocationUpdates(fusedLocationClient) { location ->
                val newLocation = LatLng(location.latitude, location.longitude)
                locationState.value = newLocation
            }
        }
    } else { //If not, request permission
        SideEffect { locationPermissionState.launchPermissionRequest() }
    }

    //Set up the sensors that are going to be used
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    //Listen to the accelerometer and magnetic field sensors to update direction
    val sensorEventListener = rememberUpdatedState(object : SensorEventListener {
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0 ,3)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                }
            }

            if (gravity.isNotEmpty() && geomagnetic.isNotEmpty()) {
                val rotation = FloatArray(9)
                val incline = FloatArray(9)
                if (SensorManager.getRotationMatrix(rotation, incline, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotation, orientation)
                    directionState.value = Math.toDegrees(orientation[0].toDouble()).toFloat()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    })

    //Listen to the light sensor to update the brightness value
    val lightSensorEventListener = rememberUpdatedState(object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null && event.sensor.type == Sensor.TYPE_LIGHT) {
                lightLevelState.value = event.values[0]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    })

    //Listen to the temperature sensor to update temperature value
    val temperatureSensorEventListener = rememberUpdatedState(object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null && event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                temperatureState.value = event.values[0]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    })

    //On launch, register the listeners for each of the sensors
    LaunchedEffect(Unit) {
        sensorManager.registerListener(sensorEventListener.value, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener.value, magneticField, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(lightSensorEventListener.value, lightSensor, SensorManager.SENSOR_DELAY_UI)
        if (temperatureSensor != null) { //A lot devices doesn't have a temperature sensor, therefore check first before registering the listener
            sensorManager.registerListener(temperatureSensorEventListener.value, temperatureSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    //On dispose, unregister the listeners for each of the sensors
    DisposableEffect(Unit) {
        onDispose {
            sensorManager.unregisterListener(sensorEventListener.value)
            sensorManager.unregisterListener(lightSensorEventListener.value)
            sensorManager.unregisterListener(temperatureSensorEventListener.value)
        }
    }

    //Ensure the camera follows the updated location
    LaunchedEffect(locationState.value) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(locationState.value, 16f))
    }

    //This is the user interface
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar( //Used not for the title, but for the profile button
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    if (!isProfileDrawerOpen.value) {
                        Button(onClick = { isProfileDrawerOpen.value = true }) {
                            Text("Profile")
                        }
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker( //Create a marker for the user's location on the map
                    state = MarkerState(position = locationState.value),
                    title = "Your Location"
                )
            }

            //Display direction and environment information on the lower part of the screen
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Lat: ${locationState.value.latitude}, Lng: ${locationState.value.longitude}",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Direction: ${getDirection(directionState.value)}, ${directionState.value.toInt()}°",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Light Level: ${lightLevelState.value} lux",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (temperatureSensor != null)
                        "Temperature: ${temperatureState.value ?: "Loading..."}°C"
                    else "Temperature: Sensor Not Available",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            //The profile drawer
            if (isProfileDrawerOpen.value) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .background(Color.White)
                        .align(Alignment.CenterEnd)
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        Text("Profile Name:", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = profileNameState.value,
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text("Enter New Profile Name:")
                        TextField(
                            value = newProfileNameState.value,
                            onValueChange = { newProfileNameState.value = it },
                            singleLine = true
                        )
                        Button(onClick = {
                            saveProfileName(newProfileNameState.value)
                        }) {
                            Text("Save")
                        }
                        Button(onClick = {
                            isProfileDrawerOpen.value = false
                        }) {
                            Text("Close profile")
                        }
                    }
                }
            }
        }
    }
}

/**
 * The function startLocationUpdates
 *
 * This function request new location updates using the client FusedLocationProviderClient. When
 * the position is updated, the last known location is also updated.
 */
@SuppressLint("MissingPermission")
fun startLocationUpdates(client: FusedLocationProviderClient, onUpdate: (Location) -> Unit) {
    val request = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 2000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    client.requestLocationUpdates(request, object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { onUpdate(it) }
        }
    }, null)
}

/**
 * The function getDirection
 *
 * This function converts the degrees acquired by the accelerometer and magnetic field sensors
 * and converts them into a direction
 */
fun getDirection(azimuth: Float): String {
    val degrees = (azimuth + 360) % 360
    return when {
        degrees >= 0 && degrees < 22.5 -> "North"
        degrees >= 22.5 && degrees < 67.5 -> "North-East"
        degrees >= 67.5 && degrees < 112.5 -> "East"
        degrees >= 112.5 && degrees < 157.5 -> "South-East"
        degrees >= 157.5 && degrees < 202.5 -> "South"
        degrees >= 202.5 && degrees < 247.5 -> "South-West"
        degrees >= 247.5 && degrees < 292.5 -> "West"
        degrees >= 292.5 && degrees < 337.5 -> "North-West"
        else -> "North"
    }
}
