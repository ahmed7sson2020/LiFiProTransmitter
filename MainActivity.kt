package com.example.lifiprotransmitter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var rootView: ConstraintLayout
    private lateinit var messageEditText: TextInputEditText
    private lateinit var encodedTextView: MaterialTextView
    private lateinit var statusText: MaterialTextView
    private lateinit var encodeButton: MaterialButton
    private lateinit var transmitButton: MaterialButton
    private lateinit var speedToggleGroup: MaterialButtonToggleGroup

    private var cameraManager: CameraManager? = null
    private var cameraIdWithFlash: String? = null
    private var pulseDurationMs: Long = 150L
    private var transmitJob: Job? = null

    private val wordEncodingMap = mapOf(
        "hi" to "001",
        "hello" to "0001"
    )

    private val frameStart = "1111"
    private val frameEnd = "0000"

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) initCamera()
            else showSnack("Camera permission required")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupSpeedToggle()
        setupCamera()

        encodeButton.setOnClickListener { encodeMessage() }
        transmitButton.setOnClickListener { startTransmission() }
    }

    private fun bindViews() {
        rootView = findViewById(R.id.rootLayout)
        messageEditText = findViewById(R.id.messageEditText)
        encodedTextView = findViewById(R.id.encodedTextView)
        statusText = findViewById(R.id.statusText)
        encodeButton = findViewById(R.id.encodeButton)
        transmitButton = findViewById(R.id.transmitButton)
        speedToggleGroup = findViewById(R.id.speedToggleGroup)
    }

    private fun setupSpeedToggle() {
        val mediumButton = findViewById<MaterialButton>(R.id.speedMediumButton)
        speedToggleGroup.check(mediumButton.id)

        speedToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            pulseDurationMs = when (checkedId) {
                R.id.speedSlowButton -> 300L
                R.id.speedMediumButton -> 150L
                R.id.speedFastButton -> 75L
                else -> 150L
            }
            statusText.text = "Speed: ${pulseDurationMs}ms"
        }
    }

    private fun setupCamera() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            initCamera()
        }
    }

    private fun initCamera() {
        val cm = cameraManager ?: return
        for (id in cm.cameraIdList) {
            val chars = cm.getCameraCharacteristics(id)
            val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            if (hasFlash) {
                cameraIdWithFlash = id
                break
            }
        }
    }

    private fun encodeMessage() {
        val text = messageEditText.text?.toString()?.trim()?.lowercase().orEmpty()
        if (text.isEmpty()) {
            showSnack("Enter text")
            return
        }

        val bits = wordEncodingMap[text] ?: encodeAsBinary(text)
        val framed = frameStart + bits + frameEnd
        encodedTextView.text = framed
        statusText.text = "Encoded (${framed.length} bits)"
    }

    private fun encodeAsBinary(text: String): String {
        val sb = StringBuilder()
        for (c in text) {
            sb.append(c.code.toString(2).padStart(8, '0'))
        }
        return sb.toString()
    }

    private fun startTransmission() {
        val bits = encodedTextView.text?.toString().orEmpty()
        if (bits.isEmpty()) return
        if (cameraIdWithFlash == null) return

        transmitJob?.cancel()
        transmitJob = lifecycleScope.launch {
            for (bit in bits) {
                setFlash(bit == '1')
                delay(pulseDurationMs)
            }
            setFlash(false)
            statusText.text = "Done"
        }
    }

    private fun setFlash(on: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraIdWithFlash?.let {
                cameraManager?.setTorchMode(it, on)
            }
        }
    }

    private fun showSnack(msg: String) {
        Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        transmitJob?.cancel()
        setFlash(false)
    }
}
