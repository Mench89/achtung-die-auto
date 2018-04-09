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

class Bullet(world: World, position: Vector2, size: Vector2, angle: Float) {

    /**
     * Should be set true when this bullet object should be destroyed in next draw()-call.
     */
    var shouldDie = false

    private val bulletVelocity = 500f
    private val sprite: Sprite
    private val body: Body

    init {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(position.x, position.y)
        bodyDef.angle = angle
        body = world.createBody(bodyDef)

        val shape = PolygonShape()
        shape.setAsBox(size.x / 2, size.y / 2)
        body.createFixture(shape, 2f) // Shape density
        body.userData = this

        val currentForwardNormal = Vector2(body.getWorldVector(Vector2(0f, 1f)))
        body.applyLinearImpulse(currentForwardNormal.scl(bulletVelocity), body.worldCenter, true)

        // Create the sprite
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0.1f, 0.1f, 0.1f, 1f)
        pix.fill()
        sprite = Sprite(Texture(pix))
        val correctedPosition = Vector2(body.position.x - (sprite.width / 2), body.position.y - (sprite.height / 2))
        sprite.setOriginCenter()
        sprite.setBounds(correctedPosition.x, correctedPosition.y, size.x, size.y)
    }

    fun draw(spriteBatch: PolygonSpriteBatch) {
        val correctedPosition = Vector2(body.position.x - (sprite.width / 2), body.position.y - (sprite.height / 2))
        sprite.setOriginCenter()
        sprite.setPosition(correctedPosition.x, correctedPosition.y)
        sprite.rotation = body.angle * WorldConstants.RADTODEG
        sprite.draw(spriteBatch)
    }

    fun destroy(world: World) {
        world.destroyBody(body)
    }
}