package com.example.visitsarawak

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddEditSpotActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var spotId: String? = null
    private var spotOwnerUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_spot)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.addEditToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val titleText = findViewById<TextView>(R.id.titleText)
        val subtitleInput = findViewById<EditText>(R.id.editSubtitle)
        val titleInput = findViewById<EditText>(R.id.editTitle)
        val descriptionInput = findViewById<EditText>(R.id.editDescription)
        val timeInput = findViewById<EditText>(R.id.editTime)
        val imageInput = findViewById<EditText>(R.id.editImageRef)
        val saveBtn = findViewById<MaterialButton>(R.id.btnSave)
        val deleteBtn = findViewById<MaterialButton>(R.id.btnDelete)

        spotId = intent.getStringExtra("id")

        if (spotId != null) {
            // Edit Spot
            titleText.text = "Edit Spot"
            toolbar.title = "Edit Spot"

            titleInput.setText(intent.getStringExtra("title"))
            subtitleInput.setText(intent.getStringExtra("subtitle"))
            descriptionInput.setText(intent.getStringExtra("description"))
            timeInput.setText(intent.getStringExtra("time"))
            imageInput.setText(intent.getStringExtra("imageRef"))

            // Get ownership info from Firestore
            db.collection("items").document(spotId!!)
                .get()
                .addOnSuccessListener { document ->
                    spotOwnerUid = document.getString("ownerUid")

                    // Show delete button only if user owns this spot
                    val currentUser = auth.currentUser
                    if (currentUser != null && spotOwnerUid == currentUser.uid) {
                        deleteBtn.visibility = View.VISIBLE
                    } else {
                        deleteBtn.visibility = View.GONE
                        Toast.makeText(this, "You can only view this spot (not owner)", Toast.LENGTH_LONG).show()
                    }
                }

        } else {
            // Add New Spot
            titleText.text = "Add New Spot"
            toolbar.title = "Add New Spot"
            deleteBtn.visibility = View.GONE

            val isQuickAdd = intent.getBooleanExtra("quickAdd", false)
            if (isQuickAdd) {
                val prefilledImageRef = intent.getStringExtra("prefilledImageRef")
                imageInput.setText(prefilledImageRef)
            }
        }

        // Save Spot Button
        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val subtitle = subtitleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val time = timeInput.text.toString().trim()
            val imageRef = imageInput.text.toString().trim()

            if (title.isEmpty() || subtitle.isEmpty() || description.isEmpty() || time.isEmpty() || imageRef.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            if (spotId == null) {
                // Add New Spot Map
                val spotData = hashMapOf(
                    "title" to title,
                    "subtitle" to subtitle,
                    "description" to description,
                    "time" to time,
                    "imageRef" to imageRef,
                    "ownerUid" to currentUser.uid
                )

                db.collection("items")
                    .add(spotData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Spot added successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Edit Existing Spot
                if (spotOwnerUid != currentUser.uid) {
                    Toast.makeText(this, "You can only edit your own spots!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val spotData = hashMapOf(
                    "title" to title,
                    "subtitle" to subtitle,
                    "description" to description,
                    "time" to time,
                    "imageRef" to imageRef,
                    "ownerUid" to currentUser.uid
                )

                db.collection("items")
                    .document(spotId!!)
                    .update(spotData as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Spot updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Delete Button
        deleteBtn.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser == null || spotOwnerUid != currentUser.uid) {
                Toast.makeText(this, "You can only delete your own spots!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Delete Spot")
                .setMessage("Are you sure you want to delete this spot?")
                .setPositiveButton("Delete") { _, _ ->
                    if (spotId != null) {
                        db.collection("items")
                            .document(spotId!!)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Spot deleted successfully!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(this, "Failed to delete: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}