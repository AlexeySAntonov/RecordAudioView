package com.aleksejantonov.vectoranimations

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.view_record_audio.view.*
import kotlin.math.abs

class RecordAudioView(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), View.OnTouchListener {

    enum class RecordState {
        DEFAULT,
        RECORDING,
        STOPPED
    }

    private var recordAudioAnimSet: AnimatorSet? = null
    private var recordCancelAnimSet: AnimatorSet? = null
    private var recording: Boolean = false
    private var recordingPinned: Boolean = false
    private var timer: CountDownTimer? = null

    init {
        addView(inflate(R.layout.view_record_audio))
        updateRecordPanelState()
        setupRecordButton()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                false
            }
            MotionEvent.ACTION_UP   -> {
                if (recording && !recordingPinned) {
                    stopRecording()
                    return true
                }
                false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!recordingPinned && abs(event.y) > context.getPxFromDp(48)) {
                    recordingPinned = true
                    context?.vibrate()
                    recordLock.setImageResource(R.drawable.ic_lock_24dp)
                    record.setImageResource(R.drawable.ic_send_24dp)
                }
                true
            }
            else                    -> false
        }
    }

    fun onPause() {
        if (recording) stopRecording()
    }

    private fun updateRecordPanelState() {
        if (recording) {
            recordCancelAnimSet?.cancel()
            recordCancelAnimSet = AnimatorSet()
            recordCancelAnimSet?.interpolator = AccelerateInterpolator()
            recordCancelAnimSet?.playTogether(
                ObjectAnimator.ofFloat(recordPanel, View.TRANSLATION_X, context?.getScreenWidth()?.toFloat() ?: 0f).apply {
                    duration = RECORD_PANEL_APPEARANCE_DURATION
                },
                ObjectAnimator.ofFloat(recordLock, View.TRANSLATION_Y, context?.getScreenHeight()?.toFloat() ?: 0f).apply {
                    duration = RECORD_PANEL_APPEARANCE_DURATION
                },
                ObjectAnimator.ofFloat(recordPanel, View.ALPHA, 0.8f, 0f).apply {
                    duration = RECORD_PANEL_APPEARANCE_DURATION
                },
                ObjectAnimator.ofFloat(record, View.SCALE_X, record.scaleX, 1f).apply {
                    duration = RECORD_PANEL_APPEARANCE_DURATION
                },
                ObjectAnimator.ofFloat(record, View.SCALE_Y, record.scaleY, 1f).apply {
                    duration = RECORD_PANEL_APPEARANCE_DURATION
                },
                ObjectAnimator.ofFloat(record, View.ALPHA, record.alpha, 1f).apply {
                    duration = RECORD_PANEL_APPEARANCE_DURATION
                }
            )
            recordCancelAnimSet?.addListener(object : AnimatorListenerAdapter {
                override fun onAnimationEnd(animator: Animator) {
                    if (animator == recordCancelAnimSet) {
                        recordPanel.alpha = 1f
                        recordPanel.visibility = View.GONE
                        recordLock.visibility = View.GONE
                        recordLock.setImageResource(R.drawable.ic_lock_open_24dp)
                        recordDuration.text = String.format("%02d:%02d.%02d", 0, 0, 0)
                        record.setBackgroundResource(0)
                        record.setImageResource(R.drawable.ic_mic_24dp)
                        record.setColorFilter(Color.argb(255, 255, 67, 94)) // R.color.cardRed
                        record.scaleX = 1f
                        record.scaleY = 1f
                        record.alpha = 1f
                        recordCancelAnimSet = null
                    }
                }
            })
            recordCancelAnimSet?.start()
        } else {
            recordPanel.visibility = View.GONE
            recordLock.visibility = View.GONE
            context?.getScreenWidth()?.toFloat()?.let { recordPanel.x = it }
            context?.getScreenHeight()?.toFloat()?.let { recordLock.y = it }
            recordDuration.text = String.format("%02d:%02d.%02d", 0, 0, 0)
            recordCancel.setOnClickListener { stopRecording() }
        }
    }

    private fun setupRecordButton() {
        record.setOnTouchListener(this)
        record.setOnLongClickListener {
            if (recording) return@setOnLongClickListener true
            startRecording()
            true
        }

        record.setOnClickListener {
            if (recording && recordingPinned) {
                stopRecording()
            } else if (!recording) {
                Toast.makeText(context, "Hold to record audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        context?.vibrate()
        record.setBackgroundResource(R.drawable.background_circle)
        record.setImageResource(R.drawable.ic_mic_24dp)
        record.setColorFilter(Color.argb(255, 255, 255, 255))
        recordPanel.visibility = View.VISIBLE
        recordLock.visibility = View.VISIBLE
        recordLock.setImageResource(R.drawable.ic_lock_open_24dp)

        recordAudioAnimSet?.cancel()
        recordAudioAnimSet = AnimatorSet()
        recordAudioAnimSet?.interpolator = DecelerateInterpolator()
        recordAudioAnimSet?.playTogether(
            ObjectAnimator.ofFloat(recordPanel, View.TRANSLATION_X, 0f).apply {
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(record, View.SCALE_X, 1f, 1.5f).apply {
                //                repeatCount = ValueAnimator.INFINITE
//                repeatMode = ValueAnimator.REVERSE
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(record, View.SCALE_Y, 1f, 1.5f).apply {
                //                repeatCount = ValueAnimator.INFINITE
//                repeatMode = ValueAnimator.REVERSE
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(record, View.ALPHA, 0.5f, 1f).apply {
                //                repeatCount = ValueAnimator.INFINITE
//                repeatMode = ValueAnimator.REVERSE
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(recordLock, View.TRANSLATION_Y, -(context?.getPxFromDp(102)?.toFloat() ?: 102f)).apply {
                duration = RECORD_PANEL_APPEARANCE_DURATION
            }
        )
        recordAudioAnimSet?.start()
        launchTimer()
        recording = true
    }

    private fun launchTimer() {
        timer = object : CountDownTimer(MAX_RECORD_DURATION, 64) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (MAX_RECORD_DURATION - millisUntilFinished) / 1000
                val ms = ((MAX_RECORD_DURATION - millisUntilFinished) % 1000) / 10
                recordDuration.text = String.format("%02d:%02d.%02d", sec / 60, sec % 60, ms)
            }

            override fun onFinish() {
                // TODO
            }
        }.start()
    }

    private fun stopRecording() {
        context?.vibrate()
        updateRecordPanelState()
        timer?.cancel()
        timer = null
        recordAudioAnimSet?.cancel()
        recordAudioAnimSet = null
        recordingPinned = false
        recording = false
    }

    companion object {
        private const val RECORD_PANEL_APPEARANCE_DURATION = 330L
        private const val MAX_RECORD_DURATION = 10L * 60 * 1000 // 10 min in millis
    }
}