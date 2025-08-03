package com.cafeed28.omori;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ButtonView extends View {
    public interface Listener {
        void onClick(boolean pressed);
    }

    private Listener mListener;

    private final Drawable mDrawablePressed;
    private final Drawable mDrawableReleased;
    private final Drawable mDrawableContent;

    private int mSize;
    private final int mInsetFactor;
    private Rect mRect;
    private Rect mContentRect;

    private boolean mPressed;
    private int mAlphaPressed;
    private int mAlphaReleased = 255;

    private Paint mPaint = new Paint();

    public void setListener(Listener l) {
        mListener = l;
    }

    public ButtonView(Context context) {
        this(context, null, 0, 0);
    }

    public ButtonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        try (TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ButtonView, defStyleAttr, defStyleRes)) {
            mDrawablePressed = a.getDrawable(R.styleable.ButtonView_drawablePressed);
            mDrawableReleased = a.getDrawable(R.styleable.ButtonView_drawableReleased);
            mDrawableContent = a.getDrawable(R.styleable.ButtonView_drawableContent);

            mSize = a.getDimensionPixelSize(R.styleable.ButtonView_buttonSize, 48);
            mInsetFactor = a.getInteger(R.styleable.ButtonView_insetFactor, 3);
            recomputeSizeDependents();
        }

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        mPaint.setAntiAlias(true);
    }

    private void recomputeSizeDependents() {
        int inset = mSize / mInsetFactor;
        mRect = new Rect(0, 0, mSize, mSize);

        mDrawablePressed.setBounds(mRect);
        mDrawableReleased.setBounds(mRect);
        mContentRect = new Rect(inset, inset, mSize - inset, mSize - inset);
        if (mDrawableContent != null) {
            mDrawableContent.setBounds(mContentRect);
        }

        requestLayout();
        invalidate();
    }

    public void setParams(int alphaPressed, int alphaReleased, int buttonSize) {
        mAlphaPressed = alphaPressed;
        mAlphaReleased = alphaReleased;

        mDrawablePressed.setAlpha(mAlphaPressed);
        mDrawableReleased.setAlpha(mAlphaReleased);

        if (buttonSize != -1) {
            mSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, buttonSize, getResources().getDisplayMetrics());
            recomputeSizeDependents();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (mPressed) {
            // drawing as mask
            if (mDrawableContent != null) {
                mDrawableContent.setTint(Color.BLACK);
                mDrawableContent.setAlpha(255);
                mDrawableContent.draw(canvas);
            }

            canvas.saveLayer(mRect.left, mRect.top, mRect.right, mRect.bottom, mPaint);
            mDrawablePressed.draw(canvas);
            canvas.restore();
        } else {
            mDrawableReleased.draw(canvas);

            if (mDrawableContent != null) {
                mDrawableContent.setTint(Color.WHITE);
                mDrawableContent.setAlpha(mAlphaReleased);
                mDrawableContent.draw(canvas);
            }
        }
    }

    public void dispatchPressed(boolean pressed) {
        boolean prevPressed = mPressed;
        mPressed = pressed;

        if (mPressed != prevPressed) {
            invalidate();
            if (mListener != null) {
                mListener.onClick(mPressed);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // touch events handled in ButtonGroupView
        if (getParent() instanceof ButtonGroupView) return true;

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_UP:
                dispatchPressed(false);
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                dispatchPressed(mRect.contains(x, y));
                break;
        }

        return true;
    }
}
