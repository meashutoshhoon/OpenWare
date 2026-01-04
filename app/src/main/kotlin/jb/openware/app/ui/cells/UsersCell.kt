package jb.openware.app.ui.cells

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import jb.openware.app.R
import jb.openware.app.databinding.CellUsersBinding
import jb.openware.app.ui.components.BadgeDrawable
import jb.openware.app.ui.items.UserItem

@SuppressLint("ViewConstructor")
class UsersCell(context: Context) : FrameLayout(context) {

    private val binding = CellUsersBinding.inflate(LayoutInflater.from(context), this, true)

    private val avatarView get() = binding.icon
    private val wordLayout get() = binding.linearWord
    private val wordText get() = binding.txWord
    private val nameText get() = binding.text
    private val badgeView get() = binding.badge

    fun setData(
        userItem: UserItem
    ) {
        handleBadge(userItem.badge, userItem.verified)

        nameText.text = userItem.name

        if (userItem.avatar == "none") {
            avatarView.gone()
            wordLayout.visible()
            wordText.text = userItem.name.firstOrNull()?.uppercase() ?: ""
            wordLayout.background = GradientDrawable().apply {
                cornerRadius = 360f
                setColor(userItem.color.toColorInt())
            }
        } else {
            wordLayout.gone()
            avatarView.visible()
            Glide.with(context).load(userItem.avatar).into(avatarView)
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
                    getBadgeColor()
                )
            }
        }
    }

    fun Context.getThemeColor(attr: Int, fallback: Int = Color.TRANSPARENT): Int {
        return MaterialColors.getColor(this, attr, fallback)
    }

    private fun getBadgeColor(): Int {
        return context.getThemeColor(com.google.android.material.R.attr.colorSecondary)
    }

    private fun View.gone() {
        visibility = GONE
    }

    private fun View.visible() {
        visibility = VISIBLE
    }
}