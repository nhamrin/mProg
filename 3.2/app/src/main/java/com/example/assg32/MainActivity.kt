package com.example.assg32

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.assg32.databinding.ActivityMainBinding
import android.widget.Toast
import android.media.MediaRecorder
import android.media.MediaPlayer
import androidx.core.app.ActivityCompat

/**
 * The class MainActivity
 *
 * This class starts the program, creates a new view binding, defines the file path and sets up
 * permissions and buttons
 *
 * The program is a version of my earlier camera application from assignment 3.1. This program
 * allows the user to record audio via their device's microphone and then has the function to let
 * the user replay this audio snippet back. Note: Only the last recorded audio file can be replayed
 * back to the user, compared to the gallery function in the camera app.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        //Request permissions
        if(!allPermissionsGranted()) {
            requestPermissions()
        }

        audioFilePath = "${externalCacheDir?.absolutePath}/latest_recording.3gp"

        //Set up the listeners for recording and playing sounds
        viewBinding.recordButton.setOnClickListener { startRecording() }
        viewBinding.playButton.setOnClickListener { playRecording() }
    }

    /**
     * The function startRecording
     *
     * This function sets up the recording source and the output format for the saved file. It also
     * allows the user to stop recording by changing the functionality of the recordButton to
     * activate the function stopRecording.
     */
    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply { //Is deprecated but seems to work?
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
                Toast.makeText(this@MainActivity, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewBinding.recordButton.setOnClickListener { stopRecording() }
    }

    /**
     * The function stopRecording
     *
     * This function stops the recording and notifies the user with the message "Recording saved".
     * Then it changes the functionality of the recordButton to once again allow the user to start
     * a new recording.
     */
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()

        viewBinding.recordButton.setOnClickListener { startRecording() }
    }

    /**
     * The function playRecording
     *
     * This function replays the last saved audio recording from the defined file path.
     */
    private fun playRecording() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                Toast.makeText(this@MainActivity, "Playing last recording", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * The function requestPermissions
     *
     * This function request all the necessary permissions that the application needs to work
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE)
    }

    /**
     * The function allPermissionsGranted
     *
     * This function checks if so that all permissions have been granted and then passes this to
     * the package manager
     */
    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        private const val REQUEST_CODE = 101
    }
}