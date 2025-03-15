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

/**
 * The class MainActivity
 *
 * This class starts the program and passes a viewModel to the PrimeNumber function
 *
 * The program finds larger and larger prime numbers and displays and saves the current largest
 * one. When closing and re-opening the application, it will continue where it left of by reading
 * the current saved prime number from memory.
 */
class MainActivity : ComponentActivity() {
    private val viewModel : PrimeView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrimeNumber(viewModel)
        }
    }
}

/**
 * The function PrimeNumber
 *
 * This function is the backbone of the entire program, setting up the structure which the text and
 * prime number will slot into
 */
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

/**
 * The class PrimeView
 *
 * This class gets the current largest prime, computes the next largest prime and saves it to
 * memory
 */
class PrimeView(application: Application) : AndroidViewModel(application) {
    private val primeFile: File = File(application.filesDir, "largestPrime.txt")
    private val _largestPrime = mutableStateOf(2L)
    val largestPrime: State<Long> get() = _largestPrime

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val savedPrime = primeFile.takeIf { it.exists() }?.readText()?.toLongOrNull() ?: 2L //If the file isn't empty, get the saved prime number
            _largestPrime.value = savedPrime
            findPrimes(savedPrime) //Pass the saved prime number into the prime finding algorithm
        }
    }

    /**
     * The function findPrimes
     *
     * This function calls the prime finding function and increments to the number it should check
     * next
     *
     * @param start the current saved prime
     */
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

    /**
     * The function savePrime
     *
     * This function saves the current prime number to memory
     *
     * @param prime the current prime number
     */
    private fun savePrime(prime: Long) {
        primeFile.writeText(prime.toString())
    }

    /**
     * The function isPrime
     *
     * This function calculates if a number is prime or not
     *
     * @param num the number to be checked
     */
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

