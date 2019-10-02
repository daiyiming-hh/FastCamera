package dym.unique.demo.fragment.fastcamera

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dym.unique.demo.R
import dym.unique.demo.databinding.FragmentFastCameraBinding
import dym.unique.fastcamera.bean.CameraStatus
import dym.unique.fastcamera.callback.CameraCallbackAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class FastCameraFragment : Fragment() {

    private lateinit var mBinding: FragmentFastCameraBinding
    private val mModel = FastCameraModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_fast_camera, container, false)
        mBinding.view = this
        mBinding.model = mModel
        return mBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(mBinding) {
            flContainer.setOnTouchListener { _, _ -> true }
            sbZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        cvCamera.setZoom(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            cbFlash.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    cvCamera.setFlash(isChecked)
                }
            }

            cvCamera.setCameraCallback(object : CameraCallbackAdapter() {
                override fun onCameraOpened(status: CameraStatus) {
                    mModel.weightVisibility.set(View.VISIBLE)
                    sbZoom.max = status.maxZoom
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sbZoom.min = status.minZoom
                    }
                    sbZoom.progress = status.curZoom
                    cbFlash.isChecked = status.isFlashOpened
                }

                override fun onPictureTaken(data: ByteArray) {
                    doAsync {
                        val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                        uiThread {
                            imgPreview.setImageBitmap(bmp)
                        }
                    }
                }

                override fun onCameraClosed() {
                    mModel.weightVisibility.set(View.GONE)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.cvCamera.start()
    }

    override fun onPause() {
        super.onPause()
        mBinding.cvCamera.stop()
    }

    fun onTakePictureClicked() {
        mBinding.cvCamera.takePicture()
    }
}
