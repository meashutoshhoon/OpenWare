package jb.openware.app.ui.viewmodel.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jb.openware.app.ui.items.ServerConfig

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(MainUiState.SPLASH)
    val uiState: LiveData<MainUiState> = _uiState

    private val _navigation = MutableLiveData<MainNavigation>()
    val navigation: LiveData<MainNavigation> = _navigation

    private val _updateEvent = MutableLiveData<ServerConfig>()
    val updateEvent: LiveData<ServerConfig> = _updateEvent

    private val _serverConfig = MutableLiveData<ServerConfig>()
    val serverConfig: LiveData<ServerConfig> = _serverConfig

    fun handleServerConfig(
        config: ServerConfig, appVersion: Int
    ) {
        _serverConfig.value = config

        when {
            appVersion < config.version -> {
                _updateEvent.value = config
            }

            !config.serverStatus -> {
                _uiState.value = MainUiState.MAINTENANCE
            }

            else -> {
                _navigation.value = MainNavigation.GoNext
            }
        }
    }

    fun retry() {
        _uiState.value = MainUiState.SPLASH
    }

    fun onNoInternet() {
        _uiState.value = MainUiState.NO_INTERNET
    }
}