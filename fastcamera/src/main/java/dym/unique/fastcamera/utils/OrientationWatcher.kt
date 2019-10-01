package dym.unique.fastcamera.utils

import android.content.Context
import android.view.Display
import android.view.OrientationEventListener

class OrientationWatcher(context: Context) :
    OrientationEventListener(context) {

    private var mDisplay: Display? = null

    var displayRotation = 0
        private set
    var deviceOrientation = 0
        private set

    private var mDisplayRotationListener: ((Int) -> Unit)? = null
    private var mDeviceOrientationListener: ((Int) -> Unit)? = null

    override fun onOrientationChanged(newOrientation: Int) {
        if (newOrientation == ORIENTATION_UNKNOWN) {
            return
        }
        if (deviceOrientation != newOrientation) {
            deviceOrientation = newOrientation
            mDeviceOrientationListener?.invoke(deviceOrientation)
        }
        if (mDisplay != null) {
            val newRotation = mDisplay!!.rotation
            if (displayRotation != newRotation) {
                displayRotation = newRotation
                mDisplayRotationListener?.invoke(displayRotation)
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

    fun setDisplayRotationListener(listener: ((Int) -> Unit)?) {
        mDisplayRotationListener = listener
    }

    fun setDeviceOrientationListener(listener: ((Int) -> Unit)?) {
        mDeviceOrientationListener = listener
    }
}