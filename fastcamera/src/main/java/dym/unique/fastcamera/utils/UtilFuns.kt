@file:JvmName("UtilFuns")

package dym.unique.fastcamera.utils

inline fun safeRun(action: () -> Unit) {
    try {
        action()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

/**
 * 辗转相除法计算最大公约数
 */
fun gcd(x: Int, y: Int): Int {
    var mutableX = x
    var mutableY = y
    while (mutableY != 0) {
        val tmp = mutableY
        mutableY = mutableX % mutableY
        mutableX = tmp
    }
    return mutableX
}