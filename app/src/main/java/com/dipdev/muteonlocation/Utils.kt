// Utils.kt
package com.dipdev.muteonlocation

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.Locale


object Utils {


    // Method to get the address based on latitude and longitude
    private fun getReadableLocation(lat: Double, lon: Double, ctx: Context, callback: AddressCallback) {
        val geocoder = Geocoder(ctx, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use asynchronous geocoding for API 33+
            geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: List<Address>) {
                    if (addresses.isNotEmpty()) {
                        val address = formatAddress(addresses[0])
                        callback.onSuccess(address, lat, lon)
                    } else {
                        callback.onError("No address found")
                    }
                }

                override fun onError(errorMessage: String?) {
                    callback.onError("Geocoding error: $errorMessage")
                }
            })
        } else {
            // Use synchronous geocoding for lower API versions
            try {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = formatAddress(addresses[0])
                    callback.onSuccess(address, lat, lon)
                } else {
                    callback.onError("No address found")
                }
            } catch (e: IOException) {
                callback.onError("Geocoding failed: ${e.message}")
            }
        }
    }


    // Helper function to format the address into a readable string
    private fun formatAddress(address: Address): String {
        val addressLines = mutableListOf<String>()
        for (i in 0..address.maxAddressLineIndex) {
            addressLines.add(address.getAddressLine(i))
        }
        return addressLines.joinToString(", ")
    }

    // Method to get the location address, using last known location
    fun fetchLocationAddress(ctx: Context, locationManager: LocationManager, callback: AddressCallback) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            val location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            if (location != null) {
                getReadableLocation(location.latitude, location.longitude, ctx, callback)
            } else {
                callback.onError("Unable to retrieve last known location")
            }
        } else {
            callback.onError("Location permissions not granted")
        }
    }



    fun isWithinRange(newLat: Double, newLon: Double, savedLat: Double, savedLon: Double, range: Double = 500.0): Boolean {
        val location1 = Location("").apply {
            latitude = newLat
            longitude = newLon
        }

        val location2 = Location("").apply {
            latitude = savedLat
            longitude = savedLon
        }

        val distance = location1.distanceTo(location2) // Distance in meters
        return distance <= range // Check if within the specified range (0.5 km = 500 meters)
    }
    fun getCurrentLocation(ctx: Context): Location? {
        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null // Permissions are not granted, so we can't proceed.
        }

        return locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    fun mute(notificationManager:NotificationManager,audioManager: AudioManager){
        if (notificationManager.isNotificationPolicyAccessGranted) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        }
    }
    fun unmute(notificationManager:NotificationManager,audioManager: AudioManager) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
    }
}
