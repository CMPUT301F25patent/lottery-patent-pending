package com.example.lotterypatentpending.helpers;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * An ImageView that maintains a square aspect ratio by setting its height
 * to be the same as its width.
 */
public class SquareImageView extends AppCompatImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // passes the width measurement specification for both the width and the height, forcing a square aspect ratio.
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
