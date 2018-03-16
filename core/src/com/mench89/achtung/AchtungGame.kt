package com.mench89.achtung

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World


// TODO: Fix render loop sync.
// TODO: Add different cars.
// TODO: Weapon
// TODO: Map
// TODO: Handbrake support.
// TODO: Add gravitation? Stop the car to be completely still.
// TODO: Tweak steering, feels a little "slow"
// TODO: Start position bug, wheels and car are rotating/spinning the first couple of frames.

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
    lateinit var polygonSpriteBatch: PolygonSpriteBatch
    lateinit var img: Texture
    lateinit var inputHandler: InputHandler
    lateinit var world: World
    lateinit var car: Car
    lateinit var car2: Car
    lateinit var car3: Car
    lateinit var debugRenderer: Box2DDebugRenderer
    lateinit var camera: OrthographicCamera
    lateinit var pressedControlStates: HashSet<Tire.TireControlState>
    lateinit var walls: ArrayList<Wall>

    val mapWidth = 150f
    val mapHeight = 150f
    val wallThickness = 2f

    override fun create() {
        batch = SpriteBatch()
        polygonSpriteBatch = PolygonSpriteBatch()
        img = Texture("badlogic.jpg")
        world = World(Vector2(0f, 0f), true)
        car = Car(world)
        car2 = Car(world, Color(1f, 1f, 0f, 1f),  Vector2(40f, 20f))
        car3 = Car(world, Color(1f, 0f, 1f, 1f), Vector2(-30f, -25f))
        inputHandler = InputHandler(this)
        Gdx.input.inputProcessor = inputHandler
        debugRenderer = Box2DDebugRenderer()
        camera = OrthographicCamera(150f,150f)
        pressedControlStates = HashSet()
        walls = ArrayList()
        walls.add(Wall(world, Vector2(-mapWidth/2 + (wallThickness / 2),0f), Vector2(wallThickness, mapHeight)))
        walls.add(Wall(world, Vector2(0f,-mapHeight/2 + wallThickness/2), Vector2(mapWidth, wallThickness)))
        walls.add(Wall(world, Vector2(+mapWidth/2 - (wallThickness / 2),0f), Vector2(wallThickness, mapHeight)))
        walls.add(Wall(world, Vector2(0f,mapHeight/2 -wallThickness/2), Vector2(mapWidth, wallThickness)))

    }

    override fun render() {
        batch.projectionMatrix = camera.combined
        polygonSpriteBatch.projectionMatrix = camera.combined


        Gdx.gl.glClearColor(0f, 0.5f, 0.5f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.end()
        polygonSpriteBatch.begin()
        car.draw(polygonSpriteBatch)
        car2.draw(polygonSpriteBatch)
        car3.draw(polygonSpriteBatch)
        for (wall in walls) {
            wall.draw(polygonSpriteBatch)
        }
        polygonSpriteBatch.end()
        //debugRenderer.render(world, camera.combined)

        // TODO: Something is weird here
        car.update(pressedControlStates)
        world.step(1/20f,5, 5)
        val emptyHashSet = HashSet<Tire.TireControlState>()
        car2.update(emptyHashSet)
        car2.update(emptyHashSet)
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
        polygonSpriteBatch.dispose()
        img.dispose()
    }
}
