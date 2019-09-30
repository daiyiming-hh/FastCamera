package dym.unique.camera.camera.callback

abstract class CameraCallbackAdapter: ICameraCallback {
    override fun onCameraOpened() {
    }

    override fun onPictureTaken(data: ByteArray) {
    }

    override fun onCameraClosed() {
    }

    override fun onCameraOpenFailed() {
    }
}