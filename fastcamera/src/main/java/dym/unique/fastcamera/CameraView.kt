package dym.unique.fastcamera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.os.Handler
import android.util.AttributeSet
import android.view.*
import dym.unique.fastcamera.bean.CameraStatus
import dym.unique.fastcamera.callback.ICameraCallback
import dym.unique.fastcamera.callback.IServiceCallback
import dym.unique.fastcamera.service.CameraService
import dym.unique.fastcamera.utils.safeRun
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class CameraView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    private val mSurface = SurfaceView(context).apply {
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        setOnTouchListener { _, event ->
            mGestureDetector.onTouchEvent(event)
            true
        }
        this@CameraView.addView(this)
    }

    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler()

    private var mService: CameraService? = null
    private var mIsStart = false

    private var mCallback: ICameraCallback? = null

    private val mGestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // 处理点击对焦
                mService?.focusOn(e.x, e.y)
                return true
            }
        })

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 设置自身宽高
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 设置 Surface 宽高
        val viewWidth = measuredWidth
        val viewHeight = measuredHeight
        var cameraRadio = CameraService.CAMERA_RADIO
        if (CameraService.needInverseRadio(this)) {
            cameraRadio = cameraRadio.inverse()
        }
        if (cameraRadio.thinnerThan(viewWidth, viewHeight)) { // 相机比 Surface 高
            mSurface.measure(
                MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(
                    cameraRadio.calcRealHeight(viewWidth),
                    MeasureSpec.EXACTLY
                )
            )
        } else {
            mSurface.measure(
                MeasureSpec.makeMeasureSpec(
                    cameraRadio.calcRealWidth(viewHeight),
                    MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY)
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val viewWidth = measuredWidth
        val viewHeight = measuredHeight
        val heightOffset = ((mSurface.measuredHeight - viewHeight) / 2F).toInt()
        val widthOffset = ((mSurface.measuredWidth - viewWidth) / 2F).toInt()
        mSurface.layout(
            -widthOffset,
            -heightOffset,
            viewWidth + widthOffset,
            viewHeight + heightOffset
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        throw UnsupportedOperationException()
    }

    fun start() {
        mIsStart = true
        mExecutor.execute {
            var camera: Camera? = null
            safeRun {
                camera = Camera.open(CameraService.BACK_CAMERA)
            }
            mHandler.post {
                if (camera != null) {
                    if (mIsStart) {
                        mService = CameraService(
                            context,
                            camera!!,
                            mSurface,
                            createCameraCallback()
                        )
                        mService!!.start()
                    } else {
                        safeRun {
                            camera!!.release()
                        }
                    }
                } else {
                    mIsStart = false
                    mCallback?.onCameraOpenFailed()
                }
            }
        }
    }

    fun stop() {
        mIsStart = false
        mService?.stop()
        mService = null
    }

    fun takePicture() {
        mService?.takePicture()
    }

    fun setZoom(zoom: Int) {
        mService?.setZoom(zoom)
    }

    fun setFlash(open: Boolean) {
        mService?.setFlash(open)
    }

    fun setCameraCallback(callback: ICameraCallback?) {
        mCallback = callback
    }

    private fun createCameraCallback(): IServiceCallback = object : IServiceCallback {
        override fun onCameraOpened(status: CameraStatus) {
            mCallback?.onCameraOpened(status)
        }


        override fun onPictureTaken(data: ByteArray) {
            mCallback?.onPictureTaken(data)
        }

        override fun onCameraClosed() {
            mCallback?.onCameraClosed()
        }
    }

}