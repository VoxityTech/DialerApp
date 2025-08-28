package io.voxity.dialer.sensors

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager

class ProximitySensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    private var isNear = false
    private var isCallActive = false

    fun startListening() {
        proximitySensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        releaseWakeLock()
    }

    fun setCallActive(active: Boolean) {
        isCallActive = active
        if (!active) {
            releaseWakeLock()
            enableScreen()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY && isCallActive) {
            val distance = event.values[0]
            val maxRange = event.sensor.maximumRange

            isNear = distance < maxRange

            if (isNear) {
                disableScreen()
            } else {
                enableScreen()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun disableScreen() {
        (context as? Activity)?.let { activity ->
            try {
                // Use newer screen control methods for Android 10+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activity.window.attributes = activity.window.attributes.apply {
                        screenBrightness = 0.0f
                    }
                } else {
                    // For older versions, use proximity wake lock
                    if (wakeLock?.isHeld != true) {
                        wakeLock = powerManager.newWakeLock(
                            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                            "DialerApp:ProximityWakeLock"
                        )
                        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
                    }
                }
            } catch (e: Exception) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun enableScreen() {
        (context as? Activity)?.let { activity ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activity.window.attributes = activity.window.attributes.apply {
                        screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    }
                } else {
                    releaseWakeLock()
                }
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } catch (e: Exception) {
                releaseWakeLock()
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                try {
                    it.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }
            }
        }
        wakeLock = null
    }
}