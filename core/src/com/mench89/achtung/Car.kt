package com.mench89.achtung

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef

class Car(world: World) {

    private var body: Body
    private var tires: ArrayList<Tire>
    private var frontLeftTireJoint: RevoluteJoint
    private var frontRightTireJoint: RevoluteJoint

    init {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        body = world.createBody(bodyDef)
        // TODO: Need tweak?
        body.angularDamping = 3f

        val vertices = arrayOf(
        Vector2(1.5f, 0f),
        Vector2(3f, 2.5f),
        Vector2(2.8f, 5.5f),
        Vector2(1f, 10f),
        Vector2(-1f, 10f),
        Vector2(-2.8f, 5.5f),
        Vector2(-3f, 2.5f),
        Vector2(-1.5f, 0f))
        val polygonShape = PolygonShape()
        polygonShape.set(vertices)

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
        //tires.add(tires.lastIndex, tire)
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

    fun update(controlState: Tire.TireControlState) {
        for (tire in tires) {
            tire.updateFiction()
        }
        for (tire in tires) {
            tire.updateDrive(controlState)
        }

        // Control steering
        val lockAngle = 35 * WorldConstants.DEGTORAD
        val turnSpeedPerSec = 160 * WorldConstants.DEGTORAD //from lock to lock in 0.5 sec
        val turnPerTimeStep = turnSpeedPerSec / 60.0f
        var desiredAngle = 0f

        when (controlState) {
            Tire.TireControlState.LEFT -> desiredAngle = lockAngle
            Tire.TireControlState.RIGHT -> desiredAngle = -lockAngle
            else -> {} // Do noting
        }

        val angleNow = frontLeftTireJoint.jointAngle
        var angleToTurn = desiredAngle - angleNow
        angleToTurn = MathUtils.clamp(angleToTurn, -turnPerTimeStep, turnPerTimeStep)
        val newAngle = angleNow + angleToTurn
        frontLeftTireJoint.setLimits(newAngle, newAngle)
        frontRightTireJoint.setLimits(newAngle, newAngle)
    }

    /*

     */

}