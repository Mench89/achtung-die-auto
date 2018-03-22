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

class Wall(world: World, position: Vector2, size: Vector2) {

    private val sprite: Sprite
    private val body: Body

    init {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.position.set(position.x, position.y)
        body = world.createBody(bodyDef)

        val shape = PolygonShape()
        shape.setAsBox(size.x / 2, size.y / 2)
        body.createFixture(shape, 1f) // Shape density
        body.userData = this

        // Create the sprite
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0.7f, 0.7f, 0f, 1f)
        pix.fill()
        sprite = Sprite(Texture(pix))
        sprite.setBounds(position.x - (size.x / 2), position.y - (size.y / 2), size.x, size.y)
    }


    fun draw(spriteBatch: PolygonSpriteBatch) {
        sprite.draw(spriteBatch)
    }

}