package jb.openware.app.ui.activity.drawer.logs

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import jb.openware.app.databinding.ActivityChangeLogBinding
import jb.openware.app.ui.activity.drawer.logs.fragments.ChangeLogFragment
import jb.openware.app.ui.activity.drawer.logs.fragments.UpdateLogFragment
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.util.Utils

class ChangeLogActivity :
    BaseActivity<ActivityChangeLogBinding>(ActivityChangeLogBinding::inflate) {

    override fun init() {
        val adapter = AboutAdapter(this)
        binding.viewPager.setOffscreenPageLimit(2)
        binding.viewPager.setAdapter(adapter)

        val tabTitles = listOf(
            "ChangeLog", "UpdateLog"
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles.getOrNull(position).orEmpty()
        }.attach()
    }

    override fun initLogic() {
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))
    }

    class AboutAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            1 -> UpdateLogFragment()
            else -> ChangeLogFragment()
        }
    }

}