package com.aleksejantonov.recordview

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
}
