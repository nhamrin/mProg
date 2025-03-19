package com.example.assg711

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue

/**
 * The class MainActivity
 *
 * This class starts the program by calling the ManualCopyPaste function
 *
 * The program uses a very simple UI to showcase a manual version of the copy paste function. It
 * allows the user to write some text, click a button to copy this text, and another button to
 * paste the copied text into another text window.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManualCopyPasteApp()
        }
    }
}

/**
 * The function ManualCopyPasteApp
 *
 * This function sets up three variables, one for input, one for output and one third as a
 * temporary variable that is used as storage when copying. The function also creates the app's
 * layout, with two text fields and two buttons.
 */
@Composable
fun ManualCopyPasteApp() {
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var outputText by remember { mutableStateOf(TextFieldValue("")) }
    var tmp by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter text to copy") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                tmp = inputText //Copy the input text to a temporary variable
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Copy")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = outputText,
            onValueChange = { outputText = it },
            label = { Text("Pasted text will appear here") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true //Make it read-only to simulate pasting
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                outputText = tmp
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Paste")
        }
    }
}