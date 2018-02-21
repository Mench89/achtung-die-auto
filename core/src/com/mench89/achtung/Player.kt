package com.mench89.achtung

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2

/**
 * Class representing the player.
 */
class Player(position: Vector2) : Actor(Texture("car.png")) {
  init {
    setPosition(position.x * WorldConstants.CELL_SIZE, position.y * WorldConstants.CELL_SIZE)
  }
}