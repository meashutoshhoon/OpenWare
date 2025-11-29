package jb.openware.app.ui.dialogs

import android.app.Activity
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import jb.openware.app.R
import jb.openware.app.databinding.EdittextBinding
import jb.openware.app.ui.items.ReportItem

class ReportDialog {

    private val db = Firebase.database
    private val reportRef = db.getReference("Report/comments")

    fun show(activity: Activity, data: ReportItem) {

        val binding = EdittextBinding.inflate(activity.layoutInflater)

        val dialog = MaterialAlertDialogBuilder(
            activity,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle("Report Comment")
            .setMessage(
                "If you believe this message contains abusive or harmful content, " +
                        "or violates OpenWare’s rules, please report it below."
            )
            .setIcon(R.drawable.report)
            .setView(binding.root)
            .setPositiveButton("Report") { _, _ ->
                val reason = binding.edittext.text.toString().trim()

                if (reason.isBlank()) {
                    Toast.makeText(activity, "Please enter a reason.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val payload = data.toMap(uid = currentUid(), reason = reason)
                reportRef.push().updateChildren(payload)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun currentUid(): String =
        Firebase.auth.currentUser?.uid ?: "UnknownUser"
}
