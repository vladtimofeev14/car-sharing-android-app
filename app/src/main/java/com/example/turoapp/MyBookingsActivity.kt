package com.example.turoapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turoapp.adapters.MyBookingsAdapter
import com.example.turoapp.models.Booking
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

/**
 * MyBookingsActivity - Screen used by Renters to view their own bookings.
 *
 * Features:
 * - Loads Firestore records where Firestore.renterId == current user
 * - Displays results in a list
 * - Shows empty state if user has no bookings
 * - Allows renter to cancel booking
 */
class MyBookingsActivity : AppCompatActivity() {
    private lateinit var myBookingsRecyclerView: RecyclerView
    private lateinit var myBookingsEmptyMessageTextView: TextView
    private lateinit var myBookingsAdapter: MyBookingsAdapter
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    /**
     * Initializes UI components and sets up RecyclerView.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)
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
        myBookingsRecyclerView = findViewById(R.id.my_bookings_recycler_view)
        myBookingsEmptyMessageTextView = findViewById(R.id.my_bookings_empty_textview)
    }

    /**
     * Configures RecyclerView and adapter for displaying bookings.
     */
    private fun setUpRecyclerView(){

        myBookingsAdapter = MyBookingsAdapter(
            mutableListOf(),
            onCancelClicked = { bookingId -> cancelBooking(bookingId) },
            db = db
        )

        myBookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        myBookingsRecyclerView.adapter = myBookingsAdapter
    }

    /**
     * Loads bookings for the logged-in renter.
     * updates the UI with result.
     */
    private fun loadBookings() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("renterId", uid)
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

                myBookingsAdapter.updateBookings(bookingList)
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
        if(isEmpty){
            myBookingsEmptyMessageTextView.visibility = View.VISIBLE
            myBookingsRecyclerView.visibility = View.GONE
        } else {
            myBookingsEmptyMessageTextView.visibility = View.GONE
            myBookingsRecyclerView.visibility = View.VISIBLE
        }
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

