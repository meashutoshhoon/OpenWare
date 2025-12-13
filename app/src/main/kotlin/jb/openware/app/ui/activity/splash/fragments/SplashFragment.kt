package jb.openware.app.ui.activity.splash.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.MaterialSharedAxis
import com.google.gson.Gson
import jb.openware.app.databinding.FragmentSplashBinding
import jb.openware.app.ui.items.ServerConfig
import jb.openware.app.ui.viewmodel.splash.MainViewModel
import jb.openware.app.util.ConnectionManager
import jb.openware.app.util.RequestNetworkController.Companion.GET
import jb.openware.app.util.Utils
import jb.openware.app.util.serverUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private var hasDecided = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (hasDecided) return
        hasDecided = true

        viewLifecycleOwner.lifecycleScope.launch {
            delay(200) // allow Material animation to start

            if (!Utils.isConnected(requireContext())) {
                viewModel.onNoInternet()
                return@launch
            }

            fetchServerConfig()
        }
    }

    fun fetchServerConfig() {
        val manager = ConnectionManager()

        manager.startRequest(
            GET, serverUrl, "Request_Tag", object : ConnectionManager.RequestListener {
                override fun onResponse(
                    tag: String, response: String, responseHeaders: HashMap<String, Any>
                ) {
                    try {
                        val config = Gson().fromJson(response, ServerConfig::class.java)

                        viewModel.handleServerConfig(
                            config, appVersion = BuildConfig.VERSION_CODE
                        )

                    } catch (_: Exception) {
                        viewModel.onNoInternet()
                    }
                }

                override fun onErrorResponse(tag: String, message: String) {
                    if (!Utils.isConnected(requireContext())) {
                        viewModel.onNoInternet()
                    }
                }
            })

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


