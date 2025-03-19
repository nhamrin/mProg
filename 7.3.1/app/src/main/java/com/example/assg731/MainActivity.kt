package com.example.assg731

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * The class MainActivity
 *
 * This class starts the program by calling the InternetConnectivityApp function
 *
 * The program simply shows the user some text stating whether or not the device is connected to
 * the internet
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InternetConnectivityApp(this)
        }
    }
}

/**
 * The function InternetConnectivityApp
 *
 * This function will always call the function isInternetAvailable to check if the device is
 * connected to the internet. If it is, a text field will say that the phone is connected to the
 * internet, otherwise it will say that it isn't.
 */
@Composable
fun InternetConnectivityApp(context: Context) {
    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isConnected = isInternetAvailable(context)
            delay(2000) //Check every two seconds
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isConnected) "The phone is connected to the internet :)"
                   else "The phone is not connected to internet :(",
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * The function isInternetAvailable
 *
 * This function uses a system service asking via the connectivity service if the device has the
 * ability to be connected to the internet. By default this will return false, but once the device
 * has some internet connectivity, it will instead return true.
 */
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}