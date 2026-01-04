package com.webuild.statusbar.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

import com.webuild.statusbar.R;
import com.webuild.statusbar.ui.observers.BatteryObserver;

import java.util.Locale;

/**
 * 电池状态显示控件
 * 显示电池电量百分比和充电状态
 */
public class BatteryView extends AppCompatTextView implements BatteryObserver.Listener {
    private static final int LOW_WARNING_THRESHOLD = 20;

    private boolean mBatteryCharging;
    private int mBatteryLevel;

    public BatteryView(Context context) {
        this(context, null);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mBatteryCharging = false;
        this.mBatteryLevel = 99;
        init();
    }

    private void init() {
        setTextColor(0xFF000000);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        setTypeface(getTypeface(), Typeface.BOLD);
        setIncludeFontPadding(false);
        setSingleLine(true);
        setGravity(Gravity.CENTER_VERTICAL);
        setCompoundDrawablePadding(dpToPx(0));
        setBatteryCharging(false);
        setBatteryLevel(100);
    }

    private int dpToPx(int dp) {
        final float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onBatteryChanged(boolean charging, int level) {
        post(() -> {
            setBatteryCharging(charging);
            setBatteryLevel(level);
        });
    }

    public void setBatteryCharging(boolean charging) {
        if (this.mBatteryCharging == charging) {
            return;
        }
        this.mBatteryCharging = charging;
        updateDisplay();
    }

    public void setBatteryLevel(int level) {
        if (this.mBatteryLevel == level) {
            return;
        }
        this.mBatteryLevel = level;
        setText(String.format(Locale.getDefault(), "%d%%", this.mBatteryLevel));
        updateDisplay();
    }

    private void updateDisplay() {
        int iconRes;
        if (mBatteryCharging) {
            iconRes = R.drawable.ic_battery_charging_horizontal;
        } else if (mBatteryLevel >= 90) {
            iconRes = R.drawable.ic_battery_full_horizontal;
        } else if (mBatteryLevel >= 60) {
            iconRes = R.drawable.ic_battery_high_horizontal;
        } else if (mBatteryLevel >= 30) {
            iconRes = R.drawable.ic_battery_medium_horizontal;
        } else {
            iconRes = R.drawable.ic_battery_low_horizontal;
        }

        android.graphics.drawable.Drawable icon = getContext().getDrawable(iconRes);
        if (icon != null) {
            int width = Math.round(icon.getIntrinsicWidth() * 1.8f);
            int height = Math.round(icon.getIntrinsicHeight() * 1.8f);
            icon.setBounds(0, 0, width, height);
        }

        setCompoundDrawablesRelative(null, null, icon, null);

        if (mBatteryCharging) {
            setTextColor(0xFF000000);
        } else if (mBatteryLevel < LOW_WARNING_THRESHOLD) {
            setTextColor(0xFFFF5555);
        } else {
            setTextColor(0xFF000000);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BatteryObserver.getInstance(getContext()).addObserver(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        BatteryObserver.getInstance(getContext()).removeObserver(this);
        super.onDetachedFromWindow();
    }
}
