package com.example.visitsarawak

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch

class AboutActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<MaterialToolbar>(R.id.aboutToolbar)
        setSupportActionBar(toolbar)

        // Back navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Student's name, Unique Code Variation
        val aboutTitle = findViewById<TextView>(R.id.aboutTitle)
        val aboutName = findViewById<TextView>(R.id.aboutName)
        val aboutCode = findViewById<TextView>(R.id.aboutCode)

        aboutTitle.text = getString(R.string.visit_sarawak)
        aboutName.text = getString(R.string.your_name_here)

        // Unique Variation Code
        aboutCode.text = """
            Matric Number: 100804
            Unique Variation Code: 04
            Theme: Tourist Spots
            Layout: 2-column Grid
            Accent: Blue
            Extra field: Time
            Button: "Try it"
        """.trimIndent()

        // Large Text toggle
        val toggle = findViewById<MaterialSwitch>(R.id.textSwitch)
        val sample = findViewById<TextView>(R.id.sampleText)

        toggle.setOnCheckedChangeListener { _, isChecked ->

            val newSize = if (isChecked) 24f else 16f
            sample.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
        }
    }

    // Back action
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
