package com.example.turoapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.turoapp.models.UserSession
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * SearchCarActivity allows renters to search cars by city name
 * and display the results on a Google Map using custom markers.
 */
class SearchCarActivity : AppCompatActivity()  {
    private lateinit var searchCarMap: GoogleMap
    private lateinit var searchCarEditText: EditText
    private lateinit var searchCarButton: Button
    private lateinit var searchCarLogoutButton: Button
    private lateinit var goToBookingsButton: Button
    private val auth = Firebase.auth
    private val db  = Firebase.firestore

    /**
     * Initializes UI, events, and map components.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        bindWidgets()
        wireUpEvents()
        setUpMap()
    }

    /**
     * Attaches listeners to all buttons and initializes the Map when ready.
     */
    private fun wireUpEvents(){
        searchCarLogoutButton.setOnClickListener {
            logoutUser()
        }

        // Opens renter bookings screen
        goToBookingsButton.setOnClickListener{
            val intent = Intent(this, MyBookingsActivity::class.java)
            startActivity(intent)
        }

        // Search city button
        searchCarButton.setOnClickListener {
            val city = searchCarEditText.text.toString().trim()
            if (city.isNotEmpty()) {
                searchCity(city)
            }
        }

    }

    private fun setUpMap(){
        // Load map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            searchCarMap = googleMap

            // Redirect to details when clicking on price marker
            searchCarMap.setOnMarkerClickListener { marker ->
                val listingId = marker.tag as? String
                if (listingId != null) {
                    goToListingDetail(listingId)
                }
                true
            }
        }
    }

    /**
     * Connects UI widgets from XML to variables.
     */
    private fun bindWidgets() {
        searchCarLogoutButton = findViewById(R.id.logout_button)
        searchCarEditText = findViewById(R.id.search_city_edittext)
        searchCarButton = findViewById(R.id.search_button)
        goToBookingsButton = findViewById(R.id.go_to_bookings_button)
    }

    /**
     * Converts typed city name into GPS coordinates using Geocoder,
     * moves the map camera to that location, and loads listings from Firestore.
     *
     * @param city The city name entered by the user
     */
    private fun searchCity(city: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@SearchCarActivity, Locale.getDefault())
                val result = geocoder.getFromLocationName(city, 1)

                if (result == null || result.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SearchCarActivity, "City not found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val lat = result[0].latitude
                val lng = result[0].longitude
                val cityLocation = LatLng(lat, lng)

                withContext(Dispatchers.Main) {
                    searchCarMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 12f))
                }

                getListings(city)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SearchCarActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Queries Firestore for listings in the selected city
     * and creates price markers on the map.
     *
     * @param city City name used for Firestore filtering
     */
    private fun getListings(city: String) {
        db.collection("listings")
            .whereEqualTo("city", city)
            .get()
            .addOnSuccessListener { result ->
                searchCarMap.clear()

                for (doc in result) {
                    val listingId = doc.id

                    val lat = doc.getDouble("latitude") ?: continue
                    val lng = doc.getDouble("longitude") ?: continue
                    val cost = doc.get("cost") as? Number
                    val costValue = cost?.toDouble() ?: 0.0
                    val brand = doc.getString("brand") ?: ""

                    val location = LatLng(lat, lng)

                    val marker = searchCarMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .icon(createPriceMarker(costValue))
                            .title("$brand")
                    )
                    marker?.tag = listingId
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load listings", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Creates a custom marker with car rental price.
     *
     * @param price Daily rental price
     */
    private fun createPriceMarker(price: Double): BitmapDescriptor {
        val text = "$${price.toInt()}"
        val paint = Paint()
        paint.textSize = 40f
        paint.color = Color.WHITE
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER

        val padding = 20
        val textWidth = paint.measureText(text).toInt() + padding * 2
        val textHeight = (paint.descent() - paint.ascent()).toInt() + padding * 2

        val bitmap = Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rectPaint = Paint()
        rectPaint.color = Color.BLACK
        rectPaint.isAntiAlias = true
        canvas.drawRoundRect(
            RectF(0f, 0f, textWidth.toFloat(), textHeight.toFloat()),
            20f, 20f, rectPaint
        )

        canvas.drawText(text, textWidth / 2f, textHeight / 2f - (paint.ascent() / 2), paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Redirects to car profile screen when marker clicked.
     */
    private fun goToListingDetail(documentId: String) {
        val intent = Intent(this, CarProfileActivity::class.java)
        intent.putExtra("listingId", documentId)
        startActivity(intent)
    }

    /**
     * Logs out the current user and resets in-memory session.
     */
    private fun logoutUser(){
        auth.signOut()
        UserSession.resetSession()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        goToLoginScreen()
    }

    /** Redirects to login screen */
    private fun goToLoginScreen(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}