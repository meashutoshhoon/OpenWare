package jb.openware.app.ui.viewmodel.settings

import android.util.Pair
import androidx.lifecycle.ViewModel

class SettingsItemViewModel : ViewModel() {

    var isToolbarExpanded: Boolean = true

    var scrollPosition: Pair<Int, Int>? = null
}
