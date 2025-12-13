package jb.openware.app.ui.viewmodel.settings

import android.util.Pair
import androidx.lifecycle.ViewModel
import jb.openware.app.ui.items.SettingsItem

class SettingsViewModel : ViewModel() {

    var isToolbarExpanded: Boolean = true

    var settingsData: List<SettingsItem> = emptyList()

    var rvPositionAndOffset: Pair<Int, Int>? = null
}
