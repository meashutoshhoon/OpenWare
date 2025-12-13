package jb.openware.app.ui.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jb.openware.app.databinding.ItemSettingsBinding
import jb.openware.app.ui.activity.drawer.AboutUsActivity
import jb.openware.app.ui.activity.drawer.settings.LookAndFeelActivity
import jb.openware.app.ui.items.SettingsItem
import jb.openware.app.ui.viewmodel.settings.SettingsItemViewModel
import jb.openware.app.util.Const
import jb.openware.app.util.HapticUtils
import jb.openware.app.util.Utils
import java.lang.ref.WeakReference

class SettingsAdapter(
    private val settingsList: List<SettingsItem>,
    private val context: Context,
    private val activity: Activity,
    private val viewModel: SettingsItemViewModel
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {
    private val viewMap: MutableMap<String, WeakReference<View>> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = settingsList[position]
        viewMap[item.id] = WeakReference(holder.binding.root)
        holder.bind(item, position == itemCount - 1)
    }

    override fun getItemCount(): Int = settingsList.size

    inner class ViewHolder(val binding: ItemSettingsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(settingsItem: SettingsItem, isLastItem: Boolean) = with(binding) {
            symbolImageView.setImageDrawable(settingsItem.getSymbol(context))
            settingTitle.text = settingsItem.title
            settingDescription.text = settingsItem.description

            settingDescription.visibility =
                if (TextUtils.isEmpty(settingsItem.description)) View.GONE else View.VISIBLE

            setupSwitch(settingsItem)
            setupClickListener(settingsItem)
            setupItemMargin(isLastItem)
        }

        private fun setupSwitch(settingsItem: SettingsItem) = with(binding.settingSwitch) {
            setOnCheckedChangeListener(null)

            visibility = if (settingsItem.hasSwitch) View.VISIBLE else View.GONE
            isChecked = settingsItem.isChecked

            setOnCheckedChangeListener { _, isChecked ->
                settingsItem.isChecked = isChecked
                settingsItem.saveSwitchState()
            }
        }

        private fun setupClickListener(settingsItem: SettingsItem) = with(binding) {
            settingsItemLayout.setOnClickListener { v ->
                HapticUtils.weakVibrate(v)
                handleItemClick(settingsItem.id)
            }
        }

        private fun setupItemMargin(isLastItem: Boolean) {
            val bottom =
                if (isLastItem) Utils.convertDpToPixel(30f, itemView.context).toInt() else 0
            val lp = itemView.layoutParams as? ViewGroup.MarginLayoutParams ?: return
            lp.bottomMargin = bottom
            itemView.layoutParams = lp
        }

        private fun handleItemClick(id: String) {
            when (id) {
                Const.ID_LOOK_AND_FEEL -> {
                    viewModel.scrollPosition = null
                    viewModel.isToolbarExpanded = true

                    val intent = Intent(activity, LookAndFeelActivity::class.java)
                    activity.startActivity(intent)
                }

                Const.ID_ABOUT -> {
                    viewModel.scrollPosition = null
                    viewModel.isToolbarExpanded = true

                    val intent = Intent(activity, AboutUsActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
    }
}
