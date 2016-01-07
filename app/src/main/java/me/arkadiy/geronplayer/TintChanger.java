package me.arkadiy.geronplayer;

import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Arkadiy on 17.12.2015.
 */
public class TintChanger implements View.OnTouchListener {
    int color;

    public TintChanger(Resources resourses) {
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
            color = resourses.getColor(R.color.accent);
        } else {
            color = resourses.getColor(R.color.accent, null);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ((ImageView) v).setColorFilter(color);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ((ImageView) v).setColorFilter(null);
                break;
        }
        return false;
    }
}
