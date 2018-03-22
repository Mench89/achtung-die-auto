package com.mench89.achtung

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*


// TODO: Fix render loop sync.
// TODO: Add different cars.
// TODO: Weapon
// TODO: Map
// TODO: Handbrake support.
// TODO: Add gravitation? Stop the car to be completely still.
// TODO: Tweak steering, feels a little "slow"
// TODO: Start position bug, wheels and car are rotating/spinning the first couple of frames.
// TODO: Add restart function for debugging
// TODO: UI for showing score
// TODO: Audio
// TODO: Add "death" state to car

class AchtungGame : ApplicationAdapter(), InputHandler.MovementListener {
    override fun onUserKeyDown(keyCode: Int) {
        System.out.println("User pressed a key! " + keyCode)
        val controlState = controlStateFromKeyCode(keyCode)
        if (controlState != null) {
            pressedControlStates.add(controlState)
        }

        if (keyCode == Input.Keys.SPACE) {
            val bulletHeight = 3f
            // We want the start position to start at the front of the car and additional with the half height of the bullet to not collied with the
            // shooting car.
            bullets.add(Bullet(world, getNosePosition(car.getBody(), (car.getHeight() / 2) + 1 + (bulletHeight / 2)), Vector2(1f, bulletHeight), car.getAngle()))
        }
    }

    override fun onUserKeyUp(keyCode: Int) {
        System.out.println("User released a key! " + keyCode)
        pressedControlStates.remove(controlStateFromKeyCode(keyCode))
    }

    private lateinit var polygonSpriteBatch: PolygonSpriteBatch
    private lateinit var img: Texture
    private lateinit var inputHandler: InputHandler
    private lateinit var world: World
    private lateinit var car: Car
    private lateinit var car2: Car
    private lateinit var car3: Car
    private lateinit var debugRenderer: Box2DDebugRenderer
    private lateinit var camera: OrthographicCamera
    private lateinit var pressedControlStates: HashSet<Tire.TireControlState>
    private lateinit var walls: ArrayList<Wall>
    private lateinit var bullets: ArrayList<Bullet>

    private val mapWidth = 100f
    private val mapHeight = 100f
    private val wallThickness = 2f

    override fun create() {
        polygonSpriteBatch = PolygonSpriteBatch()
        img = Texture("badlogic.jpg")
        world = World(Vector2(0f, 0f), true)
        car = Car(world)
        car2 = Car(world, Color(1f, 1f, 0f, 1f),  Vector2(40f, 20f))
        car3 = Car(world, Color(1f, 0f, 1f, 1f), Vector2(-30f, -25f))
        inputHandler = InputHandler(this)
        Gdx.input.inputProcessor = inputHandler
        debugRenderer = Box2DDebugRenderer()
        camera = OrthographicCamera(mapWidth,mapHeight)
        pressedControlStates = HashSet()
        walls = ArrayList()
        walls.add(Wall(world, Vector2(-mapWidth/2 + (wallThickness / 2),0f), Vector2(wallThickness, mapHeight)))
        walls.add(Wall(world, Vector2(0f,-mapHeight/2 + wallThickness/2), Vector2(mapWidth, wallThickness)))
        walls.add(Wall(world, Vector2(+mapWidth/2 - (wallThickness / 2),0f), Vector2(wallThickness, mapHeight)))
        walls.add(Wall(world, Vector2(0f,mapHeight/2 -wallThickness/2), Vector2(mapWidth, wallThickness)))
        bullets = ArrayList()

        world.setContactListener(object : ContactListener {
            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
                // Do nothing
            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
                // Do nothing
            }

            override fun endContact(contact: Contact?) {

            }

            override fun beginContact(contact: Contact?) {
                val objectA = contact?.fixtureA?.body?.userData as? Bullet
                if(objectA != null) {
                    objectA.shouldDie = true
                }
                val objectB = contact?.fixtureB?.body?.userData as? Bullet
                if(objectB != null) {
                    objectB.shouldDie = true
                }
            }
        })

    }

    override fun render() {
        polygonSpriteBatch.projectionMatrix = camera.combined

        Gdx.gl.glClearColor(0f, 0.5f, 0.5f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        polygonSpriteBatch.begin()
        car.draw(polygonSpriteBatch)
        car2.draw(polygonSpriteBatch)
        car3.draw(polygonSpriteBatch)
        for (wall in walls) {
            wall.draw(polygonSpriteBatch)
        }
            val bulletsToDestroy = ArrayList<Bullet>()
        for (bullet in bullets) {
            // TODO: Destroy and dealloc resources used by bullet
                bullet.draw(polygonSpriteBatch)
            if (bullet.shouldDie) {
                bulletsToDestroy.add(bullet)
            }
        }
        bullets.removeAll(bulletsToDestroy)
        for (bullet in bulletsToDestroy) {
            bullet.destroy(world)
        }
        polygonSpriteBatch.end()
        // Uncomment to show hit boxes.
        debugRenderer.render(world, camera.combined)

        car.update(pressedControlStates)
        world.step(1/20f,5, 5)
        val emptyHashSet = HashSet<Tire.TireControlState>()
        car2.update(emptyHashSet)
        car2.update(emptyHashSet)
    }

    override fun dispose() {
        polygonSpriteBatch.dispose()
        img.dispose()
    }

    private fun controlStateFromKeyCode(keyCode: Int): Tire.TireControlState? {
        when(keyCode) {
            Input.Keys.UP -> return Tire.TireControlState.UP
            Input.Keys.DOWN -> return Tire.TireControlState.DOWN
            Input.Keys.LEFT -> return Tire.TireControlState.LEFT
            Input.Keys.RIGHT -> return Tire.TireControlState.RIGHT
            else -> {} // Don't handle.
        }

        return null
    }

    private fun getNosePosition(body: Body, length: Float): Vector2 {
        val currentForwardNormal = Vector2(body.getWorldVector(Vector2(0f, 1f)))
        // Use the half height of the object to reach the front of the object, because we currently have the center point of the object.
        val nosePoint = currentForwardNormal.scl(length)

        return Vector2(body.worldCenter.x + nosePoint.x, body.worldCenter.y + nosePoint.y)
    }
}
