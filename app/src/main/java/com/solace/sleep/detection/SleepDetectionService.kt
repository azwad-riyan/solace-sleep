package com.solace.sleep.detection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.solace.sleep.MainActivity
import com.solace.sleep.R
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.Profile
import com.solace.sleep.util.Constants
import com.solace.sleep.util.isWithinDetectionWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class SleepDetectionService : Service() {

    @Inject lateinit var sensorManager: SensorManager
    @Inject lateinit var sessionRepository: SleepSessionRepository
    @Inject lateinit var profileRepository: ProfileRepository
    @Inject lateinit var preferences: AppPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val stateMachine = SleepStateMachine()

    private var sensorSampler: SensorSampler? = null
    private var screenReceiver: ScreenStateReceiver? = null
    private var isScreenOn = true

    private var activeProfile: Profile? = null
    private val interruptionTracker = mutableListOf<com.solace.sleep.domain.model.SleepInterruption>()
    private var interruptionStart = java.time.Instant.now()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(Constants.NOTIF_DETECTION_ID, buildNotification())
        Timber.d("SleepDetectionService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("SleepDetectionService started")
        serviceScope.launch {
            preferences.setDetectionServiceRunning(true)
            profileRepository.observeActiveProfile().collectLatest { profile ->
                profile?.let { startDetection(it) }
            }
        }
        return START_STICKY
    }

    private fun startDetection(profile: Profile) {
        activeProfile = profile
        stopSensors()

        val sampler = SensorSampler(sensorManager, profile.sensitivity)
        sensorSampler = sampler

        screenReceiver = ScreenStateReceiver(
            onScreenOff = {
                isScreenOn = false
                updateConfidence(sampler)
            },
            onScreenOn = {
                isScreenOn = true
                stateMachine.transition(SleepEvent.ScreenTurnedOn)
                updateConfidence(sampler)
            }
        )

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)
        sampler.start()

        serviceScope.launch {
            sampler.readings.collect { readings ->
                val confidence = sampler.computeConfidenceScore(
                    screenOff = !isScreenOn,
                    isStill = readings.isStill,
                    isDark = readings.isDark
                )
                stateMachine.transition(SleepEvent.ConfidenceUpdated(confidence))

                if (readings.isStill && !isScreenOn) {
                    stateMachine.transition(SleepEvent.SustainedStillness)
                }
                if (!readings.isStill && readings.accelerometerMagnitude > Constants.WAKE_MOVEMENT_THRESHOLD_MEDIUM) {
                    stateMachine.transition(SleepEvent.MotionDetected)
                }

                checkDetectionWindow(profile)
            }
        }

        serviceScope.launch {
            stateMachine.state.collect { ctx ->
                if (ctx.state == SleepState.FINALIZED) {
                    finalizeSession(ctx)
                }
            }
        }

        stateMachine.transition(SleepEvent.DetectionWindowEntered)
    }

    private fun checkDetectionWindow(profile: Profile) {
        val now = LocalTime.now()
        val inWindow = isWithinDetectionWindow(
            now,
            profile.detectionWindowStart,
            profile.detectionWindowEnd
        )
        if (!inWindow && stateMachine.currentState == SleepState.WATCHING) {
            stateMachine.transition(SleepEvent.DetectionWindowExited)
        } else if (inWindow && stateMachine.currentState == SleepState.IDLE) {
            stateMachine.transition(SleepEvent.DetectionWindowEntered)
        }
    }

    private fun updateConfidence(sampler: SensorSampler) {
        val readings = sampler.readings.value
        val confidence = sampler.computeConfidenceScore(
            screenOff = !isScreenOn,
            isStill = readings.isStill,
            isDark = readings.isDark
        )
        stateMachine.transition(SleepEvent.ConfidenceUpdated(confidence))
    }

    private fun finalizeSession(ctx: SleepStateContext) {
        val profile = activeProfile ?: return
        val sleepOnset = ctx.sleepOnsetAt ?: return
        val wakeTime = ctx.wakeTimeAt ?: java.time.Instant.now()

        val session = SessionBuilder.build(
            profileId = profile.id,
            sleepOnset = sleepOnset,
            wakeTime = wakeTime,
            confidenceScore = ctx.confidenceScore,
            interruptions = interruptionTracker.toList()
        )

        if (session != null) {
            serviceScope.launch {
                sessionRepository.saveSession(session)
                Timber.d("Session saved: ${session.id}, duration=${session.durationMinutes}m")
                showCorrectionNotification()
            }
        }

        interruptionTracker.clear()
        stateMachine.reset()
        stateMachine.transition(SleepEvent.DetectionWindowEntered)
    }

    private fun showCorrectionNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_CORRECTION)
            .setContentTitle(getString(R.string.notification_correction_title))
            .setContentText(getString(R.string.notification_correction_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(Constants.NOTIF_CORRECTION_ID, notification)
    }

    private fun stopSensors() {
        sensorSampler?.stop()
        sensorSampler = null
        screenReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
        screenReceiver = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSensors()
        serviceScope.cancel()
        serviceScope.launch {
            preferences.setDetectionServiceRunning(false)
        }
        Timber.d("SleepDetectionService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.CHANNEL_DETECTION,
            getString(R.string.notification_channel_detection_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_detection_description)
            setShowBadge(false)
        }
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)

        val correctionChannel = NotificationChannel(
            Constants.CHANNEL_CORRECTION,
            getString(R.string.notification_channel_correction_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_correction_description)
        }
        nm.createNotificationChannel(correctionChannel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, Constants.CHANNEL_DETECTION)
            .setContentTitle(getString(R.string.notification_detection_title))
            .setContentText(getString(R.string.notification_detection_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
