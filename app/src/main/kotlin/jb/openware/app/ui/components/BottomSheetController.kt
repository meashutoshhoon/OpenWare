package jb.openware.app.ui.components

import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetController(private val context: Context) {

    private lateinit var dialog: BottomSheetDialog

    fun create(@LayoutRes layoutRes: Int, cancelable: Boolean = true) {
        dialog = BottomSheetDialog(context).apply {
            setContentView(layoutRes)
            setCancelable(cancelable)
        }
    }

    fun show() {
        check(::dialog.isInitialized) { "BottomSheetDialog not created" }
        dialog.show()
    }

    fun dismiss() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun setCancelable(cancelable: Boolean) {
        check(::dialog.isInitialized) { "BottomSheetDialog not created" }
        dialog.setCancelable(cancelable)
    }

    fun <T : View> find(@IdRes viewId: Int): T {
        check(::dialog.isInitialized) { "BottomSheetDialog not created" }
        return dialog.findViewById<T>(viewId)
            ?: error("View ID $viewId not found in BottomSheet layout")
    }

    fun isShowing(): Boolean = ::dialog.isInitialized && dialog.isShowing
}
