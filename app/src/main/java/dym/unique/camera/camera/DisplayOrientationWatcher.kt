package dym.unique.camera.camera

import android.content.Context
import android.view.Display
import android.view.OrientationEventListener

class DisplayOrientationWatcher(context: Context) :
    OrientationEventListener(context) {

    private var mDisplay: Display? = null

    var rotation = -1
        private set
    var orientation = -1
        private set

    private var mRotationListener: ((Int) -> Unit)? = null
    private var mOrientationListener: ((Int) -> Unit)? = null

    override fun onOrientationChanged(newOrientation: Int) {
        if (newOrientation == ORIENTATION_UNKNOWN) {
            return
        }
        if (orientation != newOrientation) {
            orientation = newOrientation
            mOrientationListener?.invoke(orientation)
        }
        if (mDisplay != null) {
            val newRotation = mDisplay!!.rotation
            if (rotation != newRotation) {
                rotation = newRotation
                mRotationListener?.invoke(rotation)
            }
        }
    }

    fun enable(display: Display) {
        super.enable()
        mDisplay = display
    }

    override fun disable() {
        super.disable()
        mDisplay = null
    }

    fun setRotationListener(listener: ((Int) -> Unit)?) {
        mRotationListener = listener
    }

    fun setOrientationListener(listener: ((Int) -> Unit)?) {
        mOrientationListener = listener
    }
}