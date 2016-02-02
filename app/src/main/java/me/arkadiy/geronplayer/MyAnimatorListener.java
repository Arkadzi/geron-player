package me.arkadiy.geronplayer;

import android.animation.Animator;

public class MyAnimatorListener implements Animator.AnimatorListener {
    private boolean isAnimated;
    @Override
    public void onAnimationStart(Animator animation) {
        isAnimated = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        isAnimated = false;

    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public boolean isAnimated() {
        return isAnimated;
    }
}
