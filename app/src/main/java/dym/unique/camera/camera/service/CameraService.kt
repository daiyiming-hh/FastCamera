package dym.unique.camera.camera.service

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.view.ViewCompat
import dym.unique.camera.camera.bean.CameraStatus
import dym.unique.camera.camera.bean.Radio
import dym.unique.camera.camera.callback.IServiceCallback
import dym.unique.camera.camera.callback.SurfaceCallbackAdapter
import dym.unique.camera.camera.utils.CameraController
import dym.unique.camera.camera.utils.OrientationWatcher
import dym.unique.camera.camera.utils.safeRun
import kotlin.math.min

@Suppress("DEPRECATION")
class CameraService(
    context: Context,
    private val mCamera: Camera,
    private val mSurface: SurfaceView,
    private val mCallback: IServiceCallback
) {
    private val mCameraController =
        CameraController(mCamera.parameters)

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
        mOrientationWatcher.enable(ViewCompat.getDisplay(mSurface)!!)
        setupPreview()
        mCallback.onCameraOpened(with(mCameraController.parameters) {
            CameraStatus(getMinZoom(), getMaxZoom(), getCurZoom())
        })
    }

    fun stop() {
        mOrientationWatcher.disable()
        mCamera.release()
        mCallback.onCameraClosed()
    }

    fun takePicture() {
        val takePicture = {
            mCamera.takePicture(null, null, null,
                Camera.PictureCallback { data, camera ->
                    camera.startPreview()
                    mCallback.onPictureTaken(data)
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

    fun setZoom(zoom: Int) {
        mCameraController.parameters
            .setZoom(zoom)
            .flushTo(mCamera)
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
                    .setPreviewSize(
                        min(mSurface.width, mSurface.height),
                        CAMERA_RADIO
                    )
                    .setPictureSize(
                        MIN_PIC_SIZE,
                        CAMERA_RADIO
                    )
                    .flushTo(mCamera)
                it.startPreview()
            }
        }
    }

    companion object {
        val CAMERA_RADIO = Radio(4, 3) // 固定的比例
        const val MIN_PIC_SIZE = 1280 // 最小边大于等于这个值
    }
}