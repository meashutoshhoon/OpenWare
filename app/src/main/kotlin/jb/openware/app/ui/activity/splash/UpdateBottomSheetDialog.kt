package jb.openware.app.ui.activity.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jb.openware.app.databinding.UpdateCusBinding
import jb.openware.app.ui.components.TextFormatter
import jb.openware.app.ui.items.ServerConfig

class UpdateBottomSheetDialog(
    private val config: ServerConfig, private val appVersion: Int, private val onLater: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: UpdateCusBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = UpdateCusBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val isMandatory = appVersion < config.necessaryUpdateVersion

        binding.version.text = "v${config.typoVersion}"
        TextFormatter.format(binding.message, config.updateMessage)

        binding.later.visibility = if (isMandatory) View.GONE else View.VISIBLE
        isCancelable = !isMandatory

        binding.update.setOnClickListener {
            openUrl(config.updateLink)
        }

        binding.later.setOnClickListener {
            dismiss()
            onLater()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}
