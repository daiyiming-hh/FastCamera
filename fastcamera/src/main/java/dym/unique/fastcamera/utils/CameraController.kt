package dym.unique.fastcamera.utils

import android.hardware.Camera
import dym.unique.fastcamera.bean.CameraStatus
import dym.unique.fastcamera.bean.Radio
import dym.unique.fastcamera.service.CameraService
import java.util.*

@Suppress("DEPRECATION")
class CameraController(private val mCameraParameters: Camera.Parameters) {

    private val mCameraInfo = Camera.CameraInfo().apply {
        Camera.getCameraInfo(CameraService.BACK_CAMERA, this)
    }

    val features = Features()
    val parameters = Parameters()

    fun packageCameraStatus(): CameraStatus = with(parameters) {
        CameraStatus(
            0,
            getMaxZoom(),
            getCurZoom(),
            isFlashOpened()
        )
    }

    inner class Features {
        fun setDisplayRotation(camera: Camera, rotation: Int): Features {
            camera.setDisplayOrientation(calcDisplayRotation(mCameraInfo, rotation))
            return this
        }
    }

    inner class Parameters {
        fun flushTo(camera: Camera): Parameters {
            camera.parameters = mCameraParameters
            return this
        }

        fun setRotation(orientation: Int): Parameters {
            mCameraParameters.setRotation(calcCameraRotation(mCameraInfo, orientation))
            return this
        }

        fun setAutoFocus(): Parameters {
            mCameraParameters.focusMode = calcBetterAutoFocusMode(mCameraParameters)
            return this
        }

        fun setPreviewSize(minPreviewSize: Int, radio: Radio): Parameters {
            calcBetterPreviewSize(mCameraParameters, minPreviewSize, radio)?.let {
                mCameraParameters.setPreviewSize(it.width, it.height)
            }
            return this
        }

        fun setPictureSize(minPicSize: Int, radio: Radio): Parameters {
            calcBetterPictureSize(mCameraParameters, minPicSize, radio)?.let {
                mCameraParameters.setPictureSize(it.width, it.height)
            }
            return this
        }

        fun setZoom(zoom: Int): Parameters {
            require(zoom in 0..mCameraParameters.maxZoom) { "zoom 值超出范围！" }
            mCameraParameters.zoom = zoom
            return this
        }

        fun setFlash(open: Boolean): Parameters {
            mCameraParameters.flashMode = if (open) {
                Camera.Parameters.FLASH_MODE_ON
            } else {
                Camera.Parameters.FLASH_MODE_OFF
            }
            return this
        }

        fun setFocusCenter(
            displayRotation: Int,
            surfaceWidth: Int,
            surfaceHeight: Int,
            x: Float,
            y: Float
        ): Parameters {
            mCameraParameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO // 取消掉连续对焦
            val focusArea =
                calcFocusArea(mCameraInfo, displayRotation, surfaceWidth, surfaceHeight, x, y)
            if (mCameraParameters.maxNumFocusAreas > 0) {
                val focus = ArrayList<Camera.Area>()
                focus.add(Camera.Area(focusArea, 1000))
                mCameraParameters.focusAreas = focus
            }
            if (mCameraParameters.maxNumMeteringAreas > 0) {
                val metering = ArrayList<Camera.Area>()
                metering.add(Camera.Area(focusArea, 1000))
                mCameraParameters.meteringAreas = metering
            }
            return this
        }

        fun isAutoFocus(): Boolean {
            val focusMode = mCameraParameters.focusMode
            return focusMode != null && focusMode.contains("continuous")
        }

        fun getMaxZoom() = mCameraParameters.maxZoom

        fun getCurZoom() = mCameraParameters.zoom

        fun isFlashOpened() = mCameraParameters.flashMode == Camera.Parameters.FLASH_MODE_ON
    }
}