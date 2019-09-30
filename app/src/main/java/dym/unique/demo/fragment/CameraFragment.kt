package dym.unique.demo.fragment

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import dym.unique.demo.R
import dym.unique.fastcamera.CameraView
import dym.unique.fastcamera.bean.CameraStatus
import dym.unique.fastcamera.callback.CameraCallbackAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class CameraFragment : Fragment() {
    private lateinit var mCvCamera: CameraView
    private lateinit var mImgPreview: ImageView
    private lateinit var mSbZoom: SeekBar

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
        mSbZoom = view.findViewById(R.id.sb_zoom)

        view.findViewById<View>(R.id.fl_container).setOnTouchListener { _, _ -> true }
        view.findViewById<View>(R.id.btn_take_picture).setOnClickListener {
            mCvCamera.takePicture()
        }
        mSbZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mCvCamera.setZoom(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        mCvCamera.setCameraCallback(object : CameraCallbackAdapter() {
            override fun onCameraOpened(status: CameraStatus) {
                mSbZoom.visibility = View.VISIBLE
                mSbZoom.max = status.maxZoom
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mSbZoom.min = status.minZoom
                }
                mSbZoom.progress = status.curZoom
            }

            override fun onPictureTaken(data: ByteArray) {
                doAsync {
                    val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                    uiThread {
                        mImgPreview.setImageBitmap(bmp)
                    }
                }
            }

            override fun onCameraClosed() {
                mSbZoom.visibility = View.GONE
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
