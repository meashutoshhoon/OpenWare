package jb.openware.app.ui.common

import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity

class BackHandler(
    private val activity: ComponentActivity,
    private val onBackPressedAction: () -> Unit = { activity.finishAffinity() }
) {
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressedAction()
        }
    }

    fun register() {
        activity.onBackPressedDispatcher.addCallback(activity, onBackPressedCallback)
    }

    fun unregister() {
        onBackPressedCallback.isEnabled = false
    }
}