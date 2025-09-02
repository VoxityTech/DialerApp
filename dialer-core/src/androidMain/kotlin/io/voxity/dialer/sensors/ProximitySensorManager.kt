package io.voxity.dialer.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.util.Log

class ProximitySensorManager(private val context: Context) : SensorEventListener {

    private val TAG = "ProximitySensorManager"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var wakeLock: PowerManager.WakeLock? = null
    private var isNear = false
    private var isCallActive = false
    private var isListening = false

    // Callback interface for proximity changes
    interface ProximityCallback {
        fun onProximityNear()
        fun onProximityFar()
    }

    private var proximityCallback: ProximityCallback? = null

    fun setProximityCallback(callback: ProximityCallback?) {
        proximityCallback = callback
    }

    fun startListening() {
        if (isListening) return

        proximitySensor?.let { sensor ->
            val registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (registered) {
                isListening = true
                Log.d(TAG, "Proximity sensor listening started")
            } else {
                Log.w(TAG, "Failed to register proximity sensor listener")
            }
        } ?: Log.w(TAG, "Proximity sensor not available")
    }

    fun stopListening() {
        if (!isListening) return

        sensorManager.unregisterListener(this)
        isListening = false
        releaseWakeLock()
        Log.d(TAG, "Proximity sensor listening stopped")
    }

    fun setCallActive(active: Boolean) {
        Log.d(TAG, "Call active state changed: $active")
        isCallActive = active
        if (!active) {
            releaseWakeLock()
            proximityCallback?.onProximityFar()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY && isCallActive) {
            val distance = event.values[0]
            val maxRange = event.sensor.maximumRange
            val wasNear = isNear

            isNear = distance < maxRange

            Log.d(TAG, "Proximity changed - distance: $distance, maxRange: $maxRange, isNear: $isNear")

            if (wasNear != isNear) {
                if (isNear) {
                    acquireWakeLock()
                    proximityCallback?.onProximityNear()
                } else {
                    releaseWakeLock()
                    proximityCallback?.onProximityFar()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld != true) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "DialerApp:ProximityWakeLock"
                ).apply {
                    acquire()
                }
                Log.d(TAG, "Proximity wake lock acquired")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                    Log.d(TAG, "Proximity wake lock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock", e)
        }
    }
}