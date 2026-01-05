package com.example.turoapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.turoapp.R
import com.example.turoapp.models.Booking
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Adapter used by Renters to show only their own bookings.
 * Shows: confirmation code, car info, rental dates,
 * and includes button to cancel booking.
 *
 * @param bookings list of renter’s bookings
 * @param db Firestore reference for loading related listing data
 * @param onCancelClicked callback when cancel button clicked
 */
class MyBookingsAdapter(
    private var bookings: MutableList<Booking>,
    private val db: FirebaseFirestore,
    private val onCancelClicked: (String) -> Unit
) : RecyclerView.Adapter<MyBookingsAdapter.MyBookingsViewHolder>() {

    /**
     * ViewHolder holding UI elements for each booking card.
     */
    inner class MyBookingsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val confirmCode: TextView = view.findViewById(R.id.my_booking_confirmation_textview)
        val brandModelText: TextView = view.findViewById(R.id.my_booking_brand_model_textview)
        val colorText: TextView = view.findViewById(R.id.my_booking_color_textview)
        val address: TextView = view.findViewById(R.id.my_booking_address_city_textview)
        val datesText: TextView = view.findViewById(R.id.my_booking_dates_textview)
        val cancelButton: Button = view.findViewById(R.id.my_booking_cancel_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBookingsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_bookings, parent, false)
        return MyBookingsViewHolder(view)
    }

    override fun getItemCount() = bookings.size

    /**
     * Binds booking data AND loads additional related listing data from Firestore.
     *
     * @param holder UI holder for the item
     * @param position index of the booking in list
     */

    override fun onBindViewHolder(holder: MyBookingsViewHolder, position: Int) {
        val booking = bookings[position]

        // Load listing info from Firestore
        db.collection("listings").document(booking.listingId).get()
            .addOnSuccessListener { doc ->
                holder.brandModelText.text = "${doc.getString("brand")} ${doc.getString("model")}"
                holder.colorText.text = doc.getString("color")
                holder.address.text = "${doc.getString("address")}, ${doc.getString("city")}"
            }

        holder.confirmCode.text = booking.confirmationCode
        holder.datesText.text = "${booking.startDate} → ${booking.endDate}"
        holder.cancelButton.setOnClickListener { onCancelClicked(booking.id) }
    }

    /**
     * Update RecyclerView with new list of bookings.
     */

    fun updateBookings(newList: List<Booking>) {
        bookings.clear()
        bookings.addAll(newList)
        notifyDataSetChanged()
    }
}
