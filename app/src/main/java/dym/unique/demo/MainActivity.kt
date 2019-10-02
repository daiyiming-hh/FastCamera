package dym.unique.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dym.unique.demo.fragment.fastcamera.FastCameraFragment
import dym.unique.demo.fragment.oldcamera.OldCameraFragment
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            toast("请获取权限后重启 APP！")
        }
    }

    fun onOpenFastCameraClicked(view: View) {
        openFragment(FastCameraFragment())
    }

    fun onOpenOldCameraClicked(view: View) {
        openFragment(OldCameraFragment())
    }

    private fun openFragment(fragment: Fragment) {
        val tran = supportFragmentManager.beginTransaction()
        tran.setCustomAnimations(
            R.anim.fast_process_page_show,
            R.anim.fast_process_page_hide,
            R.anim.fast_process_page_show,
            R.anim.fast_process_page_hide
        )
        tran.replace(R.id.fl_container, fragment)
        tran.addToBackStack(null)
        tran.commit()
    }
}
