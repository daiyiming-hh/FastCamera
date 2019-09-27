package dym.unique.camera.camera

import android.content.Context
import android.hardware.Camera
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.view.ViewCompat
import dym.unique.camera.utils.SurfaceCallbackAdapter
import dym.unique.camera.utils.safeRun

@Suppress("DEPRECATION")
class CameraService(
    context: Context,
    private val mCamera: Camera,
    private val mSurface: SurfaceView,
    private val mSurfaceHolder: SurfaceHolder
) : IService {
    private var mCameraParams = mCamera.parameters
    private val mCameraInfo = Camera.CameraInfo().also {
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, it)
    }

    private val mOrientationWatcher = OrientationWatcher(context).apply {
        setRotationListener(this@CameraService::setDisplayOrientation)
        setOrientationListener(this@CameraService::setCameraOrientation)
    }

    init {
        mSurfaceHolder.addCallback(object : SurfaceCallbackAdapter() {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                safeRun {
                    mCamera.stopPreview()
                }
                setupPreview()
            }
        })
    }

    override fun start() {
        setupPreview()
        mOrientationWatcher.enable(ViewCompat.getDisplay(mSurface)!!)
    }

    override fun stop() {
        mOrientationWatcher.disable()
        mCamera.release()
    }

    private fun setupPreview() {
        if (mSurfaceHolder.surface == null) {
            return
        }
        safeRun {
            mCamera.let {
                it.setPreviewDisplay(mSurfaceHolder)
                it.startPreview()
                setCameraOrientationParams(mOrientationWatcher.orientation)
                setAutoFocusParams(true)
                it.parameters = mCameraParams
            }
            setDisplayOrientation(mOrientationWatcher.rotation)
        }
    }

    private fun setDisplayOrientation(rotation: Int) {
        val degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        mCamera.setDisplayOrientation(
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (360 - (mCameraInfo.orientation + degrees) % 360) % 360
            } else {
                (mCameraInfo.orientation - degrees + 360) % 360
            }
        )
    }

    private fun setCameraOrientation(orientation: Int) {
        setCameraOrientationParams(orientation)
        mCamera.parameters = mCameraParams
    }

    private fun setCameraOrientationParams(orientation: Int) {
        val degrees = when (orientation) {
            in 315..360, in 0..44 -> 0
            in 45..134 -> 90
            in 135..224 -> 180
            in 225..314 -> 270
            else -> 0
        }
        mCameraParams.setRotation(
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (mCameraInfo.orientation - degrees + 360) % 360
            } else {
                (mCameraInfo.orientation + degrees) % 360
            }
        )
    }

    private fun setAutoFocusParams(autoFocus: Boolean) {
        with(mCameraParams) {
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
    }
}