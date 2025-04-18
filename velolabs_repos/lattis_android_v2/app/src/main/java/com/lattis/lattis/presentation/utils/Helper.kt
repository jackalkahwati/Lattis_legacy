package com.lattis.lattis.presentation.utils

import kotlin.random.Random

fun getRandomRotation(): Float{
    return Random.nextInt(0, 20).toFloat()
}