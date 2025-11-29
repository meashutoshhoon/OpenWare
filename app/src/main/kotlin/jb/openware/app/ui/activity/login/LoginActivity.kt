package jb.openware.app.ui.activity.login

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import jb.openware.app.databinding.ActivityLoginBinding
import jb.openware.app.util.ThemeUtil
import kotlin.math.roundToInt

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        ThemeUtil.updateTheme(this)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }

    private fun dp(value: Int): Int =
        (resources.displayMetrics.density * value).roundToInt()

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



}