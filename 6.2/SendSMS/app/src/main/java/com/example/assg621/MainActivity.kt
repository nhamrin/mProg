package com.example.assg621

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
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

/**
 * The class MainActivity
 *
 * This class starts the program by calling the SMSSenderUI function
 *
 * The program has a simple graphical interface where the user can enter a phone number and a
 * message. It also has a button that will send this message to the specified phone number as a SMS
 * using the smsManager.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SMSSenderUI()
        }
    }
}

/**
 * The function SMSSenderUI
 *
 * This function requests the required permission, creates the two information fields and the
 * button with the text "Send SMS"
 */
@Composable
fun SMSSenderUI() {
    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    //NOTE: SEND_SMS is a restricted permission in (at least) Android 15+, the user needs to allow
    //this permission manually in the device's settings menu
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .statusBarsPadding()) { //As to not interfere with status icons and front camera
        TextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = message, onValueChange = { message = it }, label = { Text("Message") })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (phoneNumber.isNotBlank() && message.isNotBlank()) {
                sendSMS(context, phoneNumber, message) //Pass variables to sendSMS function
            } else {
                Toast.makeText(context, "Enter valid data", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Send SMS")
        }
    }
}

/**
 * The function sendSMS
 *
 * This function makes use of the smsManager to send the given message to the specified recipient
 * via a SMS. If the message doesn't get sent successfully, a error will be thrown.
 */
fun sendSMS(context: android.content.Context, phoneNumber: String, message: String) {
    try {
        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
