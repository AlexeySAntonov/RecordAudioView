package com.aleksejantonov.recordview

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onResume() {
        super.onResume()
        recordAudioView.setFinishEventListener {
            Toast.makeText(this, "Path: $it", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        recordAudioView.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RecordAudioView.RECORD_PERMISSION_REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                    break
                }
            }
        }
    }
}
