package com.example.assg21

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private val viewModel : PrimeView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrimeNumber(viewModel)
        }
    }
}

@Composable
fun PrimeNumber(viewModel: PrimeView) {
    val largestPrime = viewModel.largestPrime.value

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Largest Prime Found:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(largestPrime.toString(), style = MaterialTheme.typography.displayLarge)
            }
        }
    }
}

class PrimeView(application: Application) : AndroidViewModel(application) {
    private val primeFile: File = File(application.filesDir, "largestPrime.txt")
    private val _largestPrime = mutableStateOf(2L)
    val largestPrime: State<Long> get() = _largestPrime

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val savedPrime = primeFile.takeIf { it.exists() }?.readText()?.toLongOrNull() ?: 2L
            _largestPrime.value = savedPrime
            findPrimes(savedPrime)
        }
    }

    private suspend fun findPrimes(start: Long) {
        var current = start
        withContext(Dispatchers.Default) {
            while (true) {
                current++

                if (isPrime(current)) {
                    _largestPrime.value = current
                    savePrime(current)
                    delay(1000L)
                }
            }
        }
    }

    private fun savePrime(prime: Long) {
        primeFile.writeText(prime.toString())
    }

    private fun isPrime(num: Long): Boolean {
        if (num < 2) {
            return false
        }
        else if (num == 2L) {
            return true
        }
        else if (num % 2 == 0L) {
            return false
        }

        val sqrt = sqrt(num.toDouble()).toInt()

        for (i in 3..sqrt step 2) {
            if (num % i == 0L) {
                return false
            }
        }
        return true
    }
}

