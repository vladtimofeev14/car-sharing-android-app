package com.example.turoapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.turoapp.models.Booking
import com.example.turoapp.models.Listing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * CarProfileActivity
 *
 * Screen where a renter can view complete car details
 * and submit a booking request by selecting rental dates.
 *
 * Features:
 * - Retrieves listing details from Firestore based on listingId passed via Intent
 * - Loads owner profile info (name)
 * - Allows renter to enter start & end rental dates
 * - Creates a booking document in Firestore upon confirmation
 */
class CarProfileActivity : AppCompatActivity() {

    private lateinit var profileCarLicensePlateTextview: TextView
    private lateinit var profileCarImage: ImageView
    private lateinit var profileCarBrandTextview: TextView
    private lateinit var profileCarModelTextview: TextView
    private lateinit var profileCarColorTextview: TextView
    private lateinit var profileCarOwnerTextview: TextView
    private lateinit var profileCarCostTextview: TextView
    private lateinit var profileCarStartRentEditText: EditText
    private lateinit var profileCarEndRentEditText: EditText
    private lateinit var profileCarBookButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var currentListing: Listing? = null
    private var docId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_profile)
        bindWidgets()
        loadListingFromFirestore()
        wireEvents()
    }

    private fun bindWidgets() {
        profileCarLicensePlateTextview = findViewById(R.id.car_profile_license_textview)
        profileCarImage = findViewById(R.id.car_profile_image_imageview)
        profileCarBrandTextview = findViewById(R.id.car_profile_brand_textview)
        profileCarModelTextview = findViewById(R.id.car_profile_model_textview)
        profileCarColorTextview = findViewById(R.id.car_profile_color_textview)
        profileCarOwnerTextview = findViewById(R.id.car_profile_owner_textview)
        profileCarCostTextview = findViewById(R.id.car_profile_cost_textview)
        profileCarStartRentEditText = findViewById(R.id.car_profile_start_date_edittext)
        profileCarEndRentEditText = findViewById(R.id.car_profile_end_date_edittext)
        profileCarBookButton = findViewById(R.id.car_profile_book_button)
    }

    private fun wireEvents() {
        profileCarBookButton.setOnClickListener {
            val listing = currentListing
            if (listing != null) {
                bookListing(listing)
            } else {
                Toast.makeText(this, "Listing not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Loads listing details from Firestore using the listingId received from Intent.
     * Also triggers loading owner profile after listing is loaded.
     */
    private fun loadListingFromFirestore() {
        docId = intent.getStringExtra("listingId")
        if (docId.isNullOrEmpty()) {
            finish()
            return
        }

        db.collection("listings").document(docId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val listing = Listing(
                        id = document.id,
                        brand = document.getString("brand") ?: "",
                        model = document.getString("model") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        color = document.getString("color") ?: "",
                        licensePlate = document.getString("licensePlate") ?: "",
                        cost = (document.get("cost") as? Number)?.toDouble() ?: 0.0,
                        city = document.getString("city") ?: "",
                        address = document.getString("address") ?: "",
                        createdByUID = document.getString("createdByUID") ?: "",
                        latitude = document.getDouble("latitude") ?: 0.0,
                        longitude = document.getDouble("longitude") ?: 0.0,
                        isBooked = document.getBoolean("isBooked") ?: false
                    )
                    currentListing = listing
                    bindListingData(listing)
                    loadOwnerProfile(listing.createdByUID)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading listing", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    /**
     * Displays listing info (image, brand, model, etc.) into UI.
     */
    private fun bindListingData(listing: Listing) {
        profileCarLicensePlateTextview.text = listing.licensePlate
        profileCarBrandTextview.text = listing.brand
        profileCarModelTextview.text = listing.model
        profileCarColorTextview.text = "Color: ${listing.color}"
        profileCarCostTextview.text = "$${listing.cost} per day"

        Glide.with(this)
            .load(listing.imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(profileCarImage)
    }

    /**
     * Retrieves owner's full name from Firestore and displays it.
     *
     * @param uid User ID of the owner to load
     */
    private fun loadOwnerProfile(uid: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val firstName = userDoc.getString("firstName") ?: "Unknown"
                val lastName = userDoc.getString("lastName") ?: "Unknown"

                profileCarOwnerTextview.text = "Owner: $firstName $lastName"
            }
    }

    /**
     * Creates a booking Firestore document with user-selected rental dates.
     * Also generates a confirmation code for the booking.
     *
     * @param listing Listing being booked
     */
    private fun bookListing(listing: Listing) {
        val start = profileCarStartRentEditText.text.toString().trim()
        val end = profileCarEndRentEditText.text.toString().trim()

        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Enter rental dates", Toast.LENGTH_SHORT).show()
            return
        }

        val confirmationCode = generateConfirmationCode()
        val currentUser = auth.currentUser
        val listingId = intent.getStringExtra("listingId") ?: return

        val bookingId = db.collection("bookings").document().id

        val bookingData = Booking(
            id = bookingId,
            confirmationCode = confirmationCode,
            listingId = listingId,
            ownerId = listing.createdByUID,
            renterId = currentUser?.uid ?: "",
            startDate = start,
            endDate = end
        )

        db.collection("bookings").document(bookingId)
            .set(bookingData)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to book", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Generates a short alphanumeric confirmation code.
     * Used for showing both renter and owner reference to the booking.
     *
     * @return String generated confirmation code
     */
    private fun generateConfirmationCode(): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val random = java.util.Random()

        val letter1 = letters[random.nextInt(letters.length)]
        val letter2 = letters[random.nextInt(letters.length)]
        val numPart = (1000 + random.nextInt(9000)).toString() // 4 digits

        return "$letter1$letter2$numPart"
    }
}
