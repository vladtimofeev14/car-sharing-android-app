package com.example.turoapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.turoapp.R
import com.example.turoapp.models.Listing


/**
 * Adapter showing listings created by the currently logged-in owner.
 *
 * @param listings the owner’s listings to display
 */
class MyListingsAdapter(
    private val listings: MutableList<Listing>
) : RecyclerView.Adapter<MyListingsAdapter.ListingViewHolder>() {

    /**
     * ViewHolder for each listing card UI components.
     */
    inner class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carImage: ImageView = itemView.findViewById(R.id.my_listings_image)
        val brandModelText: TextView = itemView.findViewById(R.id.my_listings_brand_model)
        val licensePlateText: TextView = itemView.findViewById(R.id.my_listings_license_plate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_listings, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]

        holder.brandModelText.text = "${listing.brand} ${listing.model}"
        holder.licensePlateText.text = listing.licensePlate

        Glide.with(holder.itemView.context)
            .load(listing.imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.carImage)
    }

    override fun getItemCount(): Int = listings.size

    /**
     * Refreshes the RecyclerView with new listings.
     */
    fun updateData(newData: List<Listing>) {
        listings.clear()
        listings.addAll(newData)
        notifyDataSetChanged()
    }
}