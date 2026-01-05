package com.example.turoapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.turoapp.R
import com.example.turoapp.models.Booking
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Adapter used by the Owner (ManageBookingsActivity) to display all bookings
 * made on the owner’s cars. Each item includes booking and renter details,
 * along with a Cancel Booking button to delete a booking from Firestore.
 *
 * @param bookings list of bookings to display
 * @param db reference to Firestore to fetch related listing details
 * @param onCancel callback when cancel button is pressed
 */
class ManageBookingsAdapter(
    private val bookings: MutableList<Booking>,
    private val onCancelClicked: (String) -> Unit,
    private val db: FirebaseFirestore
) : RecyclerView.Adapter<ManageBookingsAdapter.ManageBookingsViewHolder>() {

    /**
     * ViewHolder holding UI elements for each booking card.
     */
    inner class ManageBookingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val renterNameText: TextView = itemView.findViewById(R.id.manage_renter_name_textview)
        val brandModelText: TextView = itemView.findViewById(R.id.manage_brand_model_textview)
        val colorText: TextView = itemView.findViewById(R.id.manage_color_textview)
        val plateText: TextView = itemView.findViewById(R.id.manage_license_plate_textview)
        val costText: TextView = itemView.findViewById(R.id.manage_cost_textview)
        val datesText: TextView = itemView.findViewById(R.id.manage_rent_dates_textview)
        val carImage: ImageView = itemView.findViewById(R.id.manage_car_imageview)
        val confirmCode: TextView = itemView.findViewById(R.id.manage_confirmation_code)
        val cancelButton: Button = itemView.findViewById(R.id.manage_cancel_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageBookingsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_bookings, parent, false)
        return ManageBookingsViewHolder(view)
    }

    override fun getItemCount() = bookings.size

    /**
     * Binds booking data AND loads additional related listing data from Firestore.
     *
     * @param holder UI holder for the item
     * @param position index of the booking in list
     */
    override fun onBindViewHolder(holder: ManageBookingsViewHolder, position: Int) {
        val booking = bookings[position]

        // Load renter name
        db.collection("users").document(booking.renterId)
            .get().addOnSuccessListener { doc ->
                holder.renterNameText.text = "${doc.getString("firstName")} ${doc.getString("lastName")}"
            }

        // Load listing data
        db.collection("listings").document(booking.listingId)
            .get().addOnSuccessListener { doc ->
                holder.brandModelText.text =
                    "${doc.getString("brand")} ${doc.getString("model")}"
                holder.confirmCode.text = doc.getString("confirmationCode")
                holder.colorText.text = doc.getString("color")
                holder.plateText.text = doc.getString("licensePlate")
                holder.costText.text = "${(doc.get("cost") as? Number)?.toDouble()} $/day"

                Glide.with(holder.itemView.context)
                    .load(doc.getString("imageUrl"))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.carImage)
            }

        holder.datesText.text = "${booking.startDate} → ${booking.endDate}"
        holder.cancelButton.setOnClickListener { onCancelClicked(booking.id) }
    }

    /**
     * Replaces current booking list and refreshes RecyclerView.
     */
    fun updateBookings(newList: List<Booking>) {
        bookings.clear()
        bookings.addAll(newList)
        notifyDataSetChanged()
    }
}
