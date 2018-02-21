package com.mench89.achtung

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

class AchtungGame : ApplicationAdapter(), InputHandler.MovementListener {
    override fun onUserKeyDown(keyCode: Int) {
        System.out.println("User pressed a key! " + keyCode)
        val position = car.getPosition()
        if(keyCode == Input.Keys.UP) {
            position.y += 1
        }
        if(keyCode == Input.Keys.DOWN) {
            position.y -= 1
        }
        if(keyCode == Input.Keys.LEFT) {
            position.x -= 1
        }
        if(keyCode == Input.Keys.RIGHT) {
            position.x += 1
        }
        car.setPosition(position)
    }

    override fun onUserKeyUp(keyCode: Int) {
        System.out.println("User released a key! " + keyCode)
    }

    lateinit var batch: SpriteBatch
    lateinit var img: Texture
    lateinit var car: Player
    lateinit var inputHandler: InputHandler

    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        car = Player(Vector2(0.0f, 0.0f))
        inputHandler = InputHandler(this)
        Gdx.input.inputProcessor = inputHandler
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 0f, 0f)
        car.draw(batch)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}
