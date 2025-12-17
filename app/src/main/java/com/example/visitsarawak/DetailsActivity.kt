package com.example.visitsarawak

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore

class DetailsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var spotTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.detailToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val spotId = intent.getStringExtra("spotId")

        if (spotId != null) {
            loadSpotById(spotId)
        } else {
            Toast.makeText(this, "Error: Spot not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadSpotById(spotId: String) {
        val progressBar = findViewById<ProgressBar>(R.id.detailProgress)
        progressBar.visibility = View.VISIBLE

        db.collection("items")
            .document(spotId)
            .get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE

                if (document.exists()) {
                    val title = document.getString("title") ?: ""
                    val subtitle = document.getString("subtitle") ?: ""
                    val description = document.getString("description") ?: ""
                    val time = document.getString("time") ?: ""
                    val imageRef = document.getString("imageRef") ?: ""

                    displaySpotDetails(title, subtitle, description, time, imageRef)
                } else {
                    Toast.makeText(this, "Spot not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun displaySpotDetails(title: String, subtitle: String, description: String, time: String, imageRef: String) {
        spotTitle = title

        // Find views
        val titleView = findViewById<TextView>(R.id.detailTitle)
        val subtitleView = findViewById<TextView>(R.id.detailSubtitle)
        val descriptionView = findViewById<TextView>(R.id.detailDescription)
        val timeView = findViewById<TextView>(R.id.detailTime)
        val imageView = findViewById<ImageView>(R.id.detailImage)
        val tryButton = findViewById<MaterialButton>(R.id.tryButton)
        val shareButton = findViewById<MaterialButton>(R.id.shareButton)

        // Set text content
        titleView.text = title
        subtitleView.text = subtitle
        descriptionView.text = description
        timeView.text = time

        // Set image
        val imageId = resources.getIdentifier(imageRef, "drawable", packageName)
        imageView.setImageResource(imageId)
        imageView.contentDescription = "Image of $title"

        // Try It button
        tryButton.setOnClickListener {
            Toast.makeText(this, "Enjoy your visit to $title!", Toast.LENGTH_SHORT).show()
        }

        // Share button
        shareButton.setOnClickListener {
            shareSpot()
        }
    }

    private fun shareSpot() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out: $spotTitle")
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}