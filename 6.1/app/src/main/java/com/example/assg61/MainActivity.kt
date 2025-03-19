package com.example.assg61

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.assg61.ui.theme.Assg61Theme

data class CallLogEntry(val number: String, val type: String, val date: String)

/**
 * The class MainActivity
 *
 * This class starts the program by calling the CallHistoryScreen function
 *
 * The program shows the user's call history with information such as number, type (incoming or
 * outgoing) and date. It also allows for calling one of these numbers, handled with a Intent.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Assg61Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CallHistoryScreen()
                }
            }
        }
    }
}

/**
 * The function CallHistoryScreen
 *
 * This function first request the necessary to permissions to access the device's call logs and
 * the ability to make phone calls. Once these permissions have been accepted, the call history is
 * shown. If not accepted, a text that informs the user to accept permissions is shown.
 */
@Composable
fun CallHistoryScreen() {
    val context = LocalContext.current
    val callLogs = remember { mutableStateListOf<CallLogEntry>() }
    val permissionState = remember { mutableStateOf(false) }

    val permissions = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allPermissionsGranted = permissionsMap.values.all { it }
        permissionState.value = allPermissionsGranted
        if (allPermissionsGranted) {
            callLogs.clear()
            callLogs.addAll(getCallHistory(context))
        }
    }

    LaunchedEffect(Unit) {
        val allPermissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allPermissionsGranted) {
            permissionState.value = true
            callLogs.addAll(getCallHistory(context))
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Call History", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier
            .height(8.dp)
            .systemBarsPadding())

        if (permissionState.value) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(callLogs) { call ->
                    CallLogItem(call) { //Use function to create structure for each call
                        makeCall(context, call.number)
                    }
                }
            }
        } else {
            Text("Permissions required to access call logs.")
        }
    }
}

/**
 * The function CallLogItem
 *
 * This function creates and is the basis for all boxes with call history. It showcases the number,
 * what type the call was (incoming or outgoing) and what date the call was made.
 */
@Composable
fun CallLogItem(call: CallLogEntry, onCallClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCallClick() },
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Number: ${call.number}", fontWeight = FontWeight.Bold)
            Text("Type: ${call.type}")
            Text("Date: ${call.date}")
        }
    }
}

/**
 * The function getCallHistory
 *
 * This function creates list with all the entries acquired from the device's call history. This is
 * later returned to the CallHistoryScreen function to display the correct call history.
 */
fun getCallHistory(context: Context): List<CallLogEntry> {
    val callList = mutableListOf<CallLogEntry>()
    val cursor = context.contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
        null, null, CallLog.Calls.DATE + " DESC"
    )

    cursor?.use {
        val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
        val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
        val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)

        while (it.moveToNext()) {
            val number = it.getString(numberIndex)
            val type = when (it.getInt(typeIndex)) {
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.MISSED_TYPE -> "Missed"
                else -> "Unknown"
            }
            val date = it.getString(dateIndex)
            callList.add(CallLogEntry(number, type, date))
        }
    }
    return callList
}

/**
 * The function makeCall
 *
 * This function makes use of a Intent to enable the user to call any of the numbers from the call
 * history screen. It also makes sure that the user has accepted the permission to use the phone
 * function, otherwise the Intent will not start.
 */
fun makeCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$number")
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        context.startActivity(intent)
    }
}
