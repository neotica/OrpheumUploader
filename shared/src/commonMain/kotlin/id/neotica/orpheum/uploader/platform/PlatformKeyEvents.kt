package id.neotica.orpheum.uploader.platform

expect class PlatformKeyEvent {
    val char: Char?
    val isEscape: Boolean
    val isCtrlDown: Boolean
    val isMetaDown: Boolean
}

expect fun installPlatformKeyDispatcher(handler: (PlatformKeyEvent) -> Boolean): () -> Unit
