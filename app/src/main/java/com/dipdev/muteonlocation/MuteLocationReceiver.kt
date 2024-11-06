package com.dipdev.muteonlocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log

class MuteLocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action==TelephonyManager.ACTION_PHONE_STATE_CHANGED){
            Log.d("hg","hfhhgfjfjh")
        }
    }
}