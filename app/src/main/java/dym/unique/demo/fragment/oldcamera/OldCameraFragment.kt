package dym.unique.demo.fragment.oldcamera

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dym.unique.demo.R
import dym.unique.demo.databinding.FragmentOldCameraBinding

class OldCameraFragment : Fragment() {

    private lateinit var mBinding: FragmentOldCameraBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_old_camera, container, false)
        return mBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(mBinding) {
            flContainer.setOnTouchListener { _, _ -> true }
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
}
