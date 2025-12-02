package com.example.visitsarawak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class SpotAdapter(
    private val spots: List<Spot>,
    private val onClick: (Spot) -> Unit
) : RecyclerView.Adapter<SpotAdapter.SpotViewHolder>() {

    inner class SpotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.spotTitle)
        val image: ImageView = view.findViewById(R.id.spotImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spot, parent, false)
        return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = spots[position]
        holder.title.text = spot.title
        holder.image.setImageResource(spot.imageRes)

        holder.image.contentDescription = spot.title

        holder.itemView.setOnClickListener { onClick(spot) }
    }

    override fun getItemCount() = spots.size
}
