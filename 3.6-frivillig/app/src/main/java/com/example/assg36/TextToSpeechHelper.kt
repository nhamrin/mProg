package com.example.assg36

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

/**
 * The class TextToSpeechHelper
 *
 * This class defines the text to speech function using the built in Android library called
 * TextToSpeech. The class sets the language to Swedish and should in theory deny other languages
 * from being used, although that doesn't really seem to be working. If that were to work as
 * intended, a toast would pop up and state that that language isn't supported.
 */
class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var context: Context = context

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("sv", "SE")) ?: TextToSpeech.LANG_MISSING_DATA //Set language to Swedish
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "Det där språket stöds inte", Toast.LENGTH_LONG).show() //Okay, this doesn't really seem to work...
            }
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}