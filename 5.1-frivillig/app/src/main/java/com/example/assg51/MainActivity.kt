package com.example.assg51

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient

/**
 * The class MainActivity
 *
 * This class starts the program by calling the GPSLocationApp function. It also initialises the
 * location client via a location service.
 *
 * The app uses the device's GPS and showcases the data to the user in a simple UI. The chosen
 * attributes from the GPS are: Latitude, longitude, speed, altitude and accuracy.
 */
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            GPSLocationApp(fusedLocationClient)
        }
    }
}

/**
 * The function GPSLocationApp
 *
 * This function initialises the variables used to display GPS attributes, makes sure that the
 * needed permissions are set and creates the visual user interface to display these attributes.
 * It makes use of a location call back object to continuously update the GPS-values with a small
 * interval delay.
 */
@Composable
fun GPSLocationApp(fusedLocationClient: FusedLocationProviderClient) {
    var latitude by remember { mutableStateOf("-") }
    var longitude by remember { mutableStateOf("-") }
    var speed by remember { mutableStateOf("-") }
    var altitude by remember { mutableStateOf("-") }
    var accuracy by remember { mutableStateOf("-") }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    latitude = it.latitude.toString()
                    longitude = it.longitude.toString()
                    speed = it.speed.toString()
                    altitude = it.altitude.toString()
                    accuracy = it.accuracy.toString()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.Builder(500)
                .setMinUpdateIntervalMillis(500)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Latitude: $latitude", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Longitude: $longitude", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Speed: $speed m/s", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Altitude: $altitude meters", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Accuracy: $accuracy meters", style = MaterialTheme.typography.headlineSmall)
    }
}