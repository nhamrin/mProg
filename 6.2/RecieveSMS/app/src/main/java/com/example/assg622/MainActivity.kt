package com.example.assg622

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The class SmsViewModel
 *
 * This class passes a view model that will be used to create the app's user interface. It also
 * hosts some variables and functions dedicated to adding messages and stating if a permission was
 * denied or not.
 */
class SmsViewModel : ViewModel() {
    private val _smsMessages = mutableStateListOf<String>()
    val smsMessages: List<String> get() = _smsMessages

    var permissionDenied by mutableStateOf(false)
        private set

    fun addSmsMessage(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _smsMessages.add(0, message)
        }
    }

    fun changePermissionDenied(denied: Boolean) {
        permissionDenied = denied
    }
}

/**
 * The class MainActivity
 *
 * This class starts the program by calling the SmsReceiverApp function and sets up the application
 * by creating a view model, requesting permissions and registering a receiver
 */
class MainActivity : ComponentActivity() {
    private lateinit var smsViewModel: SmsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smsViewModel = SmsViewModel()
        requestSmsPermission()

        registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))

        setContent {
            SmsReceiverApp(smsViewModel)
        }
    }

    /**
     * The function requestSmsPermission
     *
     * This function request the application's needed permissions
     */
    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            smsViewModel.changePermissionDenied(!isGranted)
        }

    private val smsReceiver = object : BroadcastReceiver() { //Use a broadcast receiver
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) { //Not 100% like the assignment tip but seems to work
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle.get("pdus") as? Array<*>
                    pdus?.forEach {
                        val format = bundle.getString("format")
                        try {
                            val sms = SmsMessage.createFromPdu(it as ByteArray, format) //This might help against crashing? "format"
                            val message = "From: ${sms.originatingAddress}\n${sms.messageBody}"
                            smsViewModel.addSmsMessage(message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
    }
}

/**
 * The function SmsReceiverApp
 *
 * This function hosts the front end of the app using a viewModel. It creates a interface for the
 * received SMSs and also states if the needed permissions have been denied.
 */
@Composable
fun SmsReceiverApp(viewModel: SmsViewModel) {
    MaterialTheme {
        Surface(modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Received SMS Messages", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                if (viewModel.permissionDenied) {
                    Text("Permission denied", color = MaterialTheme.colorScheme.error)
                } else {
                    LazyColumn {
                        items(viewModel.smsMessages) { message ->
                            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Text(text = message, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
