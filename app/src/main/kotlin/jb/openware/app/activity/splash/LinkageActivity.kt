package `in`.afi.codekosh.activity.splash

import `in`.afi.codekosh.R
import `in`.afi.codekosh.tools.BaseFragment
import `in`.afi.codekosh.tools.ThemeBuilder

class LinkageActivity: BaseFragment() {

    override fun isHomeFragment(): Boolean {
        return false
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_linkage
    }

    override fun getThemeDescriptions(themeBuilder: ThemeBuilder?) {
    }

    override fun initialize() {
    }

    override fun initializeLogic() {
    }
}