package dym.unique.fastcamera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import dym.unique.fastcamera.bean.CameraStatus
import dym.unique.fastcamera.bean.PreSettings
import dym.unique.fastcamera.callback.ICameraCallback
import dym.unique.fastcamera.callback.IServiceCallback
import dym.unique.fastcamera.service.CameraService
import dym.unique.fastcamera.utils.safeRun
import java.util.concurrent.Executors
import kotlin.math.max

@Suppress("DEPRECATION")
class CameraView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    private val mSurface = SurfaceView(context)
    private val mUserFocusAreaView = View(context)

    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler()

    private var mService: CameraService? = null
    private var mIsStart = false

    private val mPreSettings = PreSettings()

    private var mCallback: ICameraCallback? = null
    private val mHideAutoFocusAreaAction = {
        mUserFocusAreaView.visibility = View.GONE
    }

    private val mSurfaceGestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // 显示对焦区域
                mHandler.removeCallbacks(mHideAutoFocusAreaAction)
                with(mUserFocusAreaView) {
                    setBackgroundResource(R.drawable.drawable_focus_area_normal_bg)
                    translationX = e.x - (mSurface.width - this@CameraView.width) / 2F
                    translationY = e.y - (mSurface.height - this@CameraView.height) / 2F
                    visibility = View.VISIBLE
                }
                // 处理点击对焦
                mService?.focusOn(e.x, e.y)
                return true
            }
        })

    init {
        with(mSurface) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            setOnTouchListener { _, event ->
                mSurfaceGestureDetector.onTouchEvent(event)
                true
            }
            this@CameraView.addView(this)
        }
        with(mUserFocusAreaView) {
            visibility = View.GONE
            this@CameraView.addView(this)
        }
    }

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
        // 设置 FocusView 宽高
        val focusAreaSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            60F,
            context.resources.displayMetrics
        ).toInt()
        mUserFocusAreaView.measure(
            MeasureSpec.makeMeasureSpec(focusAreaSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(focusAreaSize, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 配置 Surface 位置
        val viewWidth = measuredWidth
        val viewHeight = measuredHeight
        val surfaceHeightOffset = ((mSurface.measuredHeight - viewHeight) / 2F).toInt()
        val surfaceWidthOffset = ((mSurface.measuredWidth - viewWidth) / 2F).toInt()
        mSurface.layout(
            -surfaceWidthOffset,
            -surfaceHeightOffset,
            viewWidth + surfaceWidthOffset,
            viewHeight + surfaceHeightOffset
        )
        // 配置 FocusArea 位置
        val focusAreaOffset =
            (max(mUserFocusAreaView.measuredWidth, mUserFocusAreaView.measuredHeight) / 2f).toInt()
        mUserFocusAreaView.layout(
            -focusAreaOffset,
            -focusAreaOffset,
            focusAreaOffset,
            focusAreaOffset
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
                        mService!!.start(mPreSettings.use())
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
        mHandler.removeCallbacksAndMessages(null)
    }

    fun takePicture() {
        mService?.takePicture()
    }

    fun setZoom(zoom: Int) {
        if (mService != null) {
            mService!!.setZoom(zoom)
        } else {
            mPreSettings.zoom = zoom
        }
    }

    fun setFlash(open: Boolean) {
        if (mService != null) {
            mService!!.setFlash(open)
        } else {
            mPreSettings.flash = open
        }
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

        override fun onUserFocusDone(success: Boolean) {
            if (success) {
                mUserFocusAreaView.visibility = View.GONE
            } else {
                mUserFocusAreaView.setBackgroundResource(R.drawable.drawable_focus_area_failed_bg)
                mHandler.postDelayed(mHideAutoFocusAreaAction, 600)
            }
            mCallback?.onUserFocusDone(success)
        }

        override fun onCameraClosed() {
            mCallback?.onCameraClosed()
        }
    }

}