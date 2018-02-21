package com.mench89.achtung

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

open class Actor(var sprite: Texture) : Drawable {
  val shape = Rectangle(0F, 0F, sprite.width.toFloat(), sprite.height.toFloat())

  fun setPosition(x: Float, y: Float) {
    shape.setPosition(x, y)
  }

  fun setPosition(position: Vector2) {
    shape.setPosition(position)
  }

  fun getX() : Float {
    return shape.x
  }

  fun getY() : Float {
    return shape.y
  }

  fun getPosition() : Vector2 {
    return Vector2(getX(), getY())
  }

  fun getCenterPosition(): Vector2 {
    val center = Vector2()
    return shape.getCenter(center)
  }

  override fun draw(batch : SpriteBatch) {
    // Scale the textures x1.5
    batch.draw(sprite, shape.x, shape.y, WorldConstants.CELL_SIZE, WorldConstants.CELL_SIZE)
  }
}