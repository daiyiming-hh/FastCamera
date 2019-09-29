package dym.unique.camera.camera

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.view.ViewCompat
import dym.unique.camera.utils.OrientationWatcher
import dym.unique.camera.utils.SurfaceCallbackAdapter
import dym.unique.camera.utils.safeRun
import kotlin.math.min

@Suppress("DEPRECATION")
class CameraService(
    context: Context,
    private val mCamera: Camera,
    private val mSurface: SurfaceView
) {
    private var mCameraController = CameraController(mCamera.parameters)

    private val mOrientationWatcher = OrientationWatcher(context).apply {
        setRotationListener {
            mCameraController.features
                .setDisplayRotation(mCamera, it)
        }
        setOrientationListener {
            mCameraController.parameters
                .setCameraOrientation(it)
                .flushTo(mCamera)
        }
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

    fun start() {
        setupPreview()
        mOrientationWatcher.enable(ViewCompat.getDisplay(mSurface)!!)
    }

    fun stop() {
        mOrientationWatcher.disable()
        mCamera.release()
    }

    fun takePicture(callback: (data: ByteArray) -> Unit) {
        val takePicture = {
            mCamera.takePicture(null, null, null,
                Camera.PictureCallback { data, camera ->
                    camera.startPreview()
                    callback(data)
                })
        }
        if (mCameraController.parameters.isAutoFocus()) {
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

    fun focusOn(x: Float, y: Float) {
        TODO("对焦到触摸点")
    }

    private fun setupPreview() {
        if (mSurface.holder.surface == null) {
            return
        }
        safeRun {
            mCamera.let {
                it.setPreviewDisplay(mSurface.holder)
                mCameraController.features
                    .setDisplayRotation(mCamera, mOrientationWatcher.rotation)
                mCameraController.parameters
                    .setCameraOrientation(mOrientationWatcher.orientation)
                    .setAutoFocus(true)
                    .setPreviewSize(min(mSurface.width, mSurface.height), CONST_RADIO)
                    .setPictureSize(MIN_PIC_SIZE, CONST_RADIO)
                    .flushTo(mCamera)
                it.startPreview()
            }
        }
    }

    companion object {
        val CONST_RADIO = Radio(4, 3) // 固定的比例
        val MIN_PIC_SIZE = 1280 // 最小边大于等于这个值
    }
}