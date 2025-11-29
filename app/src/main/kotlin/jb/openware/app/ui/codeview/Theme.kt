package jb.openware.app.ui.codeview

class Theme(val name: String) {
    val path: String
        get() = "file:///android_asset/highlightjs/styles/$name.css"

    companion object {
        val AGATE: Theme = Theme("agate")
        @JvmField
        val ANDROIDSTUDIO: Theme = Theme("androidstudio")
        val ARDUINO_LIGHT: Theme = Theme("arduino-light")
        val ARTA: Theme = Theme("arta")
        val ASCETIC: Theme = Theme("ascetic")
        val ATELIER_CAVE_DARK: Theme = Theme("atelier-cave-dark")
        val ATELIER_CAVE_LIGHT: Theme = Theme("atelier-cave-light")
        val ATELIER_DUNE_DARK: Theme = Theme("atelier-dune-dark")
        val ATELIER_DUNE_LIGHT: Theme = Theme("atelier-dune-light")
        val ATELIER_ESTUARY_DARK: Theme = Theme("atelier-estuary-dark")
        val ATELIER_ESTUARY_LIGHT: Theme = Theme("atelier-estuary-light")
        val ATELIER_FOREST_DARK: Theme = Theme("atelier-forest-dark")
        val ATELIER_FOREST_LIGHT: Theme = Theme("atelier-forest-light")
        val ATELIER_HEATH_DARK: Theme = Theme("atelier-heath-dark")
        val ATELIER_HEATH_LIGHT: Theme = Theme("atelier-heath-light")
        val ATELIER_LAKESIDE_DARK: Theme = Theme("atelier-lakeside-dark")
        val ATELIER_LAKESIDE_LIGHT: Theme = Theme("atelier-lakeside-light")
        val ATELIER_PLATEAU_DARK: Theme = Theme("atelier-plateau-dark")
        val ATELIER_PLATEAU_LIGHT: Theme = Theme("atelier-plateau-light")
        val ATELIER_SAVANNA_DARK: Theme = Theme("atelier-savanna-dark")
        val ATELIER_SAVANNA_LIGHT: Theme = Theme("atelier-savanna-light")
        val ATELIER_SEASIDE_DARK: Theme = Theme("atelier-seaside-dark")
        val ATELIER_SEASIDE_LIGHT: Theme = Theme("atelier-seaside-light")
        val ATELIER_SULPHURPOOL_DARK: Theme = Theme("atelier-sulphurpool-dark")
        val ATELIER_SULPHURPOOL_LIGHT: Theme = Theme("atelier-sulphurpool-light")
        val ATOM_ONE_DARK: Theme = Theme("atom-one-dark")
        val ATOM_ONE_LIGHT: Theme = Theme("atom-one-light")
        val BROWN_PAPER: Theme = Theme("brown-paper")
        val CODEPEN_EMBED: Theme = Theme("codepen-embed")
        val COLOR_BREWER: Theme = Theme("color-brewer")
        @JvmField
        val DARCULA: Theme = Theme("darcula")
        @JvmField
        val DARK: Theme = Theme("dark")
        val DARKULA: Theme = Theme("darkula")
        val DEFAULT: Theme = Theme("default")
        val DOCCO: Theme = Theme("docco")
        val DRACULA: Theme = Theme("dracula")
        val FAR: Theme = Theme("far")
        val FOUNDATION: Theme = Theme("foundation")
        @JvmField
        val GITHUB: Theme = Theme("github")
        val GITHUB_GIST: Theme = Theme("github-gist")
        @JvmField
        val GOOGLECODE: Theme = Theme("googlecode")
        val GRAYSCALE: Theme = Theme("grayscale")
        val GRUVBOX_DARK: Theme = Theme("gruvbox-dark")
        val GRUVBOX_LIGHT: Theme = Theme("gruvbox-light")
        val HOPSCOTCH: Theme = Theme("hopscotch")
        @JvmField
        val HYBRID: Theme = Theme("hybrid")
        val IDEA: Theme = Theme("idea")
        val IR_BLACK: Theme = Theme("ir-black")
        val KIMBIE_DARK: Theme = Theme("kimbie.dark")
        val KIMBIE_LIGHT: Theme = Theme("kimbie.light")
        val MAGULA: Theme = Theme("magula")
        val MONO_BLUE: Theme = Theme("mono-blue")
        @JvmField
        val MONOKAI: Theme = Theme("monokai")
        val MONOKAI_SUBLIME: Theme = Theme("monokai-sublime")
        val OBSIDIAN: Theme = Theme("obsidian")
        @JvmField
        val OCEAN: Theme = Theme("ocean")
        val PARAISO_DARK: Theme = Theme("paraiso-dark")
        val PARAISO_LIGHT: Theme = Theme("paraiso-light")
        val POJOAQUE: Theme = Theme("pojoaque")
        val PUREBASIC: Theme = Theme("purebasic")
        val QTCREATOR_DARK: Theme = Theme("qtcreator_dark")
        val QTCREATOR_LIGHT: Theme = Theme("qtcreator_light")
        val RAILSCASTS: Theme = Theme("railscasts")
        val RAINBOW: Theme = Theme("rainbow")
        val SCHOOL_BOOK: Theme = Theme("school-book")
        @JvmField
        val SOLARIZED_DARK: Theme = Theme("solarized-dark")
        val SOLARIZED_LIGHT: Theme = Theme("solarized-light")
        val SUNBURST: Theme = Theme("sunburst")
        val TOMORROW: Theme = Theme("tomorrow")
        val TOMORROW_NIGHT: Theme = Theme("tomorrow-night")
        val TOMORROW_NIGHT_BLUE: Theme = Theme("tomorrow-night-blue")
        val TOMORROW_NIGHT_BRIGHT: Theme = Theme("tomorrow-night-bright")
        val TOMORROW_NIGHT_EIGHTIES: Theme = Theme("tomorrow-night-eighties")
        @JvmField
        val VS: Theme = Theme("vs")
        @JvmField
        val VS2015: Theme = Theme("vs2015")
        @JvmField
        val XCODE: Theme = Theme("xcode")
        val XT256: Theme = Theme("xt256")
        val ZENBURN: Theme = Theme("zenburn")

        val ALL: List<Theme> = listOf(
            AGATE,
            ANDROIDSTUDIO,
            ARDUINO_LIGHT,
            ARTA,
            ASCETIC,
            ATELIER_CAVE_DARK,
            ATELIER_CAVE_LIGHT,
            ATELIER_DUNE_DARK,
            ATELIER_DUNE_LIGHT,
            ATELIER_DUNE_DARK,
            ATELIER_ESTUARY_DARK,
            ATELIER_ESTUARY_LIGHT,
            ATELIER_FOREST_DARK,
            ATELIER_FOREST_DARK,
            ATELIER_FOREST_LIGHT,
            ATELIER_HEATH_DARK,
            ATELIER_HEATH_LIGHT,
            ATELIER_LAKESIDE_DARK,
            ATELIER_LAKESIDE_LIGHT,
            ATELIER_PLATEAU_DARK,
            ATELIER_PLATEAU_LIGHT,
            ATELIER_SAVANNA_DARK,
            ATELIER_SAVANNA_LIGHT,
            ATELIER_SEASIDE_LIGHT,
            ATELIER_SEASIDE_DARK,
            ATELIER_SULPHURPOOL_DARK,
            ATELIER_SULPHURPOOL_LIGHT,
            ATOM_ONE_DARK,
            ATOM_ONE_LIGHT,
            BROWN_PAPER,
            CODEPEN_EMBED,
            COLOR_BREWER,
            DARCULA,
            DARK,
            DARKULA,
            DEFAULT,
            DOCCO,
            DRACULA,
            FAR,
            FOUNDATION,
            GITHUB,
            GITHUB_GIST,
            GOOGLECODE,
            GRAYSCALE,
            GRUVBOX_DARK,
            GRUVBOX_LIGHT,
            HOPSCOTCH,
            HYBRID,
            IDEA,
            IR_BLACK,
            KIMBIE_DARK,
            KIMBIE_LIGHT,
            MAGULA,
            MONO_BLUE,
            MONOKAI,
            MONOKAI_SUBLIME,
            OBSIDIAN,
            OCEAN,
            PARAISO_DARK,
            PARAISO_LIGHT,
            POJOAQUE,
            PUREBASIC,
            QTCREATOR_DARK,
            QTCREATOR_LIGHT,
            RAILSCASTS,
            RAINBOW,
            SCHOOL_BOOK,
            SOLARIZED_DARK,
            SOLARIZED_LIGHT,
            SUNBURST,
            TOMORROW,
            TOMORROW_NIGHT,
            TOMORROW_NIGHT_BLUE,
            TOMORROW_NIGHT_BRIGHT,
            TOMORROW_NIGHT_EIGHTIES,
            VS,
            VS2015,
            XCODE,
            XT256,
            ZENBURN
        )
    }
}
