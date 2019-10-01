package dym.unique.fastcamera.callback

import dym.unique.fastcamera.bean.CameraStatus

abstract class CameraCallbackAdapter : ICameraCallback {
    override fun onCameraOpened(status: CameraStatus) {
    }

    override fun onPictureTaken(data: ByteArray) {
    }

    override fun onUserFocusDone(success: Boolean) {
    }

    override fun onCameraClosed() {
    }

    override fun onCameraOpenFailed() {
    }
}