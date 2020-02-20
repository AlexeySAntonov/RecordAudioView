package com.aleksejantonov.recordview

import android.animation.Animator

interface AnimatorListenerAdapter : Animator.AnimatorListener {
    override fun onAnimationCancel(animation: Animator?) = Unit
    override fun onAnimationRepeat(animation: Animator?) = Unit
    override fun onAnimationStart(animation: Animator?) = Unit
}