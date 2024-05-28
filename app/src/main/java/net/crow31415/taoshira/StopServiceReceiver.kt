package net.crow31415.taoshira

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val targetIntent = Intent(context, ShockDetectService::class.java)
        context.stopService(targetIntent)
    }
}