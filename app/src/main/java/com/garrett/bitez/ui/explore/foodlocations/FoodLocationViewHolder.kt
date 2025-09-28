package com.garrett.bitez.ui.explore.foodlocations

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.garrett.bitez.R
import com.garrett.bitez.data.model.FoodLocation

class FoodLocationViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val foodLocationNameText: TextView = itemView.findViewById(R.id.food_location_name)

    fun bind(location: FoodLocation) {
        foodLocationNameText.text = location.name
    }
}