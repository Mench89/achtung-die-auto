package com.mench89.achtung

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.PolygonSprite
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import com.badlogic.gdx.math.Interpolation.circle
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.EarClippingTriangulator
import com.sun.tools.internal.xjc.reader.Ring.begin



class Car(world: World) {

    private var body: Body
    private var tires: ArrayList<Tire>
    private var frontLeftTireJoint: RevoluteJoint
    private var frontRightTireJoint: RevoluteJoint
    private val texture: Texture
    private val polygonSprite: PolygonSprite

    init {


        val bodyDef = BodyDef()
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
        pix.setColor(1f, 0f, 0f, 1f) // DE is red, AD is green and BE is blue.
        pix.fill()
        texture = Texture(pix)
        val earClippingTriangulator = EarClippingTriangulator()
        val shortArray = earClippingTriangulator.computeTriangles(vertices)
        val polygonRegion = PolygonRegion(TextureRegion(texture), vertices, shortArray.items)
        polygonSprite = PolygonSprite(polygonRegion)
        polygonSprite.setOrigin(0f, 0f)
        // TODO: Set poly.origin?


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
                else -> {
                } // Do noting
            }
        }

        val angleNow = frontLeftTireJoint.jointAngle
        var angleToTurn = desiredAngle - angleNow
        angleToTurn = MathUtils.clamp(angleToTurn, -turnPerTimeStep, turnPerTimeStep)
        val newAngle = angleNow + angleToTurn
        frontLeftTireJoint.setLimits(newAngle, newAngle)
        frontRightTireJoint.setLimits(newAngle, newAngle)
    }

    fun  draw(polySpriteBatch: PolygonSpriteBatch) {
        polygonSprite.setPosition(body.position.x, body.position.y)

        polygonSprite.rotation = body.angle * WorldConstants.RADTODEG
        // TODO: Blir det rätt med .region?
        //polySpriteBatch.draw(polygonSprite.region, body.position.x, body.position.y)
        polygonSprite.draw(polySpriteBatch)
        for (tire in tires) {
            tire.draw(polySpriteBatch)
        }
    }

    /*
PolygonSprite poly;
PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning
Texture textureSolid;

// Creating the color filling (but textures would work the same way)
Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
pix.setColor(0xDEADBEFF); // DE is red, AD is green and BE is blue.
pix.fill();
textureSolid = new Texture(pix);
PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid),
  new float[] {      // Four vertices
    0, 0,            // Vertex 0         3--2
    100, 0,          // Vertex 1         | /|
    100, 100,        // Vertex 2         |/ |
    0, 100           // Vertex 3         0--1
}, new short[] {
    0, 1, 2,         // Two triangles using vertex indices.
    0, 2, 3          // Take care of the counter-clockwise direction.
});
poly = new PolygonSprite(polyReg);
poly.setOrigin(a, b);
polyBatch = new PolygonSpriteBatch();

     */

}