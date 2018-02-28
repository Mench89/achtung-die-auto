package com.mench89.achtung

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World

class AchtungGame : ApplicationAdapter(), InputHandler.MovementListener {
    override fun onUserKeyDown(keyCode: Int) {
        System.out.println("User pressed a key! " + keyCode)
        /*val position = car.getPosition()
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
        */
        tire.handleInput(keyCode)
    }

    override fun onUserKeyUp(keyCode: Int) {
        System.out.println("User released a key! " + keyCode)
    }

    lateinit var batch: SpriteBatch
    lateinit var img: Texture
    lateinit var car: Player
    lateinit var inputHandler: InputHandler
    lateinit var tire: Tire
    lateinit var world: World
    lateinit var debugRenderer: Box2DDebugRenderer
    lateinit var camera: OrthographicCamera

    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        car = Player(Vector2(0.0f, 0.0f))
        world = World(Vector2(0f, 0f), true)
        tire = Tire(world)
        inputHandler = InputHandler(this)
        Gdx.input.inputProcessor = inputHandler
        debugRenderer = Box2DDebugRenderer()
        camera = OrthographicCamera(100f,100f)
    }

    override fun render() {

        world.step(1/20f,5, 5)

        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 0f, 0f)
        car.draw(batch)
        batch.end()
        debugRenderer.render(world, camera.combined)

        tire.updateFiction()
        //tire.updateDrive(Tire.TireControlState.DOWN)
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}
