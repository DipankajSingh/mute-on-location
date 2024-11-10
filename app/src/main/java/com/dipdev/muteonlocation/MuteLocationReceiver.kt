package com.dipdev.muteonlocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log

class MuteLocationReceiver : BroadcastReceiver() {

    private var onCall = false

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == TelephonyManager.EXTRA_STATE_RINGING && !onCall) {
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                NotificationUtils.showNotification("Mute Location", "Incoming call from $incomingNumber", context)

                // Set flag to prevent repeated notifications during the same call
                onCall = true
            } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                // Reset the flag when the call ends
                onCall = false
            }
        }
    }
}
