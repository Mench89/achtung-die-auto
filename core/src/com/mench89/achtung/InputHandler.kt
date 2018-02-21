package com.mench89.achtung

import com.badlogic.gdx.InputProcessor

/**
 * Abstracts the input handling.
 */
class InputHandler(private val listener: MovementListener) : InputProcessor {

    /**
     * Interface to report input done by the user.
     */
    interface MovementListener {

        /**
         * Called when user press a key down.
         */
        fun onUserKeyDown(keyCode: Int)

        /**
         * Called when user releases a key.
         */
        fun onUserKeyUp(keyCode: Int)
    }

    override fun keyDown(keycode: Int): Boolean {
        listener.onUserKeyDown(keycode)
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        listener.onUserKeyUp(keycode)
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        // Do nothing.
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        // Do nothing, this is mouse input.
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        // Do nothing, this is mouse input.
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        // Do nothing, this is mouse input.
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        // Do nothing, this is mouse input.
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        // Do nothing, this is mouse input.
        return false
    }
}