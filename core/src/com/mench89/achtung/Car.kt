package com.mench89.achtung

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Joint
import com.badlogic.gdx.physics.box2d.World

class Car(world: World) {

    private lateinit var body: Body
    private lateinit var tires: List<Tire>
    private lateinit var frontLeftTireJoint: Joint
    private lateinit var frontRightTireJoint: Joint



}