package com.example.visitsarawak

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: SpotAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var btnSortAsc: MaterialButton
    private lateinit var btnSortDesc: MaterialButton

    private val allSpots = mutableListOf<Spot>()
    private val filteredSpots = mutableListOf<Spot>()

    // Track current sort state (default is ASCENDING by Time)
    private var currentSortOrder: SortOrder = SortOrder.ASCENDING

    enum class SortOrder {
        ASCENDING,
        DESCENDING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Authentication Protection
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        recycler = findViewById(R.id.recyclerView)
        recycler.layoutManager = GridLayoutManager(this, 2)

        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)

        val searchView = findViewById<SearchView>(R.id.searchView)
        btnSortAsc = findViewById(R.id.btnSortAsc)
        btnSortDesc = findViewById(R.id.btnSortDesc)
        val fabAdd = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd)

        db = FirebaseFirestore.getInstance()

        adapter = SpotAdapter(
            filteredSpots,
            onClick = { selected ->
                val intent = Intent(this, DetailsActivity::class.java)
                intent.putExtra("spotId", selected.id)
                startActivity(intent)
            },
            onLongClick = { selected ->
                val currentUser = auth.currentUser
                if (currentUser != null && selected.ownerUid == currentUser.uid) {
                    val intent = Intent(this, AddEditSpotActivity::class.java)
                    intent.putExtra("id", selected.id)
                    intent.putExtra("title", selected.title)
                    intent.putExtra("subtitle", selected.subtitle)
                    intent.putExtra("description", selected.description)
                    intent.putExtra("imageRef", selected.imageRef)
                    intent.putExtra("time", selected.time)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "You can only edit your own spots", Toast.LENGTH_SHORT).show()
                }
            }
        )

        recycler.adapter = adapter

        searchView.isIconified = false
        searchView.clearFocus()
        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSpots(newText ?: "")
                return true
            }
        })

        // Ascending sort button - toggle on/off
        btnSortAsc.setOnClickListener {
                currentSortOrder = SortOrder.ASCENDING
                sortSpots(true)
                updateButtonStates()
        }

        // Descending sort button - toggle on/off
        btnSortDesc.setOnClickListener {
            if (currentSortOrder == SortOrder.DESCENDING) {
                // Deselect - switch to ascending (no "unsorted" state)
                currentSortOrder = SortOrder.ASCENDING
                sortSpots(true)
                updateButtonStates()
            } else {
                // Select descending
                currentSortOrder = SortOrder.DESCENDING
                sortSpots(false)
                updateButtonStates()
            }
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditSpotActivity::class.java)
            intent.putExtra("quickAdd", true)
            intent.putExtra("prefilledImageRef", "default_spot_image")
            startActivity(intent)
        }

        loadSpotsFromFirestore()
    }

    override fun onResume() {
        super.onResume()
        loadSpotsFromFirestore()
    }

    private fun loadSpotsFromFirestore() {
        progressBar.visibility = View.VISIBLE
        recycler.visibility = View.GONE
        emptyText.visibility = View.GONE

        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE

                allSpots.clear()
                filteredSpots.clear()

                for (doc in result) {
                    val id = doc.id
                    val title = doc.getString("title") ?: ""
                    val subtitle = doc.getString("subtitle") ?: ""
                    val description = doc.getString("description") ?: ""
                    val imageRef = doc.getString("imageRef") ?: ""
                    val time = doc.getString("time") ?: ""
                    val ownerUid = doc.getString("ownerUid") ?: ""

                    val spot = Spot(
                        id = id,
                        title = title,
                        subtitle = subtitle,
                        description = description,
                        imageRef = imageRef,
                        time = time,
                        ownerUid = ownerUid
                    )
                    allSpots.add(spot)
                }

                filteredSpots.addAll(allSpots)

                // Apply current sort order (default is ASCENDING by Time)
                when (currentSortOrder) {
                    SortOrder.ASCENDING -> sortSpots(true)
                    SortOrder.DESCENDING -> sortSpots(false)
                }

                if (filteredSpots.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    emptyText.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }

            }
            .addOnFailureListener { error ->
                progressBar.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
                Toast.makeText(this, "Failed to load spots: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterSpots(query: String) {
        filteredSpots.clear()

        if (query.isEmpty()) {
            filteredSpots.addAll(allSpots)
        } else {
            val filtered = allSpots.filter { spot ->
                spot.title.contains(query, ignoreCase = true)
            }
            filteredSpots.addAll(filtered)
        }

        // Reapply current sort after filtering
        when (currentSortOrder) {
            SortOrder.ASCENDING -> applySortLogic(true)
            SortOrder.DESCENDING -> applySortLogic(false)
        }

        adapter.notifyDataSetChanged()

        if (filteredSpots.isEmpty() && query.isNotEmpty()) {
            emptyText.text = "No spots found for \"$query\""
            emptyText.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else if (filteredSpots.isEmpty()) {
            emptyText.text = "No spots yet. Tap + to add one!"
            emptyText.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recycler.visibility = View.VISIBLE
        }
    }

    // Update button visual states
    private fun updateButtonStates() {
        when (currentSortOrder) {
            SortOrder.ASCENDING -> {
                btnSortAsc.isSelected = true
                btnSortDesc.isSelected = false
            }
            SortOrder.DESCENDING -> {
                btnSortAsc.isSelected = false
                btnSortDesc.isSelected = true
            }
        }
    }

    // Converts time strings to minutes for proper comparison
    private fun sortSpots(ascending: Boolean) {
        applySortLogic(ascending)
        adapter.notifyDataSetChanged()
    }

    private fun applySortLogic(ascending: Boolean) {
        filteredSpots.sortWith { spot1, spot2 ->
            val minutes1 = parseTimeToMinutes(spot1.time)
            val minutes2 = parseTimeToMinutes(spot2.time)

            if (ascending) {
                minutes1.compareTo(minutes2)
            } else {
                minutes2.compareTo(minutes1)
            }
        }
    }

    // Helper function: Converts time strings to minutes
    private fun parseTimeToMinutes(timeStr: String): Int {
        val lowerTime = timeStr.lowercase()

        return try {
            when {
                // Handle "Full day" or similar
                lowerTime.contains("full day") || lowerTime.contains("24 hours") -> {
                    1440 // 24 hours = 1440 minutes
                }

                // Handle hours
                lowerTime.contains("hour") -> {
                    // Extract first number before "hour"
                    val numbers = Regex("""(\d+\.?\d*)""").findAll(lowerTime).map { it.value.toDouble() }.toList()
                    if (numbers.isNotEmpty()) {
                        (numbers.first() * 60).toInt()
                    } else {
                        Int.MAX_VALUE // Fallback for unparseable
                    }
                }

                // Handle minutes
                lowerTime.contains("minute") -> {
                    // Extract first number before "minute"
                    val numbers = Regex("""(\d+)""").findAll(lowerTime).map { it.value.toInt() }.toList()
                    if (numbers.isNotEmpty()) {
                        numbers.first()
                    } else {
                        Int.MAX_VALUE
                    }
                }

                else -> {

                    val numbers = Regex("""(\d+\.?\d*)""").findAll(lowerTime).map { it.value.toDouble() }.toList()
                    if (numbers.isNotEmpty()) {
                        (numbers.first() * 60).toInt()
                    } else {
                        Int.MAX_VALUE // Unknown format goes to end
                    }
                }
            }
        } catch (e: Exception) {
            Int.MAX_VALUE // Error parsing, put at end
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}