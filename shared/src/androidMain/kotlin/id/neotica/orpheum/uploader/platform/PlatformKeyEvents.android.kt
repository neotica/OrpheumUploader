package id.neotica.orpheum.uploader.platform

actual class PlatformKeyEvent internal constructor(
    actual val char: Char?,
    actual val isEscape: Boolean,
    actual val isCtrlDown: Boolean,
    actual val isMetaDown: Boolean,
)

actual fun installPlatformKeyDispatcher(handler: (PlatformKeyEvent) -> Boolean): () -> Unit = { }
