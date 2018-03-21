package com.mench89.achtung

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.PolygonSprite
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.EarClippingTriangulator
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef

class Car(world: World, color: Color, position: Vector2) {
    constructor(world: World) : this(world, Color(1f, 0f, 0f, 1f), Vector2(10f, 10f))

    private var body: Body
    private var tires: ArrayList<Tire>
    private var frontLeftTireJoint: RevoluteJoint
    private var frontRightTireJoint: RevoluteJoint
    private val texture: Texture
    private val polygonSprite: PolygonSprite
    // We can't use the width and height from the polygon sprite, it will always be 1, most likely because it contains
    // a complex structure of vertices.
    private val width = 6f
    private val height = 10f

    init {
        val bodyDef = BodyDef()
        // Set start position
        bodyDef.position.set(position)
        bodyDef.type = BodyDef.BodyType.DynamicBody
        body = world.createBody(bodyDef)
        // TODO: Need tweak?
        body.angularDamping = 3f

        val vertices = floatArrayOf(
        1.5f, 0f,
        3f, 2.5f,
        2.8f, 5.5f,
        1f, 10f,
        -1f, 10f,
        -2.8f, 5.5f,
        -3f, 2.5f,
        -1.5f, 0f)

        // Creating the color filling (but textures would work the same way)
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color)
        pix.fill()
        texture = Texture(pix)
        val earClippingTriangulator = EarClippingTriangulator()
        val shortArray = earClippingTriangulator.computeTriangles(vertices)
        val polygonRegion = PolygonRegion(TextureRegion(texture), vertices, shortArray.items)
        polygonSprite = PolygonSprite(polygonRegion)
        polygonSprite.setOrigin(0f, 0f)

        val polygonShape = PolygonShape()
        polygonShape.set(vertices)
        val fixture = body.createFixture(polygonShape, 0.1f) //shape, density

        val jointDef = RevoluteJointDef()
        jointDef.bodyA = body
        jointDef.enableLimit = true
        jointDef.lowerAngle = 0f
        jointDef.upperAngle = 0f
        jointDef.localAnchorB.setZero() // Center of tire

        val maxForwardSpeed = 250f
        val maxBackwardSpeed = -40f
        val backTireMaxDriveForce = 300f
        val frontTireMaxDriveForce = 500f
        val backTireMaxLateralImpulse = 8.5f
        val frontTireMaxLateralImpulse = 7.5f

        tires = ArrayList()

        // Back left tire
        var tire = Tire(world)
        tire.setCharacteristics(maxForwardSpeed, maxBackwardSpeed, backTireMaxDriveForce, backTireMaxLateralImpulse)
        jointDef.bodyB = tire.body
        jointDef.localAnchorA.set(-3f, 0.75f)
        world.createJoint(jointDef)
        tires.add(tire)

        // Back right tire
        tire = Tire(world)
        tire.setCharacteristics(maxForwardSpeed, maxBackwardSpeed, backTireMaxDriveForce, backTireMaxLateralImpulse)
        jointDef.bodyB = tire.body
        jointDef.localAnchorA.set(3f, 0.75f)
        world.createJoint(jointDef)
        tires.add(tire)

        // Front left tire
        tire = Tire(world)
        tire.setCharacteristics(maxForwardSpeed, maxBackwardSpeed, frontTireMaxDriveForce, frontTireMaxLateralImpulse)
        jointDef.bodyB = tire.body
        jointDef.localAnchorA.set(-3f, 8.75f)
        frontLeftTireJoint = world.createJoint(jointDef) as RevoluteJoint
        tires.add(tire)

        // Front right tire
        tire = Tire(world)
        tire.setCharacteristics(maxForwardSpeed, maxBackwardSpeed, frontTireMaxDriveForce, frontTireMaxLateralImpulse)
        jointDef.bodyB = tire.body
        jointDef.localAnchorA.set(3f, 8.75f)
        frontRightTireJoint = world.createJoint(jointDef) as RevoluteJoint
        tires.add(tire)

    }

    fun update(controlStates: HashSet<Tire.TireControlState>) {
        for (tire in tires) {
            tire.updateFiction()
        }
        for (tire in tires) {
            tire.updateDrive(controlStates)
        }

        // Control steering
        val lockAngle = 35 * WorldConstants.DEGTORAD
        val turnSpeedPerSec = 160 * WorldConstants.DEGTORAD //from lock to lock in 0.5 sec
        val turnPerTimeStep = turnSpeedPerSec / 60.0f
        var desiredAngle = 0f

        for (controlState in controlStates) {
            when (controlState) {
                Tire.TireControlState.LEFT -> desiredAngle = lockAngle
                Tire.TireControlState.RIGHT -> desiredAngle = -lockAngle
                else -> { /* Do noting */ }
            }
        }

        val angleNow = frontLeftTireJoint.jointAngle
        var angleToTurn = desiredAngle - angleNow
        angleToTurn = MathUtils.clamp(angleToTurn, -turnPerTimeStep, turnPerTimeStep)
        val newAngle = angleNow + angleToTurn
        frontLeftTireJoint.setLimits(newAngle, newAngle)
        frontRightTireJoint.setLimits(newAngle, newAngle)
    }

    fun getPosition(): Vector2 {
        return body.position
    }

    /**
     * Get the position at the front of the car.
     */
    fun getNosePosition(): Vector2 {
        val currentForwardNormal = Vector2(body.getWorldVector(Vector2(0f, 1f)))
        // Use the half height of the car to reach the front of the car, because we currently have the center point of the car.
        val nosePoint = currentForwardNormal.scl(height/2)

        return Vector2(body.worldCenter.x + nosePoint.x, body.worldCenter.y + nosePoint.y)
    }

    fun getAngle(): Float {
        return body.angle
    }

    fun  draw(polySpriteBatch: PolygonSpriteBatch) {
        polygonSprite.setPosition(body.position.x, body.position.y)
        polygonSprite.rotation = body.angle * WorldConstants.RADTODEG
        polygonSprite.draw(polySpriteBatch)
        for (tire in tires) {
            tire.draw(polySpriteBatch)
        }
    }
}