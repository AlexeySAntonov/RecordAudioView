package com.aleksejantonov.vectoranimations

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_record_anim.*

class RecordAnim : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_record_anim, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?) {

    }

    override fun onPause() {
        super.onPause()
        recordAudioView.onPause()
    }

}
