package com.aleksejantonov.vectoranimations

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.view_record_audio.view.*
import kotlin.math.abs

class RecordAudioView(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), View.OnTouchListener {

    enum class RecordingState {
        DEFAULT,
        RECORDING,
        PINNED,
        STOPPED
    }

    private var recordAudioAnimSet: AnimatorSet? = null
    private var recordCancelAnimSet: AnimatorSet? = null
    private var lockArrowShakingAnimSet: AnimatorSet? = null
    private var recordDotAnimSet: AnimatorSet? = null
    private var recording: Boolean = false
    private var recordingPinned: Boolean = false
    private var timer: CountDownTimer? = null

    init {
        addView(inflate(R.layout.view_record_audio))
        updateRecordingState(RecordingState.DEFAULT)
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
                if (recording && !recordingPinned && event.y < 0 && abs(event.y) > context.getPxFromDp(40)) {
                    pinRecording()
                } else if (recording && !recordingPinned && event.x < 0 && abs(event.x) > context.getPxFromDp(40)) {
                    stopRecording()
                }
                true
            }
            else                    -> false
        }
    }

    fun onPause() {
        if (recording) stopRecording()
    }

    private fun updateRecordingState(state: RecordingState) {
        when (state) {
            RecordingState.DEFAULT -> {
                recordPanel.alpha = 1f
                recordPanel.visibility = View.GONE
                recordLock.visibility = View.GONE
                recordLock.setImageResource(R.drawable.ic_lock_open_24dp)
                lockArrow.alpha = 0.75f
                lockArrow.visibility = View.GONE
                recordDuration.text = String.format("%02d:%02d.%02d", 0, 0, 0)
                record.setBackgroundResource(0)
                record.setImageResource(R.drawable.ic_mic_24dp)
                record.setColorFilter(Color.argb(255, 255, 67, 94)) // R.color.cardRed
                record.scaleX = 1f
                record.scaleY = 1f
                record.alpha = 1f
                context?.getScreenWidth()?.toFloat()?.let { recordPanel.x = it }
                context?.getScreenHeight()?.toFloat()?.let {
                    recordLock.y = it
                    lockArrow.y = it
                }
                slideArrow.alpha = 0.75f
                slideArrow.visibility = View.VISIBLE
                recordCancel.text = context.getText(R.string.record_view_slide_cancel)
                recordCancel.alpha = 0.75f
                recordCancel.setOnClickListener { stopRecording() }
            }
            RecordingState.RECORDING -> {
                context?.vibrate()
                recording = true
                record.setBackgroundResource(R.drawable.background_circle)
                record.setImageResource(R.drawable.ic_mic_24dp)
                record.setColorFilter(Color.argb(255, 255, 255, 255))
                recordPanel.visibility = View.VISIBLE
                recordLock.visibility = View.VISIBLE
                lockArrow.visibility = View.VISIBLE
                slideArrow.visibility = View.VISIBLE

                startRecordingAnimation()
                launchTimer()
            }
            RecordingState.PINNED -> {
                context?.vibrate()
                recordingPinned = true
                slideArrow.visibility = View.GONE
                recordCancel.text = context.getText(R.string.record_view_cancel)
                recordCancel.alpha = 1f
                recordLock.setImageResource(R.drawable.ic_lock_24dp)
                lockArrow.visibility = View.GONE
                record.setImageResource(R.drawable.ic_send_24dp)
                disableShakingAnimation()
            }
            RecordingState.STOPPED -> {
                context?.vibrate()
                recordingPinned = false
                recording = false

                disableTimer()
                disableShakingAnimation()
                disableRecordDotAnimation()
                stopRecordingAnimation()
            }
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
        updateRecordingState(RecordingState.RECORDING)
    }

    private fun pinRecording() {
        updateRecordingState(RecordingState.PINNED)
    }

    private fun stopRecording() {
        updateRecordingState(RecordingState.STOPPED)
    }

    private fun startRecordingAnimation() {
        recordAudioAnimSet?.cancel()
        recordAudioAnimSet = AnimatorSet()
        recordAudioAnimSet?.interpolator = DecelerateInterpolator()
        recordAudioAnimSet?.playTogether(
            ObjectAnimator.ofFloat(recordPanel, View.TRANSLATION_X, 0f).apply {
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(record, View.SCALE_X, 1f, 1.5f).apply {
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(record, View.SCALE_Y, 1f, 1.5f).apply {
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(record, View.ALPHA, 0.5f, 1f).apply {
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(recordLock, View.TRANSLATION_Y, -(context?.getPxFromDp(102)?.toFloat() ?: 102f)).apply {
                duration = RECORD_PANEL_APPEARANCE_DURATION
            },
            ObjectAnimator.ofFloat(lockArrow, View.TRANSLATION_Y, -(context?.getPxFromDp(72)?.toFloat() ?: 72f)).apply {
                duration = RECORD_PANEL_APPEARANCE_DURATION
            }
        )
        recordAudioAnimSet?.addListener(object : AnimatorListenerAdapter {
            override fun onAnimationEnd(animator: Animator) {
                if (animator == recordAudioAnimSet) {
                    startArrowShakingAnimation()
                    startRecordDotAnimation()
                    recordAudioAnimSet = null
                }
            }
        })
        recordAudioAnimSet?.start()
    }

    private fun startArrowShakingAnimation() {
        lockArrowShakingAnimSet?.cancel()
        lockArrowShakingAnimSet = AnimatorSet()
        lockArrowShakingAnimSet?.interpolator = AccelerateInterpolator()
        lockArrowShakingAnimSet?.playTogether(
            ObjectAnimator.ofFloat(lockArrow, View.TRANSLATION_Y, lockArrow.y).apply {
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                duration = REVERSE_ANIM_DURATION
            }
        )
        lockArrowShakingAnimSet?.start()
    }

    private fun startRecordDotAnimation() {
        recordDotAnimSet?.cancel()
        recordDotAnimSet = AnimatorSet()
        recordDotAnimSet?.interpolator = AccelerateInterpolator()
        recordDotAnimSet?.playTogether(
            ObjectAnimator.ofFloat(recordDot, View.ALPHA, 0.5f, 1f).apply {
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                duration = REVERSE_ANIM_DURATION
            }
        )
        recordDotAnimSet?.start()
    }

    private fun stopRecordingAnimation() {
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
            ObjectAnimator.ofFloat(lockArrow, View.TRANSLATION_Y, context?.getScreenHeight()?.toFloat() ?: 0f).apply {
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
                    updateRecordingState(RecordingState.DEFAULT)
                    recordCancelAnimSet = null
                }
            }
        })
        recordCancelAnimSet?.start()
    }

    private fun launchTimer() {
        timer = object : CountDownTimer(MAX_RECORD_DURATION, 64) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (MAX_RECORD_DURATION - millisUntilFinished) / 1000
                val ms = ((MAX_RECORD_DURATION - millisUntilFinished) % 1000) / 10
                recordDuration.text = String.format("%02d:%02d.%02d", sec / 60, sec % 60, ms)
            }

            override fun onFinish() {
                updateRecordingState(RecordingState.STOPPED)
            }
        }.start()
    }

    private fun disableTimer() {
        timer?.cancel()
        timer = null
    }

    private fun disableShakingAnimation() {
        lockArrowShakingAnimSet?.cancel()
        lockArrowShakingAnimSet = null
    }

    private fun disableRecordDotAnimation() {
        recordDotAnimSet?.cancel()
        recordDotAnimSet = null
    }

    companion object {
        private const val RECORD_PANEL_APPEARANCE_DURATION = 330L
        private const val REVERSE_ANIM_DURATION = 600L
        private const val MAX_RECORD_DURATION = 10L * 60 * 1000 // 10 min in millis
    }
}