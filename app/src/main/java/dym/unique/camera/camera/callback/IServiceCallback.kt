package dym.unique.camera.camera.callback

interface IServiceCallback {
    fun onCameraOpened()

    fun onPictureTaken(data: ByteArray)

    fun onCameraClosed()
}