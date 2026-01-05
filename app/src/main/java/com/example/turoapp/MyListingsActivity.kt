package com.example.turoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turoapp.adapters.MyListingsAdapter
import com.example.turoapp.models.Listing
import com.example.turoapp.models.UserSession
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

/**
 * MyListingsActivity — Owner’s personal inventory screen.
 *
 * Features:
 * - Lists all cars created by the logged-in owner
 * - Shows empty message if owner hasn't posted any vehicles yet
 * - Provides navigation:
 *      -> Create new listing
 *      -> Manage bookings for current listings
 *      -> Logout
 */
class MyListingsActivity  : AppCompatActivity()  {
    private lateinit var myListingsAdapter: MyListingsAdapter
    private lateinit var myListingsRecyclerView : RecyclerView
    private lateinit var noListingsTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var addListingButton: Button
    private lateinit var goToBookingsButton: Button
    private val auth = Firebase.auth
    private val db  = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_listings)
        bindWidgets()
        wireUpEvents()
        setUpRecyclerView()

    }
    private fun setUpRecyclerView(){
        myListingsAdapter = MyListingsAdapter(mutableListOf())
        myListingsRecyclerView.layoutManager = LinearLayoutManager(this)
        myListingsRecyclerView.adapter = myListingsAdapter
        loadListings()
    }

    private fun wireUpEvents(){
        logoutButton.setOnClickListener {
            logoutUser()
        }

        goToBookingsButton.setOnClickListener{
            val intent = Intent(this, ManageBookingsActivity::class.java)
            startActivity(intent)
        }

        addListingButton.setOnClickListener {
            val intent = Intent(this, CreateListingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun bindWidgets() {
        logoutButton = findViewById(R.id.logout_button)
        addListingButton = findViewById(R.id.add_listing_button)
        goToBookingsButton = findViewById(R.id.go_to_bookings_button)
        myListingsRecyclerView = findViewById(R.id.my_listings_recycler_view)
        noListingsTextView = findViewById(R.id.no_listings_textview)
    }

    /**
     * Loads listings created by current owner from Firestore.
     * Updates UI depending on whether list is empty.
     */
    private fun loadListings() {

        val currentUser = FirebaseAuth.getInstance().currentUser
        val ownerId = currentUser?.uid ?: return

        db.collection("listings")
            .whereEqualTo("createdByUID", ownerId)
            .get()
            .addOnSuccessListener { result ->
                val listings = result.toObjects(Listing::class.java)

                if (listings.isEmpty()) {
                    noListingsTextView.visibility = View.VISIBLE
                    myListingsRecyclerView.visibility = View.GONE
                } else {
                    noListingsTextView.visibility = View.GONE
                    myListingsRecyclerView.visibility = View.VISIBLE
                    myListingsAdapter.updateData(listings)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load listings", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Logs user out, resets session data and returns to login screen.
     */
    private fun logoutUser(){
        auth.signOut()
        UserSession.resetSession()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        goToLoginScreen()
    }
    private fun goToLoginScreen(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}