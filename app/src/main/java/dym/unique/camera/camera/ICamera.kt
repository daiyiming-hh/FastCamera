package dym.unique.camera.camera

interface ICamera {
    fun start()

    fun stop()

    fun takePicture(callback: (data: ByteArray) -> Unit)
}