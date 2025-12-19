package jb.openware.app.util

object Const {
    const val DEV_MAIL = "sketchwarechannel@gmail.com"
    const val URL_DEV_GITHUB = "https://github.com/meashutoshhoon"
    const val URL_DEV_BM_COFFEE = "https://github.com/meashutoshhoon"
    const val ID_ABOUT = "id_about"
    const val ID_LOOK_AND_FEEL = "id_look_and_feel"
    const val PREF_SMOOTH_SCROLL = "id_smooth_scroll"
    const val ID_VERSION = "id_version"
    const val ID_CHANGELOGS = "id_changelogs"
    const val ID_REPORT = "id_report"
    const val ID_FEATURE = "id_feature"
    const val ID_GITHUB = "id_github"
    const val ID_TELEGRAM = "id_telegram"
    const val ID_LICENSE = "id_license"

    const val URL_EMAIL_BUG = "mailto:sketchwarechannel@gmail.com?subject=Bug%20Report"

    const val URL_EMAIL_FEATURE = "mailto:sketchwarechannel@gmail.com?subject=Feature%20Suggestion"
    const val URL_GITHUB_REPOSITORY = "https://github.com/meashutoshhoon/OpenWare"
    const val URL_TELEGRAM = "https://t.me/meashutoshhoon"
    const val URL_APP_LICENSE = "https://github.com/meashutoshhoon/OpenWare/blob/master/LICENSE"
    const val RELEASES_URL = "https://github.com/meashutoshhoon/OpenWare/releases"


    enum class Contributors(
        val displayName: String, val githubUrl: String
    ) {
        ASHUTOSH("Ashutosh Gupta", "https://github.com/meashutoshhoon"), ANKIT(
            "Ankit Goyal", "https://github.com/Ankit-Goyal012"
        ),
        ANUSHKA("Anushka Shrivastava", "https://github.com/MeAnushkaHoon"), ATHARV(
            "Atharv Puranik", "https://github.com/atpk2005"
        ),
    }

}