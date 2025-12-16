package jb.openware.app.ui.codeview

class Theme(val name: String) {
    val path: String
        get() = "file:///android_asset/highlightjs/styles/$name.css"

    companion object {
        @JvmField
        val ANDROIDSTUDIO: Theme = Theme("androidstudio")

        val ALL: List<Theme> = listOf(
            ANDROIDSTUDIO,
        )
    }
}
