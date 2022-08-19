package com.example.papp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.example.papp.service.PService
import timber.log.Timber

class PrintReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val action = intent.action
        if (Intent.ACTION_BOOT_COMPLETED == action) {
            if (PService.isScanServiceRunning) {
                Timber.d("ACTION_BOOT_COMPLETED: PrinterService already running")
            } else {
                Timber.d("ACTION_BOOT_COMPLETED: PrinterService startService")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(Intent(context, PService::class.java))
                } else {
                    context.startService(Intent(context, PService::class.java))
                }
            }
        }
    }

    companion object {

        @Volatile
        private var receiversRegistered = false

        fun registerReceivers(context: Context) {
            if (receiversRegistered) return
            val receiver = PrintReceiver()
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_BOOT_COMPLETED)
            //filter.addDataScheme("content")
            //filter.addDataAuthority("com.android.calendar", null)

            context.registerReceiver(receiver, filter)

            Timber.d("Registered receivers")
            receiversRegistered = true
        }
    }
}