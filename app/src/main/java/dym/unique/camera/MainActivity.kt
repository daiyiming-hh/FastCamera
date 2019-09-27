package dym.unique.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import dym.unique.camera.fragment.CameraFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onOpenCameraClicked(view: View) {
        val tran = supportFragmentManager.beginTransaction()
        tran.setCustomAnimations(
            R.anim.fast_process_page_show,
            R.anim.fast_process_page_hide,
            R.anim.fast_process_page_show,
            R.anim.fast_process_page_hide
        )
        tran.replace(R.id.fl_container, CameraFragment())
        tran.addToBackStack(null)
        tran.commit()
    }
}
