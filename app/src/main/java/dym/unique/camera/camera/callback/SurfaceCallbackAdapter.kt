package dym.unique.camera.camera.callback

import android.view.SurfaceHolder

abstract class SurfaceCallbackAdapter: SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }
}