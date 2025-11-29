package jb.openware.app.ui.activity.login

import android.os.Bundle
import android.util.Patterns
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import jb.openware.app.databinding.ActivityResetBinding
import jb.openware.app.util.ThemeUtil
import jb.openware.app.util.Utils

class ResetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetBinding
    private lateinit var progressBar: LinearProgressIndicator
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        ThemeUtil.updateTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityResetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progress
        progressBar.isIndeterminate = true

        setupListeners()
        disableScrollbars()
    }

    private fun setupListeners() {
        binding.back.setOnClickListener { finish() }

        binding.button.setOnClickListener {
            val email = binding.email.text?.toString()?.trim().orEmpty()

            if (!isValidEmail(email)) {
                binding.emailLayout.error = "Invalid email"
                return@setOnClickListener
            }

            binding.emailLayout.error = null
            Utils.hideKeyboard(binding.email)
            showLoadingDialog()

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    dismissLoadingDialog()
                    showSuccessDialog()
                }
                .addOnFailureListener { e ->
                    dismissLoadingDialog()
                    showErrorDialog(e)
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun disableScrollbars() {
        binding.scroll.apply {
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
        }
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage("A password reset link has been sent to your registered email.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showErrorDialog(e: Exception) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(e.message ?: "Unknown error occurred.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLoadingDialog() {
        TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
        progressBar.visibility = VISIBLE
    }

    private fun dismissLoadingDialog() {
        TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
        progressBar.visibility = INVISIBLE
    }
}
