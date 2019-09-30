package dym.unique.camera.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import dym.unique.camera.R
import dym.unique.camera.camera.CameraView
import dym.unique.camera.camera.callback.CameraCallbackAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class CameraFragment : Fragment() {
    private lateinit var mCvCamera: CameraView
    private lateinit var mImgPreview: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mCvCamera = view.findViewById(R.id.cv_camera)
        mImgPreview = view.findViewById(R.id.img_preview)

        view.findViewById<View>(R.id.fl_container).setOnTouchListener { _, _ -> true }
        view.findViewById<View>(R.id.btn_take_picture).setOnClickListener {
            mCvCamera.takePicture()
        }

        mCvCamera.setCameraCallback(object : CameraCallbackAdapter() {
            override fun onPictureTaken(data: ByteArray) {
                doAsync {
                    val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                    uiThread {
                        mImgPreview.setImageBitmap(bmp)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mCvCamera.start()
    }

    override fun onPause() {
        super.onPause()
        mCvCamera.stop()
    }
}
