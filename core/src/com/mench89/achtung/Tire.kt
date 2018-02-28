package com.mench89.achtung

import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*

class Tire(world: World) {

    enum class TireControlState {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private val body: Body
    private var currentTraction: Float
    private var maxForwardSpeed = 250f
    private var maxBackwardSpeed = -40f
    private var maxDriveForce = 500f
    private var maxLateralImpulse = 8.5f
    // GroundAreaFUD *groundAreas.

    // TODO: Need a method or something to destruct the resources.
    init {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        body = world.createBody(bodyDef)

        val shape = PolygonShape()
        shape.setAsBox(0.5f, 1.25f)
        body.setTransform(10f, 10f, 0f)

        val fixture = body.createFixture(shape, 1f) // Shape density
        // fixture->SetUserData( new CarTireFUD() );

        body.userData = this

        currentTraction = 1f
    }

    fun setCharacteristics(maxForwardSpeed: Float, maxBackwardSpeed: Float, maxDriveForce: Float, maxLateralImpulse: Float) {
        this.maxForwardSpeed = maxForwardSpeed
        this.maxBackwardSpeed = maxBackwardSpeed
        this.maxDriveForce = maxDriveForce
        this.maxLateralImpulse = maxLateralImpulse
    }

    fun updateTraction() {
        // TODO: Handle traction
        currentTraction = 0.2f
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
        var impulse = getLeteralVelocity().scl(-body.mass)
        if(impulse.len() > maxLateralImpulse) {
            impulse = impulse.scl(maxLateralImpulse / impulse.len())
        }
        body.applyLinearImpulse(impulse.scl(currentTraction), body.worldCenter, true)

        // Angular velocity
        // TODO: Konstant?
        body.applyAngularImpulse( currentTraction * 0.1f * body.inertia * -body.angularVelocity, true)

        // Forward linear velocity
        val currentForwardNormal = getForwardVelocity()
        // TODO: Is this the same as normilzed?
        val currentForwardSpeed = currentForwardNormal.len()
        /// TODO: Konstant?
        val dragForceMagnitude = currentForwardSpeed * -2f
        body.applyForce(currentForwardNormal.scl(currentTraction * dragForceMagnitude), body.worldCenter, true)
    }

    fun updateDrive(controlState: TireControlState) {
        // Find desired speed
        var desiredSpeed = 0f
        when (controlState) {
            TireControlState.UP -> desiredSpeed = maxForwardSpeed
            TireControlState.DOWN -> desiredSpeed = maxBackwardSpeed
            else -> {} // Do nothing.
        }

        // Find current speed in forward direction
        val currentForwardNormal = Vector2(body.getWorldVector(Vector2(0f, 1f)))
        val currentSpeed = getForwardVelocity().dot(currentForwardNormal)

        // Apply necessary force
        var force = 0f
        if(desiredSpeed > currentSpeed) {
            force = maxDriveForce
        } else if (desiredSpeed < currentSpeed) {
            force = -maxDriveForce
        } else {
            return
        }

        body.applyForce(currentForwardNormal.scl(currentTraction * force), body.worldCenter, true)
    }

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

    fun handleInput(keyCode: Int) {
        when(keyCode) {
            Input.Keys.UP -> updateDrive(TireControlState.UP)
            Input.Keys.DOWN -> updateDrive(TireControlState.DOWN)
            Input.Keys.LEFT -> updateTurn(TireControlState.LEFT)
            Input.Keys.RIGHT -> updateTurn(TireControlState.RIGHT)
            else -> {} // Don't handle.
        }
    }
}