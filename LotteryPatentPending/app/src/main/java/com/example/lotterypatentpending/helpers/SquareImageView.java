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
    /**
     * Creates a SquareImageView
     * @param context The current context
     */
    public SquareImageView(Context context) {
        super(context);
    }

    /**
     * Creates a SquareImageView with attributes
     * @param context The current context
     * @param attrs The set of attributes to apply
     */
    public SquareImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates a SquareImageView with attributes and style
     * @param context The current context
     * @param attrs The set of attributes to apply
     * @param defStyleAttr The default style
     */
    public SquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Measures the view's dimension
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link android.view.View.MeasureSpec}.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link android.view.View.MeasureSpec}.
     *
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // passes the width measurement specification for both the width and the height, forcing a square aspect ratio.
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
