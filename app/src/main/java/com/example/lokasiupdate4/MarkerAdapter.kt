package com.example.lokasiupdate4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MarkerAdapter(val markers: List<MarkerData>) :
    RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>() {

    inner class MarkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.marker_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.marker_item, parent, false)
        return MarkerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        val marker = markers[position]
        holder.title.text = marker.title


    }

    override fun getItemCount() = markers.size


}
