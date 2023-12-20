package com.example.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.newFixedThreadPoolContext
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchView=findViewById(R.id.searchView)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Inside your onCreate method or wherever you are setting up the SearchView
        val suggestionAdapter = SimpleCursorAdapter(
            this,
            android.R.layout.simple_list_item_1,
            null,
            arrayOf("suggestion"),
            intArrayOf(android.R.id.text1),
            0
        )
        searchView.suggestionsAdapter = suggestionAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle the query submission
                if (!query.isNullOrBlank()) {
                    // Use Geocoder to get the location coordinates
                    val geocoder = Geocoder(this@MainActivity)
                    try {
                        val addresses = geocoder.getFromLocationName(query, 1)
                        if (addresses != null) {
                            if (addresses.isNotEmpty()) {
                                val location = addresses[0]
                                val latLng = LatLng(location.latitude, location.longitude)

                                // Move the camera to the selected location
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                                // Add a marker at the selected location
                                mMap.addMarker(MarkerOptions().position(latLng).title(query))

                                // Optionally, you can show an AlertDialog or perform other actions
                            } else {
                                // Handle the case where no location was found
                                Toast.makeText(this@MainActivity, "Location not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    return true
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle changes in the query text
                return true
            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Enable the location layer
            mMap.isMyLocationEnabled = true

            // Get current location and move camera
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    val currentLocationMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                    if (currentLocationMarker != null) {
                        currentLocationMarker.tag = "CurrentLocationMarker"
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                }
            }

            // Define other locations and add markers and polyline as before
            val chapraLatLng = LatLng(25.7815, 84.7477) // Chapra coordinates
            val patnaLatLng = LatLng(25.5941, 85.1376) // Patna coordinates
            val amnourLatLng = LatLng(25.974308, 84.924067) // Amnour coordinates
            val sivanLatLng = LatLng(25.1337, 85.5300) // Sivan coordinates
            val garkhaLatLng = LatLng(25.2345, 85.7236) // Garkha coordinates

            mMap.addMarker(MarkerOptions().position(chapraLatLng).title("Chapra"))
            mMap.addMarker(MarkerOptions().position(patnaLatLng).title("Patna"))
//            mMap.addMarker(MarkerOptions().position(amnourLatLng).title("Amnour"))
            mMap.addMarker(MarkerOptions().position(sivanLatLng).title("Sivan"))
            mMap.addMarker(MarkerOptions().position(garkhaLatLng).title("Garkha"))

            val polylineOptions = PolylineOptions()
                .add(chapraLatLng)
                .add(LatLng(25.7815, 84.8576))
                .add(LatLng(25.7753, 84.8636))
                .add(patnaLatLng)
                .add(amnourLatLng)
                .add(sivanLatLng)
                .add(garkhaLatLng)
                .width(10f)
                .color(resources.getColor(R.color.colorPolyline))

            mMap.addPolyline(polylineOptions)

            // Set up marker click listener
            mMap.setOnMapClickListener { latLng ->

                // Display an alert dialog with current location data
                val geocoder = Geocoder(this)
                try {
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    val address = addresses?.firstOrNull()
                    val addressText = address?.getAddressLine(0) ?: "Address not found"


                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle("Current Location")
                        .setMessage("Address: $addressText\nLatitude: ${latLng.latitude}, Longitude: ${latLng.longitude}")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                    alertDialog.show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                true // Consume the event
            }
        }
        else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, check location permission again to refresh the map
                checkLocationPermission()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, call onMapReady to refresh the map
            onMapReady(mMap)
           }
    }
}