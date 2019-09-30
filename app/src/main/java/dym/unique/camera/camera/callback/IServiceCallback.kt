package dym.unique.camera.camera.callback

import dym.unique.camera.camera.bean.CameraStatus

interface IServiceCallback {
    fun onCameraOpened(status: CameraStatus)

    fun onPictureTaken(data: ByteArray)

    fun onCameraClosed()
}