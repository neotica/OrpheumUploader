package id.neotica.orpheum.uploader.ui.navigation

data class NavItem(
    val screen: Screen,
    val label: String,
    val indicator: String,
)

val navItems = listOf(
    NavItem(Screen.Upload, "Upload", "\uD83D\uDCE6"),
    NavItem(Screen.Search, "Search", "\uD83D\uDD0D"),
    NavItem(Screen.Feed, "Feed", "\uD83D\uDCCB"),
    NavItem(Screen.Albums, "Albums", "\uD83D\uDCBF"),
    NavItem(Screen.Account, "Account", "\uD83D\uDC64"),
)
