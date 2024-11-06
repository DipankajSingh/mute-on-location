package com.dipdev.muteonlocation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class MutedLocationListActivity : AppCompatActivity() {

    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muted_location_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val items = mutableListOf<LocationEntity>()

        // Initialize adapter with empty list and set DAO
        val db = AppDatabase.getDatabase(applicationContext)
        val locationDao = db.mutedLocationDao()
        adapter = MyAdapter(items, locationDao)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load muted locations asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val savedLocations = locationDao.getAllMutedLocations()

            withContext(Dispatchers.Main) {
                if (savedLocations.isEmpty()) {
                    Toast.makeText(this@MutedLocationListActivity, "No locations found!", Toast.LENGTH_SHORT).show()
                } else {
                    items.addAll(savedLocations) // Add items to the existing list
                    adapter.notifyDataSetChanged() // Notify adapter of data change
                }
            }
        }

        // Customize ActionBar
        supportActionBar?.apply {
            title = "Muted Locations"
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#00FFFFFF")))
            setDisplayHomeAsUpEnabled(true)
            setTitleColor(Color.BLACK)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }

        // Set up swipe actions for RecyclerView items
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT) {
                    // Delete action
                    adapter.deleteItem(position)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Archive action
                    Toast.makeText(this@MutedLocationListActivity, "Item archived", Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position) // Restore item (demo purposes)
                }
            }

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val backgroundColor = if (dX > 0) Color.GREEN else Color.RED
                val itemView = viewHolder.itemView

                // Draw background color based on swipe direction
                val background = ColorDrawable(backgroundColor)
                background.setBounds(
                    itemView.left + dX.toInt(),
                    itemView.top,
                    itemView.right + dX.toInt(),
                    itemView.bottom
                )
                background.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        // Attach ItemTouchHelper to RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun ActionBar.setTitleColor(color: Int) {
        val text = SpannableString(title ?: "")
        text.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        title = text
    }

    // Handle back button in the action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Closes the current activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
