package com.example.visitsarawak

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        // 2-grid column layout
        val recycler = findViewById<RecyclerView>(R.id.recyclerView)
        recycler.layoutManager = GridLayoutManager(this, 2)

        // Tourist Spot in Sarawak
        val spots = listOf(
            Spot("Kuching Waterfront", R.drawable.kuching_waterfront, getString(R.string.kuching_waterfront_desc), getString(R.string.kuching_waterfront_time)),
            Spot("Bako National Park", R.drawable.bako, getString(R.string.bako_desc), getString(R.string.bako_time)),
            Spot("Semenggoh Wildlife Centre", R.drawable.semenggoh_wildlife, getString(R.string.semenggoh_wildlife_desc), getString(R.string.semenggoh_wildlife_time)),
            Spot("Sarawak Cultural Village", R.drawable.cultural_village, getString(R.string.cultural_village_desc), getString(R.string.cultural_village_time)),
            Spot("Mulu Caves", R.drawable.mulu_cave, getString(R.string.mulu_cave_desc), getString(R.string.mulu_cave_time)),
            Spot("Damai Beach", R.drawable.damai_beach, getString(R.string.damai_beach_desc), getString(R.string.damai_beach_time)),
            Spot("Cat Museum", R.drawable.cat_museum, getString(R.string.cat_museum_desc), getString(R.string.cat_museum_time)),
            Spot("Jong's Crocodile Farm", R.drawable.crocodile_farm, getString(R.string.crocodile_farm_desc), getString(R.string.crocodile_farm_time)),
            Spot("Fairy Cave", R.drawable.fairy_cave, getString(R.string.fairy_cave_desc), getString(R.string.fairy_cave_time)),
            Spot("Upside Down House Kuching", R.drawable.upside_down, getString(R.string.upside_down_desc), getString(R.string.upside_down_time))
        )

        recycler.adapter = SpotAdapter(spots) { selected ->
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("title", selected.title)
            intent.putExtra("image", selected.imageRes)
            intent.putExtra("desc", selected.description)
            intent.putExtra("time", selected.time)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
