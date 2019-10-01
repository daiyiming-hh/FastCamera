@file:JvmName("UtilFuns")
@file:Suppress("DEPRECATION")

package dym.unique.fastcamera.utils

import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.Camera
import android.view.Surface
import dym.unique.fastcamera.bean.Radio
import kotlin.math.abs
import kotlin.math.min

inline fun safeRun(action: () -> Unit) {
    try {
        action()
    } catch (ex: Exception) {
        // ignore
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

fun mapDisplayRotationToDegrees(displayRotation: Int): Int {
    return when (displayRotation) {
        Surface.ROTATION_0 -> 0
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }
}

fun calcDisplayRotation(cameraInfo: Camera.CameraInfo, rotation: Int): Int {
    return if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        (360 - (cameraInfo.orientation + rotation) % 360) % 360
    } else {
        (cameraInfo.orientation - rotation + 360) % 360
    }
}

fun calcCameraRotation(cameraInfo: Camera.CameraInfo, orientation: Int): Int {
    val degrees = when (orientation) {
        in 315..360, in 0..44 -> 0
        in 45..134 -> 90
        in 135..224 -> 180
        in 225..314 -> 270
        else -> 0
    }
    return if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        (cameraInfo.orientation - degrees + 360) % 360
    } else {
        (cameraInfo.orientation + degrees) % 360
    }
}

fun calcBetterPreviewSize(
    cameraParameters: Camera.Parameters,
    minPreviewSize: Int,
    radio: Radio
): Camera.Size? {
    var aimSize: Camera.Size? = null
    var minDiff = Int.MAX_VALUE
    for (size in cameraParameters.supportedPreviewSizes) {
        if (radio.matches(size.width, size.height)) {
            val diff = abs(minPreviewSize - min(size.width, size.height))
            if (aimSize == null || diff < minDiff) {
                aimSize = size
                minDiff = diff
            }
        }
    }
    return aimSize
}

fun calcBetterPictureSize(
    cameraParameters: Camera.Parameters,
    minPicSize: Int,
    radio: Radio
): Camera.Size? {
    var aimSize: Camera.Size? = null
    var minDiff = Int.MAX_VALUE
    for (size in cameraParameters.supportedPictureSizes) {
        if (radio.matches(size.width, size.height)) {
            val diff = abs(minPicSize - min(size.width, size.height))
            if (aimSize == null || diff < minDiff) {
                aimSize = size
                minDiff = diff
            }
        }
    }
    return aimSize
}

fun calcBetterAutoFocusMode(cameraParameters: Camera.Parameters): String {
    val modes = cameraParameters.supportedFocusModes
    return when {
        modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ->
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        modes.contains(Camera.Parameters.FOCUS_MODE_FIXED) ->
            Camera.Parameters.FOCUS_MODE_FIXED
        modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY) ->
            Camera.Parameters.FOCUS_MODE_INFINITY
        else -> modes[0]
    }
}

fun calcFocusArea(
    cameraInfo: Camera.CameraInfo,
    displayRotation: Int,
    surfaceWidth: Int,
    surfaceHeight: Int,
    x: Float,
    y: Float
): Rect {
    // 映射对焦点
    val focusPoints = floatArrayOf(x, y)
    val previewToCameraMatrix = Matrix()
    Matrix()
        .apply {
            setScale(
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) -1F else 1F,
                1F
            )
            postRotate(calcDisplayRotation(cameraInfo, displayRotation).toFloat())
            postScale(surfaceWidth / 2000F, surfaceHeight / 2000F)
            postTranslate(surfaceWidth / 2f, surfaceHeight / 2f)
        }
        .invert(previewToCameraMatrix)
    previewToCameraMatrix.mapPoints(focusPoints)
    val focusArea = Rect()
    val focusHalfSize = 50
    focusArea.left = focusPoints[0].toInt() - focusHalfSize
    focusArea.right = focusPoints[0].toInt() + focusHalfSize
    focusArea.top = focusPoints[1].toInt() - focusHalfSize
    focusArea.bottom = focusPoints[1].toInt() + focusHalfSize
    if (focusArea.left < -1000) {
        focusArea.left = -1000
        focusArea.right = focusArea.left + 2 * focusHalfSize
    } else if (focusArea.right > 1000) {
        focusArea.right = 1000
        focusArea.left = focusArea.right - 2 * focusHalfSize
    }
    if (focusArea.top < -1000) {
        focusArea.top = -1000
        focusArea.bottom = focusArea.top + 2 * focusHalfSize
    } else if (focusArea.bottom > 1000) {
        focusArea.bottom = 1000
        focusArea.top = focusArea.bottom - 2 * focusHalfSize
    }
    return focusArea
}