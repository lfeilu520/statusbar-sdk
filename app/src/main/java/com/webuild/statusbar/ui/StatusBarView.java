package com.webuild.statusbar.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StatusBarView extends FrameLayout {
    private boolean interceptTouch = false;
    private int fallbackHeight = -1;

    public StatusBarView(Context context) {
        super(context);
        init();
    }

    public StatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFitsSystemWindows(false);
        if (getLayoutParams() == null) {
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            if (top == 0) {
                if (fallbackHeight < 0) {
                    int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                    if (resId > 0) {
                        fallbackHeight = getResources().getDimensionPixelSize(resId);
                    } else {
                        float density = getResources().getDisplayMetrics().density;
                        fallbackHeight = (int) (24 * density);
                    }
                }
                top = fallbackHeight;
            }
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, top);
            } else {
                lp.height = top;
            }
            v.setLayoutParams(lp);
            return insets;
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewCompat.requestApplyInsets(this);
    }

    public void setInterceptTouch(boolean intercept) {
        this.interceptTouch = intercept;
        setClickable(intercept);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return interceptTouch;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return interceptTouch;
    }
}

