package dym.unique.camera.camera

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.view.ViewCompat
import dym.unique.camera.utils.SurfaceCallbackAdapter
import dym.unique.camera.utils.safeRun

@Suppress("DEPRECATION")
class CameraService(
    context: Context,
    private val mCamera: Camera,
    private val mSurface: SurfaceView
) : ICamera {
    private var mParamsController = CameraParamsController(mCamera.parameters)

    private val mOrientationWatcher = OrientationWatcher(context).apply {
        setRotationListener(this@CameraService::setDisplayOrientation)
        setOrientationListener(this@CameraService::setCameraOrientation)
    }

    init {
        mSurface.holder.addCallback(object : SurfaceCallbackAdapter() {
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

    override fun takePicture(callback: (data: ByteArray) -> Unit) {
        val takePicture = {
            mCamera.takePicture(null, null, null,
                Camera.PictureCallback { data, camera ->
                    camera.startPreview()
                    callback(data)
                })
        }
        if (mParamsController.isAutoFocus()) {
            try {
                mCamera.cancelAutoFocus()
                mCamera.autoFocus { _, _ ->
                    takePicture()
                }
            } catch (ex: Exception) {
                takePicture()
            }
        } else {
            takePicture()
        }
    }

    override fun focusOn(x: Float, y: Float) {
        TODO("对焦到触摸点")
    }

    private fun setupPreview() {
        if (mSurface.holder.surface == null) {
            return
        }
        safeRun {
            mCamera.let {
                it.setPreviewDisplay(mSurface.holder)
                mParamsController.setCameraOrientation(null, mOrientationWatcher.orientation)
                mParamsController.setAutoFocusParams(null, true)
                mParamsController.flushParametersTo(mCamera)
                it.startPreview()
            }
            setDisplayOrientation(mOrientationWatcher.rotation)
        }
    }

    private fun setDisplayOrientation(rotation: Int) {
        mParamsController.setDisplayOrientation(mCamera, rotation)
    }

    private fun setCameraOrientation(orientation: Int) {
        mParamsController.setCameraOrientation(mCamera, orientation)
    }
}