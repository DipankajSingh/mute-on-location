package com.dipdev.muteonlocation

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var savedLocation: Button
    private lateinit var muteButton: Button
    private lateinit var locationText: TextView
    private lateinit var startButton: Button
    private lateinit var savedAlreadyButton: Button
    private lateinit var locationManager: LocationManager
    private lateinit var db :AppDatabase
    private lateinit var locationDao: MutedLocationDAO
    private lateinit var audioManager :AudioManager
    private lateinit var notificationManager : NotificationManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var workTag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor=ContextCompat.getColor(this,R.color.background)

        preferencesManager = PreferencesManager(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        audioManager=getSystemService(Context.AUDIO_SERVICE) as AudioManager

        workTag = "mute_location_worker_tag"

        savedLocation = findViewById(R.id.savedButton)
        muteButton = findViewById(R.id.muteButton)
        locationText = findViewById(R.id.locationText)
        startButton = findViewById(R.id.startButton)
        savedAlreadyButton = findViewById(R.id.savedAlreadyButton)
        //database initialization
        db = AppDatabase.getDatabase(applicationContext)
        locationDao = db.mutedLocationDao()

        requestAllPermissions()
        setMutingState(startButton)
        startButton.setOnClickListener {
            toggleAutoMuting(startButton)
        }

        savedLocation.setOnClickListener {
            val intent = Intent(this, MutedLocationListActivity::class.java)
            startActivity(intent)
        }

        muteButton.setOnClickListener {
            val intent = Intent(this, UnmutedNumbersActivity::class.java)
            startActivity(intent)
        }

        if (isPermissionsGranted()) {
            Utils.fetchLocationAddress(applicationContext, locationManager, object : AddressCallback {
                override fun onSuccess(address: String, latitude: Double, longitude: Double) {
                    lifecycleScope.launch {
                        locationText.text = address
                        val isInRange = checkLocationInRange(latitude, longitude)
                        updateButtonState(isInRange, address, latitude, longitude)
                    }
                }

                override fun onError(errorMessage: String) {
                    locationText.text = "Error fetching address"
                    Toast.makeText(applicationContext, "Process failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(applicationContext, "Please allow all required permissions", Toast.LENGTH_SHORT).show()
            requestAllPermissions()
        }
    }

    private fun requestAllPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            }
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
            if(ActivityCompat.checkSelfPermission(applicationContext,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(applicationContext,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                ){
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                    101)
            }
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = applicationContext.packageName
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
        if (Build.VERSION.SDK_INT >= 29) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),101)
        }
    }

    private fun isPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun setMutingState(displayInView: TextView) {
        lifecycleScope.launch {
            preferencesManager.autoMute.collectLatest { isAutoMuteEnabled ->
                if (isAutoMuteEnabled==true) {
                    displayInView.text = "On"
                    displayInView.backgroundTintList = ContextCompat.getColorStateList(applicationContext, R.color.red)
                    displayInView.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                } else {
                    displayInView.text = "Off"
                    displayInView.backgroundTintList = ContextCompat.getColorStateList(applicationContext, R.color.grey)
                    displayInView.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
                }
            }
        }
    }

    private fun toggleAutoMuting(displayInView: TextView) {
        lifecycleScope.launch {
            val isAutoMuteEnabled = preferencesManager.autoMute.first() ?: false
            preferencesManager.autoMuting(!isAutoMuteEnabled)

            if (!isAutoMuteEnabled) {
                val workRequest = PeriodicWorkRequestBuilder<MuteLocationWorker>(15, TimeUnit.MINUTES)
                    .addTag(workTag)
                    .build()
                WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                    workTag,
                    ExistingPeriodicWorkPolicy.UPDATE, // To ensure the latest work request is used
                    workRequest
                )
            } else {
                WorkManager.getInstance(applicationContext).cancelAllWorkByTag(workTag)
                Utils.unmute(notificationManager,audioManager)
            }
            setMutingState(displayInView)
        }
    }

    private suspend fun checkLocationInRange(latitude: Double, longitude: Double): Boolean {
        val savedLocations = locationDao.getAllMutedLocations()
        return savedLocations.any { location ->
            Utils.isWithinRange(latitude, longitude, location.latitude, location.longitude)
        }
    }

    private fun muteAndSaveLocation(address: String, latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            val newLocation = LocationEntity(0, latitude, longitude, address)
            locationDao.insertMutedLocation(newLocation)
            Utils.mute(notificationManager, audioManager)
            updateButtonState(true, address, latitude, longitude)
        }
    }

    private fun unmuteAndRemoveLocation(address: String,latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            Utils.unmute(notificationManager, audioManager)
            val savedLocations = locationDao.getAllMutedLocations()

            val locationToDelete = savedLocations.find { savedLocation ->
                Utils.isWithinRange(latitude, longitude, savedLocation.latitude, savedLocation.longitude)
            }
            locationToDelete?.let {
                locationDao.deleteMutedLocation(it)
                updateButtonState(false, address, latitude, longitude)
            }
        }
    }

    private suspend fun updateButtonState(isInRange: Boolean, address: String, latitude: Double, longitude: Double) {
        withContext(Dispatchers.Main) {
            if (isInRange) {
                savedAlreadyButton.text = "Unmute & Remove"
                savedAlreadyButton.backgroundTintList = ContextCompat.getColorStateList(applicationContext, R.color.grey)
                savedAlreadyButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
                savedAlreadyButton.setOnClickListener { unmuteAndRemoveLocation(address,latitude, longitude) }
            } else {
                savedAlreadyButton.text = "Mute & Save"
                savedAlreadyButton.backgroundTintList = ContextCompat.getColorStateList(applicationContext, R.color.red)
                savedAlreadyButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                savedAlreadyButton.setOnClickListener { muteAndSaveLocation(address, latitude, longitude) }
            }
        }
    }

}
