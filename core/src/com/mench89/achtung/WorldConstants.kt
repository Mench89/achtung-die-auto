package com.mench89.achtung

/**
 * Constants and other helpers relevant to rendering the world is put here.
 */
class WorldConstants private constructor() {

  companion object {
    /**
     * The width and height of a graphical cell in the world. Is the original player texture size
     * times 1.5 in order to scale the graphics some on mobile devices.
     */
    const val CELL_SIZE = 96F

    const val LEVELS_FILE_PATH = "maps/"

    const val LOG_NAME = "Achtung die auto"
  }
}