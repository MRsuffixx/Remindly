package com.mrsuffix.remindly.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver to reschedule reminders after device boot
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            ReminderWorker.schedule(context)
        }
    }
}
