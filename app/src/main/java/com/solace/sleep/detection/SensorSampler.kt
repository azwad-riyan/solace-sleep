package com.solace.sleep.detection

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.solace.sleep.domain.model.DetectionSensitivity
import com.solace.sleep.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.time.Instant
import kotlin.math.abs
import kotlin.math.sqrt

data class SensorReadings(
    val accelerometerMagnitude: Float = 0f,
    val ambientLux: Float = Float.MAX_VALUE,
    val isStill: Boolean = false,
    val isDark: Boolean = false,
    val stillSinceInstant: Instant? = null,
    val darkSinceInstant: Instant? = null
)

class SensorSampler(
    private val sensorManager: SensorManager,
    private val sensitivity: DetectionSensitivity
) : SensorEventListener {

    private val _readings = MutableStateFlow(SensorReadings())
    val readings: StateFlow<SensorReadings> = _readings.asStateFlow()

    private var lastMagnitude = 0f
    private var lastSignificantMotionAt: Instant = Instant.now()
    private var lastSignificantLightAt: Instant = Instant.now()

    private val magnitudeThreshold: Float = when (sensitivity) {
        DetectionSensitivity.LOW -> Constants.ACCEL_MAGNITUDE_THRESHOLD_LOW
        DetectionSensitivity.MEDIUM -> Constants.ACCEL_MAGNITUDE_THRESHOLD_MEDIUM
        DetectionSensitivity.HIGH -> Constants.ACCEL_MAGNITUDE_THRESHOLD_HIGH
    }

    private val stillDurationMinutes: Long = when (sensitivity) {
        DetectionSensitivity.LOW -> Constants.ACCEL_STILL_DURATION_MINUTES_LOW
        DetectionSensitivity.MEDIUM -> Constants.ACCEL_STILL_DURATION_MINUTES_MEDIUM
        DetectionSensitivity.HIGH -> Constants.ACCEL_STILL_DURATION_MINUTES_HIGH
    }

    fun start() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        accel?.let {
            sensorManager.registerListener(
                this, it, Constants.SENSOR_SAMPLE_PERIOD_US, Constants.SENSOR_BATCH_LATENCY_US
            )
        } ?: Timber.w("Accelerometer not available")

        light?.let {
            sensorManager.registerListener(
                this, it, Constants.SENSOR_SAMPLE_PERIOD_US, Constants.SENSOR_BATCH_LATENCY_US
            )
        } ?: Timber.w("Light sensor not available")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_LIGHT -> handleLight(event)
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        val delta = abs(magnitude - lastMagnitude)
        lastMagnitude = magnitude

        val now = Instant.now()
        val isMoving = delta > magnitudeThreshold

        if (isMoving) {
            lastSignificantMotionAt = now
        }

        val secondsStill = java.time.Duration.between(lastSignificantMotionAt, now).toSeconds()
        val requiredSeconds = stillDurationMinutes * 60
        val isStill = secondsStill >= requiredSeconds

        val current = _readings.value
        _readings.value = current.copy(
            accelerometerMagnitude = delta,
            isStill = isStill,
            stillSinceInstant = if (isStill) lastSignificantMotionAt else null
        )
    }

    private fun handleLight(event: SensorEvent) {
        val lux = event.values[0]
        val isDark = lux < Constants.LIGHT_LUX_THRESHOLD
        val now = Instant.now()

        if (!isDark) {
            lastSignificantLightAt = now
        }

        val current = _readings.value
        _readings.value = current.copy(
            ambientLux = lux,
            isDark = isDark,
            darkSinceInstant = if (isDark) lastSignificantLightAt else null
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    fun computeConfidenceScore(screenOff: Boolean, isStill: Boolean, isDark: Boolean): Int {
        var score = 0
        if (screenOff) score += 30
        if (isStill) score += 40
        if (isDark) score += 30
        return score.coerceIn(0, 100)
    }
}
