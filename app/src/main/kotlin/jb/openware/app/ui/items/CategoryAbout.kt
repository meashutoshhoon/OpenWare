package jb.openware.app.ui.items

import androidx.annotation.DrawableRes
import jb.openware.app.util.Const

data class CategoryAbout(
    val name: String
) {

    data class LeadDeveloperItem(
        val title: String, val description: String, @DrawableRes val imageRes: Int
    )

    data class ContributorsItem(
        val id: Const.Contributors,
        val title: String,
        val description: String,
        @DrawableRes val imageRes: Int
    )

    data class AppItem(
        val id: String, val title: String, val description: String, @DrawableRes val imageRes: Int
    )
}