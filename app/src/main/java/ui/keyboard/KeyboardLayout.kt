package ui.keyboard

/**
 * Keyboard layout types
 */
sealed class KeyboardLayout {
    /**
     * Cangjie root layout (default)
     */
    object Cangjie : KeyboardLayout()

    /**
     * Punctuation symbol layout
     */
    object Punctuation : KeyboardLayout()

    /**
     * English QWERTY layout
     */
    object English : KeyboardLayout()
}
