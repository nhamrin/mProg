package com.example.assg413

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

/**
 * The class MainActivity
 *
 * This class starts the program by calling the EmailSenderApp function
 *
 * The program gives the user three text fields that inputs a recipient, subject and message for a
 * e-mail. There is also the option to attach a file. When all text fields have some text in them,
 * the user can send the e-mail. This is done via a separate e-mailing application by launching a
 * Intent.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmailSenderApp()
        }
    }
}

/**
 * The function EmailSenderApp
 *
 * This function creates the three text fields and the two buttons for attaching a file and sending
 * the e-mail. The text fields make use of keyboardOptions to correctly categorise the content,
 * thus selecting the correct keyboard for that specific task.
 */
@Composable
fun EmailSenderApp() {
    val context = LocalContext.current
    var recipient by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        attachmentUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding() //Make sure that the text fields doesn't interfere with the status bar
    ) {
        OutlinedTextField(
            value = recipient,
            onValueChange = { recipient = it },
            label = { Text("Recipient") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { filePickerLauncher.launch("*/*") }) {
            Text("Attach File")
        }

        attachmentUri?.let {
            Text(text = "Attached: ${it.path}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                sendEmail(context, recipient, subject, message, attachmentUri) //Pass the variables to the sendEmail function
            },
            enabled = recipient.isNotEmpty() && subject.isNotEmpty() && message.isNotEmpty()
        ) {
            Text("Send Email")
        }
    }
}

/**
 * The function sendEmail
 *
 * This function makes use of a Intent to send the e-mail with the passed values using some other
 * pre-installed app on the device
 */
fun sendEmail(context: Context, recipient: String, subject: String, message: String, attachment: Uri?) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, message)
        attachment?.let {
            putExtra(Intent.EXTRA_STREAM, it)
        }
    }
    context.startActivity(Intent.createChooser(intent, "Send Email")) //Launch Intent
}
