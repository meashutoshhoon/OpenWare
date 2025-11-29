package jb.openware.app.util

import android.view.View
import jb.openware.app.ui.common.HapticFeedback.longPressHapticFeedback
import jb.openware.app.ui.common.HapticFeedback.slightHapticFeedback
import jb.openware.app.ui.common.booleanState

object HapticUtils {

    /** Types of vibration. */
    enum class VibrationType {
        Weak,
        Strong
    }

    fun vibrate(view: View, type: VibrationType) {
        if (HAPTICS_VIBRATION.booleanState) {
            when (type) {
                VibrationType.Weak -> view.slightHapticFeedback()
                VibrationType.Strong -> view.longPressHapticFeedback()
            }
        }
    }

    fun weakVibrate(view: View) {
        vibrate(view, VibrationType.Weak)
    }

    fun strongVibrate(view: View) {
        vibrate(view, VibrationType.Strong)
    }
}