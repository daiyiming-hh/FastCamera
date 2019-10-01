package dym.unique.fastcamera.service

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.core.view.ViewCompat
import dym.unique.fastcamera.bean.PreSettings
import dym.unique.fastcamera.bean.Radio
import dym.unique.fastcamera.callback.IServiceCallback
import dym.unique.fastcamera.callback.SurfaceCallbackAdapter
import dym.unique.fastcamera.utils.CameraController
import dym.unique.fastcamera.utils.OrientationWatcher
import dym.unique.fastcamera.utils.mapDisplayRotationToDegrees
import dym.unique.fastcamera.utils.safeRun
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
        setDisplayRotationListener {
            mCameraController.features
                .setDisplayRotation(mCamera, it)
        }
        setDeviceOrientationListener {
            mCameraController.parameters
                .setRotation(it)
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
                setupPreview(null)
            }
        })
    }

    fun start(settings: PreSettings.Settings?) {
        mOrientationWatcher.enable(ViewCompat.getDisplay(mSurface)!!)
        setupPreview(settings)
        mCallback.onCameraOpened(mCameraController.packageCameraStatus())
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

    fun focusByUser(x: Float, y: Float) {
        try {
            mCamera.cancelAutoFocus()
            mCameraController.parameters
                .setFocusCenter(
                    mOrientationWatcher.displayRotation,
                    mSurface.width,
                    mSurface.height,
                    x,
                    y
                )
                .flushTo(mCamera)
            mCamera.autoFocus { success, _ ->
                mCallback.onUserFocusDone(success)
            }
        } catch (ex: Exception) {
            mCallback.onUserFocusDone(false)
        }
    }

    fun setZoom(zoom: Int) {
        mCameraController.parameters
            .setZoom(zoom)
            .flushTo(mCamera)
    }

    fun setFlash(open: Boolean) {
        mCameraController.parameters
            .setFlash(open)
            .flushTo(mCamera)
    }

    private fun setupPreview(settings: PreSettings.Settings?) {
        if (mSurface.holder.surface == null) {
            return
        }
        safeRun {
            with(mCamera) {
                // 设置默认参数
                setPreviewDisplay(mSurface.holder)
                mCameraController.features
                    .setDisplayRotation(this, mOrientationWatcher.displayRotation)
                mCameraController.parameters
                    .setRotation(mOrientationWatcher.deviceOrientation)
                    .setAutoFocus()
                    .setZoom(settings?.zoom ?: 0)
                    .setFlash(settings?.flash ?: false)
                    .setPreviewSize(
                        min(mSurface.width, mSurface.height),
                        CAMERA_RADIO
                    )
                    .setPictureSize(
                        MIN_PIC_SIZE,
                        CAMERA_RADIO
                    )
                    .flushTo(this)
                // 打开预览
                startPreview()
            }
        }
    }

    companion object {
        val CAMERA_RADIO = Radio(4, 3) // 固定的比例
        const val MIN_PIC_SIZE = 1280 // 最小边大于等于这个值
        const val BACK_CAMERA = Camera.CameraInfo.CAMERA_FACING_BACK

        fun needInverseRadio(view: View): Boolean =
            mapDisplayRotationToDegrees(ViewCompat.getDisplay(view)!!.orientation % 180) == 0
    }
}