package dym.unique.camera.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dym.unique.camera.R
import dym.unique.camera.camera.CameraView

class CameraFragment : Fragment() {
    private lateinit var mCvCamera: CameraView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mCvCamera = view.findViewById(R.id.cv_camera)
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
