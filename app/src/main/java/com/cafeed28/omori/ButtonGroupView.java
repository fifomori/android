package com.cafeed28.omori;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;

public class ButtonGroupView extends GridLayout {
    public ButtonGroupView(Context context) {
        this(context, null, 0, 0);
    }

    public ButtonGroupView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ButtonGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ButtonGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // thanks to https://stackoverflow.com/a/23725322/22076815
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            Rect rect = new Rect();
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                return true;
            }
        }

        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int)event.getX();
        int y = (int)event.getY();

        boolean pressed;
        switch (action) {
            case MotionEvent.ACTION_UP:
                pressed = false;
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                pressed = true;
                break;
            default:
                return true;
        }

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            Rect rect = new Rect();
            child.getHitRect(rect);
            if (!(child instanceof ButtonView)) continue;

            ButtonView button = (ButtonView) child;
            button.dispatchPressed(rect.contains(x, y) && pressed);
        }

        return true;
    }
}
