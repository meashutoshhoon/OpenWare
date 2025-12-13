package jb.openware.app.ui.activity.splash

import android.content.Intent
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import `in`.afi.codekosh.activity.home.HomeActivity
import jb.openware.app.databinding.ActivityMainBinding
import jb.openware.app.ui.activity.login.LoginActivity
import jb.openware.app.ui.activity.splash.fragments.MaintenanceFragment
import jb.openware.app.ui.activity.splash.fragments.NoInternetFragment
import jb.openware.app.ui.activity.splash.fragments.SplashFragment
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.items.ServerConfig
import jb.openware.app.ui.viewmodel.splash.MainUiState
import jb.openware.app.ui.viewmodel.splash.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity :
    BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel: MainViewModel by viewModels()

    override fun init() {
        if (supportFragmentManager.findFragmentById(binding.fragmentContainer.id) == null) {
            showFragment(SplashFragment())
        }
        observeState()
        observeNavigation()
        observeUpdate()
    }

    private fun observeUpdate() {
        viewModel.updateEvent.observe(this) { config ->
            showUpdateDialog(config)
        }
    }

    override fun initLogic() {
        onBackPressedDispatcher.addCallback(this) {
            finishAffinity()
        }
    }

    private fun showUpdateDialog(config: ServerConfig) {
        lifecycleScope.launch {
            delay(500)
            UpdateBottomSheetDialog(
                config = config,
                appVersion = BuildConfig.VERSION_CODE
            ) {
                goNext()
            }.show(supportFragmentManager, "UpdateDialog")
        }
    }

    private fun observeState() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                MainUiState.SPLASH ->
                    showFragment(SplashFragment())

                MainUiState.NO_INTERNET ->
                    showFragment(NoInternetFragment())

                MainUiState.MAINTENANCE ->
                    showFragment(MaintenanceFragment())
            }
        }
    }

    private fun observeNavigation() {
        viewModel.navigation.observe(this) {
            goNext()
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commitAllowingStateLoss()
    }

    private fun goNext() {
        val intent = Intent(
            this,
            if (userConfig.isLoggedIn()) {
                HomeActivity::class.java
            } else {
                LoginActivity::class.java
            }
        )
        startActivity(intent)
        finish()
    }

}


