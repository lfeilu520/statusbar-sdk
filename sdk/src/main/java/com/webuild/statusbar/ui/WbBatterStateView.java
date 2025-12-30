package com.webuild.statusbar.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

import com.webuild.statusbar.R;
import com.webuild.statusbar.ui.base.BaseWatcher;

import java.util.Locale;

public class WbBatterStateView extends AppCompatTextView {
    private static final int LOW_WARNING_THRESHOLD = 20;
    private boolean mBatteryCharging;
    private int mBatteryLevel;
    private BatterStateWatcher mWatcher;

    public WbBatterStateView(Context context) {
        this(context, null);
    }

    public WbBatterStateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WbBatterStateView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setCompoundDrawablePadding(dpToPx(3));
        setBatteryCharging(false);
        setBatteryLevel(100);
    }

    private int dpToPx(int dp) {
        final float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
        BatterStateWatcher watcher = BatterStateWatcher.obtainWatcher(getContext());
        this.mWatcher = watcher;
        watcher.watch(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mWatcher != null) {
            mWatcher.unwatch(this);
            mWatcher = null;
        }
        super.onDetachedFromWindow();
    }

    static class BatterStateWatcher extends BaseWatcher<WbBatterStateView> {
        private static BatterStateWatcher sInstance;
        private boolean isRegister;
        private boolean lastCharging;
        private int lastLevel;
        private final BroadcastReceiver mReceiver;

        static BatterStateWatcher obtainWatcher(Context context) {
            if (sInstance == null) {
                sInstance = new BatterStateWatcher(context.getApplicationContext());
            }
            return sInstance;
        }

        static void release() {
            sInstance = null;
        }

        BatterStateWatcher(Context context) {
            super(context);
            this.isRegister = false;
            this.lastCharging = false;
            this.lastLevel = -1;
            this.mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                        int percentage = (int) (level * 100.0f / scale);
                        boolean charging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                                (status == BatteryManager.BATTERY_STATUS_FULL) ||
                                (plugged != 0);
                        notifyChange(charging, percentage);
                    }
                }
            };
        }

        @Override
        protected boolean watch(final WbBatterStateView view) {
            boolean result = super.watch(view);
            if (!this.isRegister) {
                register();
            }
            view.post(() -> {
                view.setBatteryCharging(lastCharging);
                view.setBatteryLevel(lastLevel);
            });
            return result;
        }

        @Override
        protected boolean unwatch(WbBatterStateView view) {
            boolean result = super.unwatch(view);
            if (this.isRegister && this.mListView.isEmpty()) {
                unregister();
                release();
            }
            return result;
        }

        private void register() {
            this.isRegister = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                this.mContext.registerReceiver(this.mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                this.mContext.registerReceiver(this.mReceiver, filter);
            }
            Intent sticky = this.mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (sticky != null) {
                int level = sticky.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = sticky.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int status = sticky.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int plugged = sticky.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                int percentage = scale > 0 ? (int) (level * 100.0f / scale) : 0;
                boolean charging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                        (status == BatteryManager.BATTERY_STATUS_FULL) ||
                        (plugged != 0);
                this.lastCharging = charging;
                this.lastLevel = percentage;
                notifyChange(charging, percentage);
            }
        }

        private void unregister() {
            try {
                this.mContext.unregisterReceiver(this.mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.isRegister = false;
            this.lastLevel = -1;
        }

        private void notifyChange(final boolean charging, final int level) {
            if (this.lastCharging == charging && this.lastLevel == level) {
                return;
            }
            this.lastCharging = charging;
            this.lastLevel = level;
            for (final WbBatterStateView view : this.mListView) {
                view.post(() -> {
                    view.setBatteryCharging(charging);
                    view.setBatteryLevel(level);
                });
            }
        }
    }
}
