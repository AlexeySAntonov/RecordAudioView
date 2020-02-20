package com.aleksejantonov.vectoranimations

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_record_anim.*

class RecordAnim : Fragment() {

    private val permissions: Array<String> = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_record_anim, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        recordAudioView.setPermissionRequireListener { requestPermissions() }
    }

    private fun requestPermissions() {
        requestPermissions(permissions, 3211)
    }

    override fun onPause() {
        super.onPause()
        recordAudioView.onPause()
    }

}
