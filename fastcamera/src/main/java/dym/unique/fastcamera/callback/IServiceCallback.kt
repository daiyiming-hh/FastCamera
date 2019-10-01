package dym.unique.fastcamera.callback

import dym.unique.fastcamera.bean.CameraStatus

interface IServiceCallback {
    fun onCameraOpened(status: CameraStatus)

    fun onPictureTaken(data: ByteArray)

    fun onUserFocusDone(success: Boolean)

    fun onCameraClosed()
}