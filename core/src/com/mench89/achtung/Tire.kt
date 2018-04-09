package com.mench89.achtung

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World

class Tire(world: World) {

    enum class TireControlState {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    val body: Body
    private var currentTraction: Float
    private var currentDrag: Float

    private var maxForwardSpeed = 500f
    private var maxBackwardSpeed = -40f
    private var maxDriveForce = 750f
    private var maxLateralImpulse = 12.5f

    private val texture: Texture
    private val sprite: Sprite
    // GroundAreaFUD *groundAreas.

    // TODO: Need a method or something to destruct the resources.
    init {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        body = world.createBody(bodyDef)

        val shape = PolygonShape()
        shape.setAsBox(0.5f, 1.25f)
        //body.setTransform(10f, 10f, 0f)

        val fixture = body.createFixture(shape, 1f) // Shape density
        // fixture->SetUserData( new CarTireFUD() );

        // Creating the color filling (but textures would work the same way)
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 1f) // DE is red, AD is green and BE is blue.
        pix.fill()
        texture = Texture(pix)
        sprite = Sprite(texture)
        sprite.setSize(1f, 2.5f)
        sprite.setOrigin( sprite.width/2, sprite.height/2)

        body.userData = this

        currentTraction = 1f
        currentDrag = 1f
    }

    fun setCharacteristics(maxForwardSpeed: Float, maxBackwardSpeed: Float, maxDriveForce: Float, maxLateralImpulse: Float) {
        this.maxForwardSpeed = maxForwardSpeed
        this.maxBackwardSpeed = maxBackwardSpeed
        this.maxDriveForce = maxDriveForce
        this.maxLateralImpulse = maxLateralImpulse
    }

    fun updateTraction() {
        // TODO: Handle traction
        currentTraction = 1f
//        currentDrag
    }

    fun getLeteralVelocity(): Vector2 {
        val currentRightNormal = Vector2(body.getWorldVector(Vector2(1f, 0f)))
        // TODO: Kontrollera uträkning.
        return currentRightNormal.scl(currentRightNormal.dot(body.linearVelocity))
    }

    fun getForwardVelocity(): Vector2 {
        val currentForwardNormal = Vector2(body.getWorldVector(Vector2(0f, 1f)))
        // TODO: Kontrollera uträkning.
        return currentForwardNormal.scl(currentForwardNormal.dot(body.linearVelocity))
    }

    fun updateFiction() {
        // Lateral linear velocity
        // TODO: Kontrollera uträkning, har vänt negativitet på velocity och mass.
       /* var impulse = getLeteralVelocity().scl(-body.mass)
        if(impulse.len() > maxLateralImpulse) {
            impulse = impulse.scl(maxLateralImpulse / impulse.len())
        }
        body.applyLinearImpulse(impulse.scl(currentTraction), body.worldCenter, true) */

        // Angular velocity
        // TODO: Konstant?
        body.applyAngularImpulse( currentTraction * 0.1f * body.inertia * -body.angularVelocity, true)

        // Forward linear velocity
        val currentForwardNormal = getForwardVelocity()
        // TODO: Is this the same as normilzed?
        val currentForwardSpeed = currentForwardNormal.len()
        /// TODO: Konstant?
        var dragForceMagnitude = currentForwardSpeed * -0.25f
        dragForceMagnitude *= currentDrag
        body.applyForce(currentForwardNormal.scl(currentTraction * dragForceMagnitude), body.worldCenter, true)
    }

    fun updateDrive(controlStates: HashSet<TireControlState>) {
        // Find desired speed
        var desiredSpeed = 0f
        for (controlState in controlStates) {
            when (controlState) {
                TireControlState.UP -> desiredSpeed = maxForwardSpeed
                TireControlState.DOWN -> desiredSpeed = maxBackwardSpeed
                else -> {
                } // Do nothing.
            }
        }

        // Find current speed in forward direction
        val currentForwardNormal = Vector2(body.getWorldVector(Vector2(0f, 1f)))
        val currentSpeed = getForwardVelocity().dot(currentForwardNormal)

        // Apply necessary force
        var force = 0f
        if (controlStates.contains(TireControlState.UP) || controlStates.contains(TireControlState.DOWN)) {
            if (desiredSpeed > currentSpeed) {
                force = maxDriveForce
            } else if (desiredSpeed < currentSpeed) {
                force = -maxDriveForce * 0.5f
            } else {
                // Do nothing.
            }
        }

        //body.applyForce(currentForwardNormal.scl(currentTraction * force), body.worldCenter, true)
        val speedFactor = currentSpeed / 120f
        var driveImpulse = currentForwardNormal.scl((force / 60.0f))
        if (driveImpulse.len() > maxLateralImpulse) {
            driveImpulse = driveImpulse.scl(maxLateralImpulse / driveImpulse.len())
        }

        // TODO: Kontrollera uträkning, har vänt negativitet på velocity och mass.
        val lateralFrictionImpulse = getLeteralVelocity().scl(-body.mass)
        var laterImpulseAvailable = maxLateralImpulse
        laterImpulseAvailable *= 2.0f * speedFactor
        if(laterImpulseAvailable < 0.5f * maxLateralImpulse) {
            laterImpulseAvailable = 0.5f * maxLateralImpulse
        }
        if (lateralFrictionImpulse.len() > laterImpulseAvailable) {
            lateralFrictionImpulse.scl(laterImpulseAvailable / lateralFrictionImpulse.len())
        }
        //m_lastDriveImpulse = driveImpulse.Length();
        //m_lastLateralFrictionImpulse = lateralFrictionImpulse.Length();

        val impulse = Vector2(driveImpulse.x + lateralFrictionImpulse.x, driveImpulse.y + lateralFrictionImpulse.y)
        if (impulse.len() > maxLateralImpulse) {
            impulse.scl(maxLateralImpulse / impulse.len())
        }
        body.applyLinearImpulse(impulse.scl(currentTraction), body.worldCenter, true)

    }

    fun  draw(polySpriteBatch: PolygonSpriteBatch) {
        sprite.setCenter(body.position.x, body.position.y)
        sprite.rotation = body.angle * WorldConstants.RADTODEG
        sprite.draw(polySpriteBatch)
    }


    /*
    fun updateTurn(controlState: TireControlState) {
        var desiredTorque = 0f
        when (controlState) {
            // TODO: Använd +=? Vad händer annars om man håller inne båda?
            TireControlState.LEFT -> desiredTorque = 15f
            TireControlState.RIGHT -> desiredTorque = -15f
            else -> {} // Do noting.
        }
        body.applyTorque(desiredTorque, true)
    }
    */


}