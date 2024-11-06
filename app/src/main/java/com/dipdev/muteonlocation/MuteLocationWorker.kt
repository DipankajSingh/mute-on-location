package com.dipdev.muteonlocation

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

class MuteLocationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private var audioManager=applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var notificationManager=applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun doWork(): Result {
        val notificationId=NotificationUtils.showNotification("Mute on Location","Checking location...",applicationContext)
        val currentLocation = Utils.getCurrentLocation(applicationContext) ?: return Result.failure()
        runBlocking {
            val db = AppDatabase.getDatabase(applicationContext)
            val locationDao = db.mutedLocationDao()
            val savedLocations = locationDao.getAllMutedLocations()

            val isInRange = savedLocations.any { savedLocation ->
                Utils.isWithinRange(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    savedLocation.latitude,
                    savedLocation.longitude
                )
            }
                try {
                    if (isInRange) {
                        Utils.mute(notificationManager,audioManager)
                    } else {
                        Utils.unmute(notificationManager,audioManager)
                        }
                }catch (e:Error){
                    NotificationUtils.showNotification(
                        "Mute on Location",
                        "Work failed to do the operation. \n"+e.message?.let { Log.d("com.dipdev.muteonlocation.MuteLocationWorker", it) },
                        applicationContext
                    )
                }
        }
        NotificationUtils.cancelNotification(applicationContext,notificationId)
        return Result.success()
    }


}
