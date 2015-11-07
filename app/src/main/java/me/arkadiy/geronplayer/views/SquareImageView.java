package me.arkadiy.geronplayer.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import me.arkadiy.geronplayer.R;

/**
 * Created by Arkadiy on 08.08.2015.
 */
public class SquareImageView extends ImageView {
    private boolean maxWidth;

    public SquareImageView(Context context) {
        super(context);

    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDirection(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDirection(context, attrs);
    }

    private void setDirection(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SquareImageView,
                0, 0);

        try {
            maxWidth = (a.getInteger(R.styleable.SquareImageView_direction, 0) == 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (maxWidth) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
        else {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        }
    }

    public void isMaxHeight(boolean maxHeight) {
        this.maxWidth = maxHeight;
    }
}
