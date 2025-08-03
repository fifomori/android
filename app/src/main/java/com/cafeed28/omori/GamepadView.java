package com.cafeed28.omori;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("RtlHardcoded")
public class GamepadView extends ViewGroup {
    public GamepadView(Context context) {
        this(context, null);
    }

    public GamepadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GamepadView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GamepadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        OmoApplication application = (OmoApplication) getContext().getApplicationContext();
        SharedPreferences preferences = application.getPreferences();

        int gamepadInsets = preferences.getInt(getContext().getString(R.string.preference_gamepad_insets), -1);

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (params.fixed) continue;

            if (gamepadInsets != -1) {
                params.offsetX += gamepadInsets;
                child.setLayoutParams(params);
            }
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            LayoutParams params = (LayoutParams) child.getLayoutParams();

            final int gravityHorizontal = params.gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            final int gravityVertical = params.gravity & Gravity.VERTICAL_GRAVITY_MASK;

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            int left = 0;
            int top = 0;

            switch (gravityHorizontal) {
                case Gravity.LEFT:
                    left = params.offsetX;
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    left = (r / 2) - params.offsetX - (width / 2);
                    break;
                case Gravity.RIGHT:
                    left = r - params.offsetX - width;
                    break;
            }

            switch (gravityVertical) {
                case Gravity.TOP:
                    top = params.offsetY;
                    break;
                case Gravity.CENTER_VERTICAL:
                    top = (b / 2) - params.offsetY - (height / 2);
                    break;
                case Gravity.BOTTOM:
                    top = b - params.offsetY - height;
                    break;
            }

            child.layout(left, top, left + width, top + height);
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int offsetX;
        public int offsetY;
        public int gravity;
        public boolean fixed;

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            try (TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.GamepadView_Layout)) {
                gravity = a.getInt(R.styleable.GamepadView_Layout_gravity, Gravity.TOP | Gravity.LEFT);
                offsetX = a.getDimensionPixelSize(R.styleable.GamepadView_Layout_offsetX, 0);
                offsetY = a.getDimensionPixelSize(R.styleable.GamepadView_Layout_offsetY, 0);
                fixed = a.getBoolean(R.styleable.GamepadView_Layout_fixed, false);
            }
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
