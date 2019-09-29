package dym.unique.camera.camera

import android.hardware.Camera
import android.view.Surface

@Suppress("DEPRECATION")
class CameraParamsController(private val mCameraParameters: Camera.Parameters) {

    private val mCameraInfo = Camera.CameraInfo().apply {
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, this)
    }

    fun flushParametersTo(camera: Camera) {
        camera.parameters = mCameraParameters
    }

    fun setCameraOrientation(camera: Camera?, orientation: Int) {
        val degrees = when (orientation) {
            in 315..360, in 0..44 -> 0
            in 45..134 -> 90
            in 135..224 -> 180
            in 225..314 -> 270
            else -> 0
        }
        mCameraParameters.setRotation(
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (mCameraInfo.orientation - degrees + 360) % 360
            } else {
                (mCameraInfo.orientation + degrees) % 360
            }
        )
        camera?.parameters = mCameraParameters
    }

    fun setAutoFocusParams(camera: Camera?, autoFocus: Boolean) {
        with(mCameraParameters) {
            val modes = supportedFocusModes
            focusMode =
                if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                    Camera.Parameters.FOCUS_MODE_FIXED
                } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                    Camera.Parameters.FOCUS_MODE_INFINITY
                } else {
                    modes[0]
                }
        }
        camera?.parameters = mCameraParameters
    }

    fun isAutoFocus(): Boolean {
        val focusMode = mCameraParameters.focusMode
        return focusMode != null && focusMode.contains("continuous")
    }

    fun setDisplayOrientation(camera: Camera, rotation: Int) {
        val degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        camera.setDisplayOrientation(
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (360 - (mCameraInfo.orientation + degrees) % 360) % 360
            } else {
                (mCameraInfo.orientation - degrees + 360) % 360
            }
        )
    }
}