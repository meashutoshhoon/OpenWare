package jb.openware.app.ui.activity.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jb.openware.app.databinding.ActivityLoginBinding
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.components.RadialProgressView
import jb.openware.app.util.HapticUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {

    private var isNameEditTextVisible: Boolean = false
    private var login = false
    private var token: String? = null
    private var colorCode: String? = null
    private var id: String = ""
    private var deviceName: String = ""
    private val userMap = mutableMapOf<String, Any>()
    private val hashMap = mutableMapOf<String, Any>()
    private val usernames = mutableListOf<String>()

    private lateinit var progressView: RadialProgressView
    private val calendar = Calendar.getInstance()

    private lateinit var auth: FirebaseAuth
    private val firebase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val users: DatabaseReference by lazy { firebase.getReference("Users") }


    fun createRoundRectDrawable(radius: Int, color: Int): Drawable {
        val radii = FloatArray(8) { radius.toFloat() }  // 8 corners
        return ShapeDrawable(RoundRectShape(radii, null, null)).apply {
            paint.color = color
        }
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        isNameEditTextVisible = savedInstanceState?.getBoolean("isNameEditTextVisible") ?: false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ACTIVITY_B && resultCode == RESULT_OK) {
            if (login) login() else register()
        }

    }


    override fun init() {
        progressView = RadialProgressView(this).apply {
            setProgressColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary))
            setSize(dp(25))
        }
        binding.scroll1.disableScrollbars()

        auth = FirebaseAuth.getInstance()

        toggle(login)
        setLoading(false)


        binding.linearGo.addView(
            progressView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 17f
            }
        )

    }

    override fun initLogic() {
        lifecycleScope.launch {
            token = getMessagingToken()
        }

        binding.forgot.setOnClickListener { openActivity<ResetActivity>() }

        binding.linearGo.setOnClickListener {
            hideKeyboard()
            HapticUtils.weakVibrate(binding.linearGo)

            // validation
            if (login && !validateLogin()) return@setOnClickListener
            if (!login && !validateRegister()) return@setOnClickListener

            MaterialAlertDialogBuilder(this)
                .setTitle("Disclaimer")
                .setMessage(
                    "By proceeding, you are certifying that you have perused and consented to our terms and conditions.\n" +
                            "Further information can be found on our terms and conditions page."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue") { _, _ ->
                    val intent = Intent(this, CaptchaActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE_ACTIVITY_B)
                }
                .show()
        }

        binding.textview8.setOnClickListener {
            login = !login
            toggle(login)
            isNameEditTextVisible = login
        }


    }

    @SuppressLint("SetTextI18n")
    private fun toggle(isLogin: Boolean) = with(binding) {

        if (isLogin) {
            // LOGIN MODE
            usernameLayout.hide()
            confirmPasswordLayout.hide()

            emailLayout.show()
            passwordLayout.show()

            textview8.text = "Login"
            textview9.text = "New user?"
            textviewGo.text = "Register"
        } else {
            // REGISTER MODE
            usernameLayout.show()
            confirmPasswordLayout.show()

            emailLayout.show()
            passwordLayout.show()

            textview8.text = "Create account"
            textview9.text = "Already have an account?"
            textviewGo.text = "Login"
        }

        login = isLogin
    }

    private fun setLoading(isLoading: Boolean) {
        TransitionManager.beginDelayedTransition(binding.background)
        binding.textviewGo.visibility = if (isLoading) View.GONE else View.VISIBLE
        progressView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun validateLogin(): Boolean = with(binding) {
        emailLayout.clearError()
        passwordLayout.clearError()

        val email = email.textString().trim()
        val password = password.textString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Email is invalid"
            return false
        }

        if (password.length < 8) {
            passwordLayout.error = "Password must be at least 8 characters"
            return false
        }

        true
    }

    private fun validateRegister(): Boolean = with(binding) {
        usernameLayout.clearError()
        emailLayout.clearError()
        passwordLayout.clearError()
        confirmPasswordLayout.clearError()

        val username = username.textString().trim()
        val email = email.textString().trim()
        val password = password.textString()
        val confirmPassword = confirmPassword.textString()

        // only letters, numbers, underscore, dash, dot
        if (!username.matches(Regex("[a-zA-Z0-9_.-]*"))) {
            usernameLayout.error = "Username contains invalid characters"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Email is invalid"
            return false
        }

        if (password.length < 8) {
            passwordLayout.error = "Password must be at least 8 characters"
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords must match"
            return false
        }

        true
    }

    private fun login() {
        setLoading(true)

        val emailTxt = binding.email.textString().trim()
        val passwordTxt = binding.password.textString()

        auth.signInWithEmailAndPassword(emailTxt, passwordTxt)
            .addOnCompleteListener { task ->
                setLoading(false)

                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        if (user.isEmailVerified) {
                            // device/user map
                            userMap["device_name"] = deviceName
                            userMap["device_id"] = id
                            users.child(user.uid).updateChildren(userMap)
                            userMap.clear()

                            getUserConfig().saveLoginDetails(emailTxt, passwordTxt, user.uid)

                            val intent = Intent().apply {
                                setClass(
                                    this@LoginActivity,
                                    HomeActivity::class.java
                                )
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }

                            startActivity(intent)
                            finish()
                        } else {
                            // email not verified
                            MaterialAlertDialogBuilder(this)
                                .setTitle("Notice")
                                .setMessage(
                                    "To proceed, account verification is necessary. " +
                                            "Please check your email for the verification process."
                                )
                                .setPositiveButton("RESEND") { _, _ ->
                                    user.sendEmailVerification()
                                        .addOnCompleteListener { taskResend ->
                                            if (taskResend.isSuccessful) {
                                                MaterialAlertDialogBuilder(this)
                                                    .setTitle("Alert")
                                                    .setMessage("A verification link has been dispatched to your email.")
                                                    .setPositiveButton("OK", null)
                                                    .show()
                                            } else {
                                                alertCreator(taskResend.exception?.message ?: "Failed to resend email.")
                                            }
                                        }
                                }
                                .show()
                        }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Authentication failed."

                    when {
                        errorMessage.contains("invalid or the user does not have a password", ignoreCase = true) ->
                            alertCreator("Authentication failed due to an incorrect password entry or a recent password modification.")

                        errorMessage.contains("There is no user record corresponding to this identifier", ignoreCase = true) ->
                            alertCreator("No user exists with the provided email address on our servers, possibly due to deletion of the account.")

                        errorMessage.contains("The email address is already in use by another account", ignoreCase = true) ->
                            alertCreator("The account is already registered on our servers. Please use a different email or sign in using the registered email.")

                        errorMessage.contains("The user account has been disabled by an administrator", ignoreCase = true) ->
                            alertCreator("The user account has been banned by the administrator for violating our terms and conditions.")

                        errorMessage.contains("The email address is badly formatted", ignoreCase = true) ->
                            alertCreator("We regret to inform you that the email address provided is not in a proper format.")

                        else ->
                            alertCreator(errorMessage)
                    }
                }
            }
    }


    private fun pickRandomColor(): String = listOf(
        "#65A9E0", "#E56555", "#5FBED5", "#F2739A", "#76C84C",
        "#8D84EE", "#50A6E6", "#F28C48", "#009688", "#00FE5E",
        "#000000", "#795548", "#E64A19", "#FFC107", "#E91E63",
        "#00BCD4"
    ).random()

    private fun View.disableScrollbars() {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
    }

    fun TextInputEditText.textString(): String = text?.toString().orEmpty()
    fun TextInputLayout.clearError() {
        error = null
    }

    companion object {
        private const val REQUEST_CODE_ACTIVITY_B = 1
    }
}