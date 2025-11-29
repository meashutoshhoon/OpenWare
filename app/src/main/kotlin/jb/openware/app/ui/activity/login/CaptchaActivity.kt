package jb.openware.app.ui.activity.login

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.safetynet.SafetyNet
import jb.openware.app.databinding.ActivityCaptchaBinding
import jb.openware.app.util.ThemeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CaptchaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaptchaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        ThemeUtil.updateTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCaptchaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener { finish() }
        binding.button.setOnClickListener { verifyCaptcha() }
    }

    private fun verifyCaptcha() {
        SafetyNet.getClient(this)
            .verifyWithRecaptcha("6Lcbp_4lAAAAAI7M5BmjuOk3RF-ujzBC6at-zMV8")
            .addOnSuccessListener {
                lifecycleScope.launch {
                    delay(150)
                    setResult(RESULT_OK)
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message ?: "Error", Toast.LENGTH_SHORT).show()
            }
    }
}
