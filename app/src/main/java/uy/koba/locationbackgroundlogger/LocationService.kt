package uy.koba.locationbackgroundlogger

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log

/**
 * Created by agurz on 8/17/17.
 */

class LocationService : Service(), LocationListener {

    private val LOCATION_UPDATE_INTERVAL: Long = 10
    private val LOCATION_UPDATE_DISTANCE: Float = 0f

    private lateinit var location: Location
    private lateinit var locationManager: LocationManager

    // Service methods

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        startRequestingLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRequestingLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    // LocationListener methods

    override fun onLocationChanged(l: Location) {
        Log.i("onLocationChanged", l.toString())

        broadcastLocationChange(l)
        location.set(l)
    }

    override fun onStatusChanged(provider: String, status: Int, bundle: Bundle) {
        Log.i("onStatusChanged", provider)
    }

    override fun onProviderEnabled(provider: String) {
        Log.i("onProviderEnabled", provider)
    }

    override fun onProviderDisabled(provider: String) {
        Log.i("onProviderDisabled", provider)
    }

    // member methods

    private fun broadcastLocationChange(location: Location) {
        val intent = Intent()
        intent.action = "locationChange"
        intent.putExtra("location", location)
        sendBroadcast(intent)
    }

    @SuppressLint("LongLogTag")
    private fun startRequestingLocationUpdates() {
        for (provider in locationManager.allProviders) {
            try {
                location = Location(provider)
                locationManager.requestLocationUpdates(provider, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISTANCE, this)
            }
            catch (ex: SecurityException) {
                Log.e("startRequestingLocationUpdates", ex.message)
            }
            catch (ex: Exception) {
                Log.e("startRequestingLocationUpdates", ex.message)
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun stopRequestingLocationUpdates() {
        try {
            locationManager.removeUpdates(this)
        }
        catch (ex: SecurityException) {
            Log.e("stopRequestingLocationUpdates", ex.message)
        }
        catch (ex: Exception) {
            Log.e("startRequestingLocationUpdates", ex.message)
        }
    }

}
