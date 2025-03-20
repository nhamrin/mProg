package com.example.assg9

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSMapApp()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GPSMapApp() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationState = remember { mutableStateOf(LatLng(37.7749, -122.4194)) } // Default SF location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(locationState.value, 15f)
    }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val directionState = remember { mutableStateOf(0f) }
    val lightLevelState = remember { mutableStateOf(0f) }

    if (locationPermissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            startLocationUpdates(fusedLocationClient) { location ->
                val newLocation = LatLng(location.latitude, location.longitude)
                locationState.value = newLocation
            }
        }
    } else {
        SideEffect { locationPermissionState.launchPermissionRequest() }
    }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    val sensorEventListener = rememberUpdatedState(object : SensorEventListener {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Update accelerometer data
                System.arraycopy(event.values, 0, rotationMatrix, 0, 3)
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                // Update magnetic field data
                System.arraycopy(event.values, 0, rotationMatrix, 3, 3)
            }

            if (rotationMatrix.size == 9) {
                // Calculate azimuth (direction)
                SensorManager.getOrientation(rotationMatrix, orientation)
                directionState.value = Math.toDegrees(orientation[0].toDouble()).toFloat()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    })

    val lightSensorEventListener = rememberUpdatedState(object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null && event.sensor.type == Sensor.TYPE_LIGHT) {
                lightLevelState.value = event.values[0]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    })

    LaunchedEffect(Unit) {
        sensorManager.registerListener(sensorEventListener.value, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener.value, magneticField, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(lightSensorEventListener.value, lightSensor, SensorManager.SENSOR_DELAY_UI)
    }

    //This is probably good to have
//    onDispose {
//        sensorManager.unregisterListener(sensorEventListener.value)
//        sensorManager.unregisterListener(lightSensorEventListener.value)
//    }

    // Ensure the camera follows the updated location
    LaunchedEffect(locationState.value) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(locationState.value, 15f))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = locationState.value),
                    title = "Your Location"
                )
            }

            // Display current coordinates in the lower fifth of the screen
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.White) // Semi-transparent background
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Lat: ${locationState.value.latitude}, Lng: ${locationState.value.longitude}",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Direction: ${directionState.value}°",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Light Level: ${lightLevelState.value} lux",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

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
