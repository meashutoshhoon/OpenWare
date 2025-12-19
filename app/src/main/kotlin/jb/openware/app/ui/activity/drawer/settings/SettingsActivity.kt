package jb.openware.app.ui.activity.drawer.settings

import android.util.Pair
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jb.openware.app.R
import jb.openware.app.databinding.ActivitySettingsBinding
import jb.openware.app.ui.adapter.SettingsAdapter
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.common.booleanState
import jb.openware.app.ui.items.SettingsItem
import jb.openware.app.ui.viewmodel.settings.SettingsItemViewModel
import jb.openware.app.ui.viewmodel.settings.SettingsViewModel
import jb.openware.app.util.Const
import jb.openware.app.util.SMOOTH_SCROLLING
import jb.openware.app.util.Utils

class SettingsActivity : BaseActivity<ActivitySettingsBinding>(ActivitySettingsBinding::inflate) {

    private lateinit var settingsData: MutableList<SettingsItem>
    private lateinit var adapter: SettingsAdapter

    private lateinit var viewModel: SettingsViewModel
    private lateinit var itemViewModel: SettingsItemViewModel

    private var rvPositionAndOffset: Pair<Int, Int>? = null


    override fun init() {
        val context = this

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        itemViewModel = ViewModelProvider(this)[SettingsItemViewModel::class.java]



        settingsData = mutableListOf()

        // --------- Settings list population (same as fragment) ---------

        settingsData.add(
            SettingsItem(
                id = Const.ID_LOOK_AND_FEEL,
                symbolResId = R.drawable.ic_pallete,
                title = getString(R.string.look_and_feel),
                description = getString(R.string.des_look_and_feel),
                hasSwitch = false,
                isChecked = false
            )
        )

        settingsData.add(
            SettingsItem(
                id = Const.PREF_SMOOTH_SCROLL,
                symbolResId = R.drawable.ic_scroll,
                title = getString(R.string.smooth_scrolling),
                description = getString(R.string.des_smooth_scroll),
                hasSwitch = true,
                isChecked = SMOOTH_SCROLLING.booleanState
            )
        )


        settingsData.add(
            SettingsItem(
                id = Const.ID_ABOUT,
                symbolResId = R.drawable.info,
                title = getString(R.string.about),
                description = getString(R.string.des_about),
                hasSwitch = false,
                isChecked = false
            )
        )

        // --------- RecyclerView setup ---------

        adapter = SettingsAdapter(
            settingsList = settingsData,
            context = context,
            activity = this,
            viewModel = itemViewModel,
        )

        binding.rvSettings.layoutManager = LinearLayoutManager(context)
        binding.rvSettings.adapter = adapter

        binding.rvSettings.viewTreeObserver.addOnDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initLogic() {
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))
    }

    override fun onPause() {
        super.onPause()

        val layoutManager = binding.rvSettings.layoutManager as? LinearLayoutManager ?: return

        val currentPosition = layoutManager.findLastVisibleItemPosition()
        val currentView = layoutManager.findViewByPosition(currentPosition)

        if (currentView != null) {
            rvPositionAndOffset = Pair(currentPosition, currentView.top)
        }

        // Save toolbar state
        viewModel.isToolbarExpanded = Utils.isToolbarExpanded(binding.appBarLayout)
    }

    override fun onResume() {
        super.onResume()

        binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded)

        rvPositionAndOffset = viewModel.rvPositionAndOffset
        rvPositionAndOffset?.let { (position, offset) ->
            val layoutManager =
                binding.rvSettings.layoutManager as? LinearLayoutManager ?: return@let
            layoutManager.scrollToPositionWithOffset(position, offset)
        }
    }
}
