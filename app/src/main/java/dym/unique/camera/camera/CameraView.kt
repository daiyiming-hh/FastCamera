package dym.unique.camera.camera

import android.content.Context
import android.hardware.Camera
import android.os.Handler
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import dym.unique.camera.utils.safeRun
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class CameraView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs) {
    init {
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler()

    private var mService: ICamera? = null

    private var mIsStart = false

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
                        mService = CameraService(context, it, this, holder).also {
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