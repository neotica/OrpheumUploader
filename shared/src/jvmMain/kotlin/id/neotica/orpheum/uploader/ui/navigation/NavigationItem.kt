package id.neotica.orpheum.uploader.ui.navigation

enum class MainScreenType {
    UPLOADER,
    TRACK_FEED,
    ALBUM_MANAGER
}

data class NavItem(
    val type: MainScreenType,
    val label: String,
    val indicator: String,
)

val navItems = listOf(
    NavItem(MainScreenType.UPLOADER, "Upload", "📦"),
    NavItem(MainScreenType.TRACK_FEED, "Feed", "📋️"),
    NavItem(MainScreenType.ALBUM_MANAGER, "Album Manager", "\uD83D\uDCBF"),
)