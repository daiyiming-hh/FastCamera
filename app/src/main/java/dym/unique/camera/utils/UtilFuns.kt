@file:JvmName("UtilFuns")

package dym.unique.camera.utils

inline fun safeRun(action: () -> Unit) {
    try {
        action()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}