package com.example.turoapp

import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Locale

/**
 * CreateListingActivity
 *
 * Screen for Owners to register a new car listing in the application.
 *
 * Features:
 * - Accepts user input for full car details
 * - Converts typed address into latitude/longitude using Geocoder
 * - Saves listing to Firestore under "listings"
 * - Automatically assigns owner ID from authenticated user
 *
 * Notes:
 * - Requires Internet connection for geocoding
 * - Displays input validation errors to user
 */
class CreateListingActivity : AppCompatActivity() {

    private lateinit var listingBrandEditText: EditText
    private lateinit var listingModelEditText: EditText
    private lateinit var listingColorEditText: EditText
    private lateinit var listingLicenseEditText: EditText
    private lateinit var listingCostEditText: EditText
    private lateinit var listingCityEditText: EditText
    private lateinit var listingAddressEditText: EditText
    private lateinit var listingImageUrlEditText: EditText
    private lateinit var createListingButton: Button

    private val db  = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    /**
     * Initializes UI and input listeners when Activity is launched.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)
        bindWidgets()
        wireUpEvents()
    }

    /**
     * Connects XML views to Kotlin variables.
     */
    private fun bindWidgets(){
        listingBrandEditText = findViewById(R.id.create_listing_brand_edittext)
        listingModelEditText = findViewById(R.id.create_listing_model_edittext)
        listingColorEditText = findViewById(R.id.create_listing_color_edittext)
        listingLicenseEditText = findViewById(R.id.create_listing_license_edittext)
        listingCostEditText = findViewById(R.id.create_listing_cost_edittext)
        listingCityEditText = findViewById(R.id.create_listing_city_edittext)
        listingAddressEditText = findViewById(R.id.create_listing_address_edittext)
        listingImageUrlEditText = findViewById(R.id.create_listing_image_url_edittext)
        createListingButton = findViewById(R.id.create_listing_button)
    }

    /**
     * Sets listener for the submit button.
     */
    private fun wireUpEvents() {
        createListingButton.setOnClickListener {
            createListing()
        }
    }

    /**
     * Validates user input, converts address to coordinates,
     * generates Firestore document for the listing,
     * and stores the listing along with owner information.
     */
    private fun createListing() {
        val brand = listingBrandEditText.text.toString().trim()
        val model = listingModelEditText.text.toString().trim()
        val color = listingColorEditText.text.toString().trim()
        val licensePlate = listingLicenseEditText.text.toString().trim()
        val cost = listingCostEditText.text.toString().trim().toDouble()
        val city = listingCityEditText.text.toString().trim()
        val address = listingAddressEditText.text.toString().trim()
        val imageUrl = listingImageUrlEditText.text.toString().trim()


        // Basic validation: required fields
        if (brand.isEmpty() || model.isEmpty() || color.isEmpty() || licensePlate.isEmpty() || city.isEmpty() || address.isEmpty() || imageUrl.isEmpty()
            ) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }


        val fullLocation = "$address, $city"

        // Convert typed address into latitude and longitude
        val geocoder = Geocoder(this, Locale.getDefault())
        val locationList = geocoder.getFromLocationName(fullLocation, 1)

        if (locationList.isNullOrEmpty()) {
            Toast.makeText(this, "Unable to locate address. Try different input.", Toast.LENGTH_SHORT).show()
            return
        }

        val latitude = locationList[0].latitude
        val longitude = locationList[0].longitude

        val uid = auth.currentUser?.uid ?: ""
        // Data to save in Firestore
        val listingData = hashMapOf<String, Any>(
            "brand" to brand,
            "model" to model,
            "color" to color,
            "licensePlate" to licensePlate,
            "cost" to cost,
            "city" to city,
            "address" to address,
            "imageUrl" to imageUrl,
            "createdByUID" to uid,
            "latitude" to latitude,
            "longitude" to longitude,
            "isBooked" to false//
        )

        // Save to Firestore
            db.collection("listings")
                .add(listingData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Listing created!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error adding listing.", Toast.LENGTH_SHORT).show()
                }
    }
}