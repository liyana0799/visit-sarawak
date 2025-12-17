package com.example.visitsarawak

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var titleText: TextView
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPassInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var toggleText: TextView
    private lateinit var statusText: TextView
    private lateinit var forgotPasswordText: TextView
    private lateinit var passwordHelperText: TextView

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        titleText = findViewById(R.id.tvAuthTitle)
        nameInputLayout = findViewById(R.id.nameInputLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        nameInput = findViewById(R.id.editName)
        emailInput = findViewById(R.id.editEmail)
        passwordInput = findViewById(R.id.editPassword)
        confirmPassInput = findViewById(R.id.editConfirmPassword)
        loginBtn = findViewById(R.id.btnLogin)
        registerBtn = findViewById(R.id.btnRegister)
        toggleText = findViewById(R.id.tvToggleMode)
        statusText = findViewById(R.id.authStatus)
        forgotPasswordText = findViewById(R.id.tvForgotPassword)
        passwordHelperText = findViewById(R.id.tvPasswordHelper)

        updateUI()

        // Show password requirements when helper text is clicked
        passwordHelperText.setOnClickListener {
            showPasswordRequirementsDialog()
        }

        toggleText.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
            statusText.text = ""
        }

        forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                statusText.text = "Please enter email and password"
                return@setOnClickListener
            }

            statusText.text = "Logging in..."
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    goToMain()
                }
                .addOnFailureListener {
                    statusText.text = "Login failed: ${it.message}"
                }
        }

        registerBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPass = confirmPassInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                statusText.text = "Please fill all fields"
                return@setOnClickListener
            }

            // Email Format Validation: Must be @gmail.com
            if (!email.endsWith("@gmail.com")) {
                statusText.text = "Email must be a @gmail.com address"
                return@setOnClickListener
            }

            // Password Requirement Validation: Check all requirements
            val passwordError = validatePassword(password)
            if (passwordError != null) {
                statusText.text = passwordError
                return@setOnClickListener
            }

            if (password != confirmPass) {
                statusText.text = "Passwords do not match!"
                return@setOnClickListener
            }

            statusText.text = "Creating account..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user!!
                    val uid = user.uid

                    // Set display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user.updateProfile(profileUpdates)
                        .addOnSuccessListener {
                            // Save to Firestore
                            val userMap = hashMapOf(
                                "uid" to uid,
                                "name" to name,
                                "email" to email
                            )

                            db.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                                    goToMain()
                                }
                                .addOnFailureListener {
                                    statusText.text = "Failed to save user data: ${it.message}"
                                }
                        }
                        .addOnFailureListener {
                            statusText.text = "Failed to update profile: ${it.message}"
                        }
                }
                .addOnFailureListener {
                    statusText.text = "Registration failed: ${it.message}"
                }
        }
    }


    private fun validatePassword(password: String): String? {
        // Check minimum length (8 characters)
        if (password.length < 8) {
            return "Password must be at least 8 characters long"
        }

        // Check for uppercase letter
        if (!password.any { it.isUpperCase() }) {
            return "Password must include at least one uppercase letter"
        }

        // Check for lowercase letter
        if (!password.any { it.isLowerCase() }) {
            return "Password must include at least one lowercase letter"
        }

        // Check for digit
        if (!password.any { it.isDigit() }) {
            return "Password must include at least one number"
        }

        // Check for special character
        val specialCharacters = "@#$%^&*()_+-=[]{}|;:',.<>?/~`"
        if (!password.any { it in specialCharacters }) {
            return "Password must include at least one special character (@, #, $, etc.)"
        }

        // All validations passed
        return null
    }

    private fun showPasswordRequirementsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Password Requirements")
        builder.setMessage(
            """
            Your password must include:
            
            ✓ At least 8 characters
            ✓ One uppercase letter (A-Z)
            ✓ One lowercase letter (a-z)
            ✓ One number (0-9)
            ✓ One special character
               (@, #, $, %, ^, &, *, etc.)
            
            Example: MyPass@123
            """.trimIndent()
        )
        builder.setPositiveButton("Got it") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")
        builder.setMessage("Enter your email address to receive a password reset link.")

        val input = EditText(this)
        input.hint = "Email Address"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.setPadding(50, 30, 50, 30)
        builder.setView(input)

        val currentEmail = emailInput.text.toString().trim()
        if (currentEmail.isNotEmpty()) {
            input.setText(currentEmail)
        }

        builder.setPositiveButton("Send Reset Link") { dialog, _ ->
            val email = input.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Password reset link sent to $email",
                        Toast.LENGTH_LONG
                    ).show()
                    statusText.text = "Check your email for reset link"
                    statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                    dialog.dismiss()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(
                        this,
                        "Failed to send reset email: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun updateUI() {
        if (isLoginMode) {
            titleText.text = "Welcome Back"
            nameInputLayout.visibility = View.GONE
            confirmPasswordLayout.visibility = View.GONE
            forgotPasswordText.visibility = View.VISIBLE
            passwordHelperText.visibility = View.GONE
            loginBtn.visibility = View.VISIBLE
            registerBtn.visibility = View.GONE
            toggleText.text = "Don't have an account? Sign Up"

            // Remove placeholder in login mode
            emailInput.hint = null
        } else {
            titleText.text = "Create Account"
            nameInputLayout.visibility = View.VISIBLE
            confirmPasswordLayout.visibility = View.VISIBLE
            forgotPasswordText.visibility = View.GONE
            passwordHelperText.visibility = View.VISIBLE
            loginBtn.visibility = View.GONE
            registerBtn.visibility = View.VISIBLE
            toggleText.text = "Already have an account? Log In"

        }

        statusText.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}