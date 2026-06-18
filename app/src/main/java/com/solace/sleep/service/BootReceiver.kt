package com.solace.sleep.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.solace.sleep.worker.WorkScheduler
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.MY_PACKAGE_REPLACED",
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Timber.d("BootReceiver: scheduling workers after boot")
                WorkScheduler.scheduleAll(context)
            }
        }
    }
}
