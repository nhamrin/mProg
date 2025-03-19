package com.example.assg721

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * The class MainActivity
 *
 * This class starts the program by calling the NotificationApp function. It also has the function
 * createNotificationChannel which, as the name suggests, creates a notification channel that the
 * notification manager can use.
 *
 * The program presents the user with three buttons. One to send a test notification, one opens a
 * test dialogue window and the last one a simple test toast.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            NotificationApp()
        }
    }

    /**
     * The function createNotificationChannel
     *
     * This function creates a new notification channel that can be used to test notifications
     */
    private fun createNotificationChannel() {
        val name = "Test Channel"
        val descriptionText = "Channel for test notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("test_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * The function NotificationApp
 *
 * This function creates the three buttons and houses the logic behind them. Once one of the
 * buttons is pressed by a user, the specific action happens. This function also uses a
 * permission launcher that controls the needed permission for test notifications to work.
 */
@Composable
fun NotificationApp() {
    val context = LocalContext.current
    var showDialogue by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            val notificationId = 1
            val notificationBuilder = NotificationCompat.Builder(context, "test_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Test")
                .setContentText("Test notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, notificationBuilder.build())
            }
        }) {
            Text("Send Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            showDialogue = true
        }) {
            Text("Open Dialog")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            Toast.makeText(context, "Test toast", Toast.LENGTH_SHORT).show()
        }) {
            Text("Open Toast")
        }
    }

    if (showDialogue) {
        Dialog(onDismissRequest = { showDialogue = false }) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Test Dialog Window")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showDialogue = false }) {
                        Text("Close")
                    }
                }
            }
        }
    }
}