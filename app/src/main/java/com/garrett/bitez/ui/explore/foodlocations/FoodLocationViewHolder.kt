package com.garrett.bitez.ui.explore.foodlocations

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.garrett.bitez.R
import com.garrett.bitez.data.model.FoodLocation

class FoodLocationViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val foodLocationNameText: TextView = itemView.findViewById<TextView>(R.id.food_location_name)
    private val foodLocationPhoto: ImageView = itemView.findViewById<ImageView>(R.id.food_location_photo)

    fun bind(foodLocationItem: FoodLocation) {
        foodLocationNameText.text = foodLocationItem.name

        // If photo available load it in
        if (foodLocationItem.photo != null) {
            Glide.with(this.itemView.context)
                .load(foodLocationItem.photo.getPhotoUrl())
                .placeholder(R.drawable.food_location_photo_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(foodLocationPhoto)
        }
        // Otherwise show placeholder
        else {
            Glide.with(this.itemView.context)
                .load(R.drawable.food_location_photo_placeholder)
                .into(foodLocationPhoto)
        }
    }
}