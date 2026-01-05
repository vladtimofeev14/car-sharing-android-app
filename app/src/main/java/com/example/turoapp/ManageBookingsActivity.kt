package com.example.turoapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turoapp.adapters.ManageBookingsAdapter
import com.example.turoapp.models.Booking
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

/**
 * ManageBookingsActivity — Screen used by Owners to see **all bookings**
 * that renters made on their listings.
 *
 * Features:
 * - Loads bookings where Firestore.ownerId == current logged-in owner
 * - Displays bookings in RecyclerView
 * - Shows "Empty" message if list is empty
 * - Allows owner to cancel a booking by deleting it from Firestore
 */
class ManageBookingsActivity : AppCompatActivity() {
    private lateinit var manageBookingsRecyclerView: RecyclerView
    private lateinit var manageBookingsEmptyTextView: TextView
    private lateinit var manageBookingsAdapter: ManageBookingsAdapter
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    /**
     * Initializes UI components and sets up RecyclerView.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_bookings)
        bindWidgets()
        setUpRecyclerView()
    }

    /**
     * Ensures data refresh whenever user returns to the screen.
     */
    override fun onResume() {
        super.onResume()
        loadBookings()
    }

    private fun bindWidgets(){
        manageBookingsRecyclerView = findViewById(R.id.manage_bookings_recycler_view)
        manageBookingsEmptyTextView = findViewById(R.id.manage_bookings_empty_textview)
    }

    /**
     * Configures RecyclerView and adapter for displaying bookings.
     */
    private fun setUpRecyclerView(){
        manageBookingsAdapter = ManageBookingsAdapter(
            mutableListOf(),
            onCancelClicked = { bookingId -> cancelBooking(bookingId) },
            db = db
        )
        manageBookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        manageBookingsRecyclerView.adapter = manageBookingsAdapter
    }

    /**
     * Loads bookings belonging to the owner from Firestore,
     * updates the UI with result.
     */
    private fun loadBookings() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { result ->
                val bookingList = result.documents.map { doc ->
                    Booking(
                        id = doc.id,
                        listingId = doc.getString("listingId") ?: "",
                        ownerId = doc.getString("ownerId") ?: "",
                        renterId = doc.getString("renterId") ?: "",
                        startDate = doc.getString("startDate") ?: "",
                        endDate = doc.getString("endDate") ?: "",
                        confirmationCode = doc.getString("confirmationCode") ?: ""
                    )
                }

                manageBookingsAdapter.updateBookings(bookingList)
                updateUi(bookingList.isEmpty())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading bookings", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Toggles UI visibility depending on list empty state
     */
    private fun updateUi(isEmpty: Boolean) {
        manageBookingsEmptyTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        manageBookingsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    /**
     * Deletes a booking from Firestore and reloads data after removal.
     *
     * @param bookingId the Firestore document ID of the booking to be deleted
     */
    private fun cancelBooking(bookingId: String) {
        db.collection("bookings")
            .document(bookingId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Booking canceled.", Toast.LENGTH_SHORT).show()
                loadBookings()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel booking.", Toast.LENGTH_SHORT).show()
            }
    }
}