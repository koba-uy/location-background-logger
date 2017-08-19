package uy.koba.locationbackgroundlogger

import android.Manifest
import android.content.pm.PackageManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import java.io.File


class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationChangeBroadcastReceiver: BroadcastReceiver
    private lateinit var map: GoogleMap

    // AppCompatActivity methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Start location service
        checkPermissionAndStartLocationService(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onStart() {
        super.onStart()

        locationChangeBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                showLocationOnMap(intent.extras["location"] as Location)
            }
        }

        registerReceiver(locationChangeBroadcastReceiver, IntentFilter("locationChange"))
    }

    override fun onStop() {
        super.onStop()

        unregisterReceiver(locationChangeBroadcastReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            checkPermissionAndStartLocationService(permissions[0])
        }
    }

    // OnMapReadyCallback methods

    override fun onMapReady(map: GoogleMap) {
        this.map = map
    }

    // children views handlers

    fun emailButtonOnClick(view: View) {
        val intent = Intent(Intent.ACTION_SEND)
        val file = File(applicationContext.filesDir, "location.log")

        with(intent) {
            type = "text/plain"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, "[Koba] LocationBackgroundLogger")
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(applicationContext, "${applicationContext.packageName}.fileprovider", file))
        }

        startActivityForResult(Intent.createChooser(intent, "Email log file"), 1)
    }

    // member methods

    private fun checkPermissionAndStartLocationService(permission: String) {
        if (checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startService(Intent(this, LocationService::class.java))
        }
        else {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog
                    .Builder(this)
                    .setTitle("Koba")
                    .setMessage("Could you tell me your location?")
                    .setPositiveButton("Yes, of course!", { _, _ -> ActivityCompat.requestPermissions(this, arrayOf(permission), 1) })
                    .create()
                    .show()
            }
            // No explanation needed, we can request the permission.
            else {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
            }
        }
    }

    private fun showLocationOnMap(location: Location) {
        val position = LatLng(location.latitude, location.longitude)

        map.run {
            addMarker(MarkerOptions().position(position))
            moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
        }
    }

}
