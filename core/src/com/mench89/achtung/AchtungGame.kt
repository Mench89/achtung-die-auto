package com.mench89.achtung

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import java.util.HashSet


// TODO: Fix render loop sync.
// TODO: Add different cars.
// TODO: Render car (with colors).
// TODO: Weapon
// TODO: Collision
// TODO: Map
// TODO: Handbrake support.
// TODO: Add gravitation? Stop the car to be completely still.

class AchtungGame : ApplicationAdapter(), InputHandler.MovementListener {
    override fun onUserKeyDown(keyCode: Int) {
        System.out.println("User pressed a key! " + keyCode)
        val controlState = controlStateOfKeyCode(keyCode)
        if (controlState != null) {
            pressedControlStates.add(controlState)
        }
    }

    override fun onUserKeyUp(keyCode: Int) {
        System.out.println("User released a key! " + keyCode)
        pressedControlStates.remove(controlStateOfKeyCode(keyCode))
    }

    lateinit var batch: SpriteBatch
    lateinit var img: Texture
    lateinit var inputHandler: InputHandler
    lateinit var world: World
    lateinit var car: Car
    lateinit var debugRenderer: Box2DDebugRenderer
    lateinit var camera: OrthographicCamera
    lateinit var pressedControlStates: HashSet<Tire.TireControlState>

    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        world = World(Vector2(0f, 0f), true)
        car = Car(world)
        inputHandler = InputHandler(this)
        Gdx.input.inputProcessor = inputHandler
        debugRenderer = Box2DDebugRenderer()
        camera = OrthographicCamera(200f,200f)
        pressedControlStates = HashSet()
    }

    override fun render() {

        world.step(1/20f,5, 5)

        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.end()
        debugRenderer.render(world, camera.combined)

        car.update(pressedControlStates)
        //pressedControlStates.forEach { car.update(it) }
       /* if(pressedControlStates.size > 0) {
            car.update(pressedControlStates.first())
        }
        */
    }

    fun controlStateOfKeyCode(keyCode: Int): Tire.TireControlState? {
        when(keyCode) {
            Input.Keys.UP -> return Tire.TireControlState.UP
            Input.Keys.DOWN -> return Tire.TireControlState.DOWN
            Input.Keys.LEFT -> return Tire.TireControlState.LEFT
            Input.Keys.RIGHT -> return Tire.TireControlState.RIGHT
            else -> {} // Don't handle.
        }

        return null
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}
