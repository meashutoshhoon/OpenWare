package jb.openware.app.ui.cells

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import jb.openware.app.R
import jb.openware.app.databinding.CellUsersBinding
import jb.openware.app.ui.components.BadgeDrawable
import androidx.core.graphics.toColorInt

@SuppressLint("ViewConstructor")
class UsersCell(context: Activity) : FrameLayout(context) {

    private val binding = CellUsersBinding.inflate(LayoutInflater.from(context), this, true)

    private val avatarView get() = binding.icon
    private val wordLayout get() = binding.linearWord
    private val wordText get() = binding.txWord
    private val nameText get() = binding.text
    private val badgeView get() = binding.badge

    private fun isNightMode(): Boolean {
        val mask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mask == Configuration.UI_MODE_NIGHT_YES
    }

    fun setData(
        avatar: String,
        name: String,
        color: String,
        badgeValue: String?,
        verifiedValue: String?
    ) {
        handleBadge(badgeValue, verifiedValue)

        nameText.text = name

        if (avatar == "none") {
            avatarView.gone()
            wordLayout.visible()
            wordText.text = name.firstOrNull()?.uppercase() ?: ""
            wordLayout.background = GradientDrawable().apply {
                cornerRadius = 360f
                setColor(color.toColorInt())
            }
        } else {
            wordLayout.gone()
            avatarView.visible()
            Glide.with(context).load(avatar).into(avatarView)
        }
    }

    private fun handleBadge(badgeValue: String?, verifiedValue: String?) {
        if (badgeValue == null || verifiedValue == null) {
            badgeView.gone()
            return
        }

        val badgeInt = badgeValue.toIntOrNull() ?: 0
        val isVerified = verifiedValue.toBoolean()

        when (badgeInt) {
            0 if isVerified -> {
                badgeView.visible()
                badgeView.setImageResource(R.drawable.verify)
                badgeView.setColorFilter(0xFF00C853.toInt(), PorterDuff.Mode.SRC_IN)
                nameText.setTextColor(0xFF00C853.toInt())
            }
            0 -> {
                badgeView.gone()
            }
            else -> {
                badgeView.visible()
                BadgeDrawable(context).setBadge(badgeInt.toString(), badgeView)
                nameText.setTextColor(
                    if (isNightMode()) 0xFF8DCDFF.toInt()
                    else 0xFF006493.toInt()
                )
            }
        }
    }

    private fun View.gone() { visibility = GONE }
    private fun View.visible() { visibility = VISIBLE }
}