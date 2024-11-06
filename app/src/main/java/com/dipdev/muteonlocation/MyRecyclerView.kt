package com.dipdev.muteonlocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyAdapter(
    private val locations: MutableList<LocationEntity>, // Store LocationEntity objects instead of just addresses
    private val locationDao: MutedLocationDAO // Pass the DAO through the constructor
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.itemTextView)
        val imageView: ImageView = itemView.findViewById(R.id.itemImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locations[position]
        holder.textView.text = location.address // Show address of each location
    }

    fun deleteItem(position: Int) {
        val location = locations[position]
        CoroutineScope(Dispatchers.IO).launch {
            locationDao.deleteMutedLocation(location) // Delete the correct location entity
        }
        locations.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = locations.size
}
