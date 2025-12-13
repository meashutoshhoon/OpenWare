package jb.openware.app.ui.activity.drawer.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import jb.openware.app.databinding.SettingsLookAndFeelBinding
import jb.openware.app.ui.common.booleanState
import jb.openware.app.ui.common.intState
import jb.openware.app.ui.viewmodel.settings.SettingsItemViewModel
import jb.openware.app.util.AMOLED_THEME
import jb.openware.app.util.DYNAMIC_THEME
import jb.openware.app.util.DeviceUtils
import jb.openware.app.util.HAPTICS_VIBRATION
import jb.openware.app.util.HapticUtils
import jb.openware.app.util.PreferenceUtil.updateBoolean
import jb.openware.app.util.PreferenceUtil.updateInt
import jb.openware.app.util.THEME_MODE
import jb.openware.app.util.ThemeUtil
import jb.openware.app.util.Utils

class LookAndFeelActivity : AppCompatActivity() {

    private lateinit var binding: SettingsLookAndFeelBinding
    private lateinit var viewModel: SettingsItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsLookAndFeelBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SettingsItemViewModel::class.java]

        setupBackPress()
        setupThemeOptions()
        setupAmoledSwitch()
        setupDynamicColorsSwitch()
        setupHapticAndVibration()
        setupDefaultLanguageOnClick()
    }

    override fun onPause() {
        super.onPause()
        viewModel.isToolbarExpanded = Utils.isToolbarExpanded(binding.appBarLayout)

        val scrollX = binding.nestedScrollView.scrollX
        val scrollY = binding.nestedScrollView.scrollY
        val scrollPosition = Pair(scrollX, scrollY)
        viewModel.scrollPosition = scrollPosition
    }

    override fun onResume() {
        super.onResume()

        binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded)

        val savedScrollPosition = viewModel.scrollPosition
        if (savedScrollPosition != null) {
            binding.nestedScrollView.viewTreeObserver.addOnGlobalLayoutListener {
                binding.nestedScrollView.scrollTo(
                    savedScrollPosition.first,
                    savedScrollPosition.second
                )
            }
        }
    }

    private fun setupBackPress() {
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))
    }

    private fun setupThemeOptions() {
        setRadioButtonState(binding.system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setRadioButtonState(binding.on, AppCompatDelegate.MODE_NIGHT_YES)
        setRadioButtonState(binding.off, AppCompatDelegate.MODE_NIGHT_NO)

        // Dark versions click -> trigger radio
        binding.darkSystem.setOnClickListener { binding.system.performClick() }
        binding.darkOn.setOnClickListener { binding.on.performClick() }
        binding.darkOff.setOnClickListener { binding.off.performClick() }
    }

    private fun setRadioButtonState(button: RadioButton, mode: Int) {
        button.isChecked = THEME_MODE.intState == mode
        button.setOnClickListener { v ->
            if (THEME_MODE.intState != mode) {
                HapticUtils.weakVibrate(v)
                handleRadioButtonSelection(button, mode)
            }
        }
    }

    private fun handleRadioButtonSelection(button: RadioButton, mode: Int) {
        clearRadioButtons()
        button.isChecked = true
        THEME_MODE.updateInt(mode)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun clearRadioButtons() {
        binding.system.isChecked = false
        binding.on.isChecked = false
        binding.off.isChecked = false
    }

    private fun setupAmoledSwitch() {
        binding.switchHighContrastDarkTheme.isChecked = AMOLED_THEME.booleanState
        binding.switchHighContrastDarkTheme.setOnCheckedChangeListener { view, isChecked ->
            HapticUtils.weakVibrate(view)
            AMOLED_THEME.updateBoolean(isChecked)
            if (ThemeUtil.isNightMode(this)) {
                recreate()
            }
        }
        binding.highContrastDarkTheme.setOnClickListener {
            binding.switchHighContrastDarkTheme.performClick()
        }
    }

    private fun setupDynamicColorsSwitch() {
        binding.dynamicColors.visibility =
            if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S) View.VISIBLE
            else View.GONE

        binding.switchDynamicColors.isChecked = DYNAMIC_THEME.booleanState
        binding.switchDynamicColors.setOnCheckedChangeListener { view, isChecked ->
            HapticUtils.weakVibrate(view)
            DYNAMIC_THEME.updateBoolean(isChecked)
            recreate()
        }
        binding.dynamicColors.setOnClickListener {
            binding.switchDynamicColors.performClick()
        }
    }

    private fun setupHapticAndVibration() {
        binding.switchHapticAndVibration.isChecked = HAPTICS_VIBRATION.booleanState
        binding.switchHapticAndVibration.setOnCheckedChangeListener { view, isChecked ->
            HapticUtils.weakVibrate(view)
            HAPTICS_VIBRATION.updateBoolean(isChecked)
        }
        binding.hapticAndVibration.setOnClickListener {
            binding.switchHapticAndVibration.performClick()
        }
    }

    private fun setupDefaultLanguageOnClick() {
        // Only Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.defaultLanguage.visibility = View.VISIBLE
        }

        binding.defaultLanguage.setOnClickListener { v ->
            HapticUtils.weakVibrate(v)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            }
        }
    }
}
