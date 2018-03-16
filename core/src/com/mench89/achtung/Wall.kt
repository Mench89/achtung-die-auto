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
        //body.setTransform(10f, 10f, 0f)
        val fixture = body.createFixture(shape, 1f) // Shape density

        // fixture->SetUserData( new CarTireFUD() );

        // Creating the color filling (but textures would work the same way)
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0.7f, 0.7f, 0f, 1f) // DE is red, AD is green and BE is blue.
        pix.fill()
        sprite = Sprite(Texture(pix))
        //sprite.setSize(size.x, size.y)
        //sprite.setOrigin(0f, 0f)
        //sprite.setSize(size.x, size.y)
        sprite.setBounds(position.x - (size.x / 2), position.y - (size.y / 2), size.x, size.y)
    }


    fun draw(spriteBatch: PolygonSpriteBatch) {
       // sprite.setPosition(body.position.x, body.position.y)
        sprite.draw(spriteBatch)
    }

}