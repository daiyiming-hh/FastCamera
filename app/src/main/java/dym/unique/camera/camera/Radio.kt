package dym.unique.camera.camera

import dym.unique.camera.utils.gcd

data class Radio(val x: Int, val y: Int) {
    fun calcRealHeight(realWidth: Int): Int = (realWidth.toFloat() * y / x).toInt()

    fun calcRealWidth(realHeight: Int): Int = (realHeight.toFloat() * x / y).toInt()

    fun thinnerThan(targetX: Int, targetY: Int): Boolean = targetX * y >= targetY * x

    fun matches(width: Int, height: Int): Boolean {
        val gcd = gcd(width, height)
        val targetX = width / gcd
        val targetY = height / gcd
        return x == targetX && y == targetY
    }

    fun inverse() = Radio(y, x)

}