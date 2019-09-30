package dym.unique.camera.camera.callback

import dym.unique.camera.camera.bean.CameraStatus

abstract class CameraCallbackAdapter : ICameraCallback {
    override fun onCameraOpened(status: CameraStatus) {
    }

    override fun onPictureTaken(data: ByteArray) {
    }

    override fun onCameraClosed() {
    }

    override fun onCameraOpenFailed() {
    }
}