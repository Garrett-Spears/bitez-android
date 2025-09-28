package com.garrett.bitez.ui.explore.foodlocations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.garrett.bitez.R
import com.garrett.bitez.data.model.FoodLocation

class FoodLocationsAdapter : ListAdapter<FoodLocation, FoodLocationViewHolder>(foodLocationComparator) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodLocationViewHolder {
        // Inflate the item layout
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_location, parent, false)

        return FoodLocationViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: FoodLocationViewHolder, position: Int) {
        val foodLocationItem: FoodLocation? = this.getItem(position)

        // Bind data to view holder to display
        if (foodLocationItem != null) {
            viewHolder.bind(foodLocationItem)
        }
    }

    // Create comparator to pass to adapter to use
    companion object {
        val foodLocationComparator: DiffUtil.ItemCallback<FoodLocation> = object : DiffUtil.ItemCallback<FoodLocation>() {
            override fun areItemsTheSame(oldItem: FoodLocation, newItem: FoodLocation): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FoodLocation, newItem: FoodLocation): Boolean {
                return oldItem == newItem
            }
        }
    }
}