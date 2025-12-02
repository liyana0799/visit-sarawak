package com.example.visitsarawak

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.appbar.MaterialToolbar

class DetailsActivity : AppCompatActivity() {

    private var spotTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val toolbar = findViewById<MaterialToolbar>(R.id.detailToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        spotTitle = intent.getStringExtra("title")
        val desc = intent.getStringExtra("desc")
        val time = intent.getStringExtra("time")
        val image = intent.getIntExtra("image", 0)

        val titleView = findViewById<TextView>(R.id.detailTitle)
        val descView = findViewById<TextView>(R.id.detailDesc)
        val timeView = findViewById<TextView>(R.id.detailTime)
        val imageView = findViewById<ImageView>(R.id.detailImage)
        val tryButton = findViewById<MaterialButton>(R.id.tryButton)
        val shareButton = findViewById<MaterialButton>(R.id.shareButton)

        titleView.text = spotTitle
        descView.text = desc
        timeView.text = time
        imageView.setImageResource(image)
        imageView.contentDescription = "Image of $spotTitle"

        tryButton.setOnClickListener {
            Toast.makeText(this, "Enjoy your visit to $spotTitle!", Toast.LENGTH_SHORT).show() //Try It button
        }

        shareButton.setOnClickListener {
            shareSpot()
        }
    }
    private fun shareSpot() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out: $spotTitle") //Share button
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
