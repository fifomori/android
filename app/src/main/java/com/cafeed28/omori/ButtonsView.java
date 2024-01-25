package com.cafeed28.omori;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ButtonsView extends View {
    static class ButtonState {
        public boolean pressed;
        public Drawable drawable;
        public int x;
        public int y;

        public ButtonState(int x, int y, Drawable drawable) {
            this.x = x;
            this.y = y;
            this.drawable = drawable;
        }
    }

    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    public interface ButtonsListener {
        void onButton(int button, boolean pressed);
    }

    private ButtonsListener mButtonsListener;

    private final int mButtonSize;
    private final int mButtonRadius;

    private final ButtonState[] mButtonStates = new ButtonState[4];
    private final Drawable mDrawableButtonPressed;
    private final Drawable mDrawableButtonReleased;

    public void setButtonsListener(ButtonsListener l) {
        mButtonsListener = l;
    }

    public ButtonsView(Context context) {
        this(context, null, 0, 0);
    }

    public ButtonsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ButtonsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ButtonsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ButtonsView, defStyleAttr, defStyleRes);

        mButtonSize = a.getDimensionPixelSize(R.styleable.ButtonsView_buttonSize, 48);
        mButtonRadius = mButtonSize / 2;

        mDrawableButtonPressed = a.getDrawable(R.styleable.ButtonsView_buttonPressedBackground);
        mDrawableButtonReleased = a.getDrawable(R.styleable.ButtonsView_buttonReleasedBackground);

        mButtonStates[TOP] = new ButtonState(
                mButtonSize * 2 - mButtonSize / 2,
                mButtonSize - mButtonSize / 2,
                a.getDrawable(R.styleable.ButtonsView_buttonTopForeground)
        );

        mButtonStates[BOTTOM] = new ButtonState(
                mButtonSize * 2 - mButtonSize / 2,
                mButtonSize * 3 - mButtonSize / 2,
                a.getDrawable(R.styleable.ButtonsView_buttonBottomForeground)
        );

        mButtonStates[LEFT] = new ButtonState(
                mButtonSize - mButtonSize / 2,
                mButtonSize * 2 - mButtonSize / 2,
                a.getDrawable(R.styleable.ButtonsView_buttonLeftForeground)
        );

        mButtonStates[RIGHT] = new ButtonState(
                mButtonSize * 3 - mButtonSize / 2,
                mButtonSize * 2 - mButtonSize / 2,
                a.getDrawable(R.styleable.ButtonsView_buttonRightForeground)
        );

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mButtonSize * 3, mButtonSize * 3);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        for (ButtonState buttonState : mButtonStates) {
            Drawable background = buttonState.pressed ? mDrawableButtonPressed : mDrawableButtonReleased;
            int color = buttonState.pressed ? Color.BLACK : Color.WHITE;

            background.setBounds(
                    buttonState.x - mButtonRadius,
                    buttonState.y - mButtonRadius,
                    buttonState.x + mButtonRadius,
                    buttonState.y + mButtonRadius
            );
            background.draw(canvas);

            if (buttonState.drawable != null) {
                buttonState.drawable.setBounds(
                        buttonState.x - mButtonRadius / 3,
                        buttonState.y - mButtonRadius / 3,
                        buttonState.x + mButtonRadius / 3,
                        buttonState.y + mButtonRadius / 3
                );
                buttonState.drawable.setTint(color);
                buttonState.drawable.draw(canvas);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        for (int i = 0; i < mButtonStates.length; i++) {
            ButtonState mButtonState = mButtonStates[i];

            boolean prevPressed = mButtonState.pressed;
            switch (action) {
                case MotionEvent.ACTION_UP:
                    mButtonState.pressed = false;
                    break;
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // https://stackoverflow.com/a/18295844
                    int x0 = mButtonState.x - mButtonRadius;
                    int y0 = mButtonState.y - mButtonRadius;
                    int x1 = mButtonState.x + mButtonRadius;
                    int y1 = mButtonState.y + mButtonRadius;

                    mButtonState.pressed = x0 <= x && x <= x1 && y0 <= y && y <= y1;
                    break;
            }

            if (mButtonState.pressed != prevPressed) {
                if (mButtonsListener != null) {
                    mButtonsListener.onButton(i, mButtonState.pressed);
                }
            }
        }

        invalidate();
        return true;
    }
}
