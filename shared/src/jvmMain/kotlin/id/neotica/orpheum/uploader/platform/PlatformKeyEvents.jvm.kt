package id.neotica.orpheum.uploader.platform

import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent

actual class PlatformKeyEvent internal constructor(
    actual val char: Char?,
    actual val isEscape: Boolean,
    actual val isCtrlDown: Boolean,
    actual val isMetaDown: Boolean,
) {
    internal companion object {
        fun fromAwt(event: KeyEvent): PlatformKeyEvent = PlatformKeyEvent(
            char = if (event.keyChar != KeyEvent.CHAR_UNDEFINED) event.keyChar else null,
            isEscape = event.keyCode == KeyEvent.VK_ESCAPE,
            isCtrlDown = event.isControlDown,
            isMetaDown = event.isMetaDown,
        )
    }
}

actual fun installPlatformKeyDispatcher(handler: (PlatformKeyEvent) -> Boolean): () -> Unit {
    val dispatcher = KeyEventDispatcher { event ->
        if (event.id == KeyEvent.KEY_PRESSED) handler(PlatformKeyEvent.fromAwt(event)) else false
    }
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
    return {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
    }
}
