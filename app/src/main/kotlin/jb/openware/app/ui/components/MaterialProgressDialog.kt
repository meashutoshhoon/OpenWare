package jb.openware.app.ui.components

import android.app.Activity
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Window
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import jb.openware.app.R

class MaterialProgressDialog(private val activity: Activity) {

    private var dialog: Dialog? = null

    fun show() {
        if (activity.isFinishing) return

        if (dialog == null) {
            dialog = Dialog(activity).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                setContentView(R.layout.loading)

                val background = findViewById<LinearLayout>(R.id.background)

                background?.setOnClickListener { hide() }
            }
        }

        dialog?.takeIf { !it.isShowing }?.show()
    }

    fun hide() {
        dialog?.let { dlg ->
            if (dlg.isShowing) dlg.dismiss()
        }
        dialog = null
    }
}
