package jb.openware.app.ui.activity.drawer.logs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import jb.openware.app.data.LogsData
import jb.openware.app.databinding.FragmentUpdateLogBinding
import jb.openware.app.ui.components.TextFormatter

class UpdateLogFragment : Fragment() {

    private var _binding: FragmentUpdateLogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the formatted update log text
        TextFormatter.format(binding.updLog, LogsData.updateLog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
