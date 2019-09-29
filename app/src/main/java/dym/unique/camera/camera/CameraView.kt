package dym.unique.camera.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.os.Handler
import android.util.AttributeSet
import android.view.*
import androidx.core.view.ViewCompat
import dym.unique.camera.utils.safeRun
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class CameraView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    private val mSurface = SurfaceView(context).apply {
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler()

    private var mService: CameraService? = null
    private var mIsStart = false

    private val mGestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // 处理点击对焦
                mService?.focusOn(e.x, e.y)
                return true
            }
        })

    init {
        addView(mSurface)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 设置自身宽高
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 设置 Surface 宽高
        val viewWidth = measuredWidth
        val viewHeight = measuredHeight
        var cameraRadio = CameraService.CONST_RADIO
        if (ViewCompat.getDisplay(this)!!.orientation % 180 == 0) {
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
        val surfaceWidth = mSurface.measuredWidth
        val surfaceHeight = mSurface.measuredHeight
        var cameraRadio = CameraService.CONST_RADIO
        if (ViewCompat.getDisplay(this)!!.orientation % 180 == 0) {
            cameraRadio = cameraRadio.inverse()
        }
        if (cameraRadio.thinnerThan(viewWidth, viewHeight)) {
            val heightOffset = ((surfaceHeight - viewHeight) / 2F).toInt()
            mSurface.layout(0, -heightOffset, viewWidth, viewHeight + heightOffset)
        } else {
            val widthOffset = ((surfaceWidth - viewWidth) / 2F).toInt()
            mSurface.layout(-widthOffset, 0, viewWidth + widthOffset, viewHeight)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(event)
        return true
    }

    fun start() {
        mIsStart = true
        mExecutor.execute {
            var camera: Camera? = null
            safeRun {
                camera = Camera.open()
            }
            camera?.let {
                mHandler.post {
                    if (mIsStart) {
                        mService = CameraService(context, it, mSurface).also {
                            it.start()
                        }
                    } else {
                        safeRun {
                            it.release()
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        mIsStart = false
        mService?.stop()
        mService = null
    }

    fun takePicture(callback: (data: ByteArray) -> Unit) {
        mService?.takePicture(callback)
    }
}