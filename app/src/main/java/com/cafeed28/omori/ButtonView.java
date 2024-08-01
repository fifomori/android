package com.cafeed28.omori;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
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

    private final int mSize;
    private final Rect mRect;
    private final Rect mContentRect;

    private boolean mPressed;

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
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ButtonView, defStyleAttr, defStyleRes);

        mDrawablePressed = a.getDrawable(R.styleable.ButtonView_drawablePressed);
        mDrawableReleased = a.getDrawable(R.styleable.ButtonView_drawableReleased);
        mDrawableContent = a.getDrawable(R.styleable.ButtonView_drawableContent);

        mSize = a.getDimensionPixelSize(R.styleable.ButtonView_buttonSize, 48);
        int inset = mSize / a.getInteger(R.styleable.ButtonView_insetFactor, 3);

        mRect = new Rect(0, 0, mSize, mSize);

        mContentRect = new Rect(inset, inset, mSize - inset, mSize - inset);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        Drawable background = mPressed ? mDrawablePressed : mDrawableReleased;
        int color = mPressed ? Color.BLACK : Color.WHITE;
        int alpha = mPressed ? 255 : 63;

        background.setBounds(mRect);
        background.setAlpha(alpha);
        background.draw(canvas);

        if (mDrawableContent != null) {
            mDrawableContent.setBounds(mContentRect);
            mDrawableContent.setAlpha(alpha);
            mDrawableContent.setTint(color);
            mDrawableContent.draw(canvas);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // touch events handled in ButtonGroupView
        if (getParent() instanceof ButtonGroupView) return true;

        int action = event.getAction();
        int x = (int)event.getX();
        int y = (int)event.getY();

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
