package com.example.visitsarawak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class SpotAdapter(
    private val spots: List<Spot>,
    private val onClick: (Spot) -> Unit,
    private val onLongClick: (Spot) -> Unit
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

        val context = holder.itemView.context
        val imageId = context.resources.getIdentifier(
            spot.imageRef,
            "drawable",
            context.packageName
        )
        holder.image.setImageResource(imageId)
        holder.image.contentDescription = "Image of ${spot.title}"

        // Short click - view details
        holder.itemView.setOnClickListener {
            onClick(spot)
        }

        // Long click - edit
        holder.itemView.setOnLongClickListener {
            onLongClick(spot)
            true
        }
    }

    override fun getItemCount() = spots.size
}