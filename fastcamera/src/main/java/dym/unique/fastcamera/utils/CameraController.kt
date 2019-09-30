package dym.unique.fastcamera.utils

import android.hardware.Camera
import android.view.Surface
import dym.unique.fastcamera.bean.Radio
import kotlin.math.abs
import kotlin.math.min

@Suppress("DEPRECATION")
class CameraController(private val mCameraParameters: Camera.Parameters) {

    private val mCameraInfo = Camera.CameraInfo().apply {
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, this)
    }

    val features = Features()
    val parameters = Parameters()

    inner class Features {
        fun setDisplayRotation(camera: Camera, rotation: Int): Features {
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
            return this
        }
    }

    inner class Parameters {
        fun flushTo(camera: Camera): Parameters {
            camera.parameters = mCameraParameters
            return this
        }

        fun setCameraOrientation(orientation: Int): Parameters {
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
            return this
        }

        fun setAutoFocus(autoFocus: Boolean): Parameters {
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
            return this
        }

        fun setPreviewSize(minPreviewSize: Int, radio: Radio): Parameters {
            var aimSize: Camera.Size? = null
            var minDiff = Int.MAX_VALUE
            for (size in mCameraParameters.supportedPreviewSizes) {
                if (radio.matches(size.width, size.height)) {
                    val diff = abs(minPreviewSize - min(size.width, size.height))
                    if (aimSize == null || diff < minDiff) {
                        aimSize = size
                        minDiff = diff
                    }
                }
            }
            if (aimSize != null) {
                mCameraParameters.setPreviewSize(aimSize.width, aimSize.height)
            }
            return this
        }

        fun setPictureSize(minPicSize: Int, radio: Radio): Parameters {
            var aimSize: Camera.Size? = null
            var minDiff = Int.MAX_VALUE
            for (size in mCameraParameters.supportedPictureSizes) {
                if (radio.matches(size.width, size.height)) {
                    val diff = abs(minPicSize - min(size.width, size.height))
                    if (aimSize == null || diff < minDiff) {
                        aimSize = size
                        minDiff = diff
                    }
                }
            }
            if (aimSize != null) {
                mCameraParameters.setPictureSize(aimSize.width, aimSize.height)
            }
            return this
        }

        fun setZoom(zoom: Int): Parameters {
            require(zoom in 0..mCameraParameters.maxZoom) { "zoom 值超出范围！" }
            mCameraParameters.zoom = zoom
            return this
        }

        fun isAutoFocus(): Boolean {
            val focusMode = mCameraParameters.focusMode
            return focusMode != null && focusMode.contains("continuous")
        }

        fun getMinZoom() = 0

        fun getMaxZoom() = mCameraParameters.maxZoom

        fun getCurZoom() = mCameraParameters.zoom
    }
}