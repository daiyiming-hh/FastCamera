package dym.unique.camera

import android.content.Context
import android.hardware.Camera
import android.os.Handler
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.view.ViewCompat
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class CameraView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {
    private val mHolder = holder.apply {
        addCallback(this@CameraView)
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    @Volatile
    private var mCamera: Camera? = null
    @Volatile
    private var mCameraParams: Camera.Parameters? = null
    private val mCameraInfo = Camera.CameraInfo()

    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler()

    private val mDisplayOrientationWatcher = DisplayOrientationWatcher(context).apply {
        setRotationListener(this@CameraView::setDisplayOrientation)
        setOrientationListener(this@CameraView::setCameraOrientation)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mDisplayOrientationWatcher.enable(ViewCompat.getDisplay(this)!!)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mDisplayOrientationWatcher.disable()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        safeRun {
            mCamera?.stopPreview()
        }
        setupPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    fun start() {
        mExecutor.execute {
            safeRun {
                mCamera = Camera.open()
                mCameraParams = mCamera?.parameters
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, mCameraInfo)
            }
            if (mCamera != null) {
                mHandler.post {
                    setupPreview()
                }
            }
        }
    }

    fun stop() {
        mCamera?.release()
        mCamera = null
        mCameraParams = null
    }

    private fun setupPreview() {
        if (mHolder.surface == null) {
            return
        }
        safeRun {
            mCamera?.let {
                it.setPreviewDisplay(mHolder)
                it.startPreview()
                setCameraOrientationParams(mDisplayOrientationWatcher.orientation)
                setAutoFocusParams(true)
                it.parameters = mCameraParams ?: return
            }
            setDisplayOrientation(mDisplayOrientationWatcher.rotation)
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
        mCamera?.setDisplayOrientation(
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (360 - (mCameraInfo.orientation + degrees) % 360) % 360
            } else {
                (mCameraInfo.orientation - degrees + 360) % 360
            }
        )
    }

    private fun setCameraOrientation(orientation: Int) {
        setCameraOrientationParams(orientation)
        mCamera?.parameters = mCameraParams ?: return
    }

    private fun setCameraOrientationParams(orientation: Int) {
        val degrees = when (orientation) {
            in 315..360, in 0..44 -> 0
            in 45..134 -> 90
            in 135..224 -> 180
            in 225..314 -> 270
            else -> 0
        }
        mCameraParams?.setRotation(
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (mCameraInfo.orientation - degrees + 360) % 360
            } else {
                (mCameraInfo.orientation + degrees) % 360
            }
        )
    }

    private fun setAutoFocusParams(autoFocus: Boolean) {
        mCameraParams?.let {
            val modes = it.supportedFocusModes
            it.focusMode =
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