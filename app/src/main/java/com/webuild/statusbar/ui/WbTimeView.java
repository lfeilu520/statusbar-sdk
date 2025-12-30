package com.webuild.statusbar.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatTextView;
import com.webuild.statusbar.R;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


public class WbTimeView extends AppCompatTextView {
    private int mFormatResId;
    private TimeWatcher mWatcher;

    public WbTimeView(Context context) {
        super(context);
        this.mFormatResId = R.string.wb_time_format_24;
    }

    public WbTimeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFormatResId = R.string.wb_time_format_24;
    }

    public WbTimeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFormatResId = R.string.wb_time_format_24;
        setTextColor(0xFF000000);  // 黑色文字
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initShow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TimeWatcher watcher = TimeWatcher.obtainWatcher(getContext());
        this.mWatcher = watcher;
        watcher.watch(this);
        initShow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mWatcher != null) {
            mWatcher.unwatch(this);
            mWatcher = null;
        }
        super.onDetachedFromWindow();
    }

    private void initShow() {
        post(new Runnable() {
            @Override
            public void run() {
                WbTimeView.this.onUpdateTime_12_24();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUpdateTime_12_24() {
        if (DateFormat.is24HourFormat(getContext())) {
            this.mFormatResId = R.string.wb_time_format_24;
        } else {
            this.mFormatResId = R.string.wb_time_format_12;
        }
        onUpdateTime();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUpdateTime() {
        setText(DateFormat.format(getContext().getText(this.mFormatResId), System.currentTimeMillis()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    static class TimeWatcher {
        private static TimeWatcher sInstance;
        private final Context mContext;
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.TIME_TICK".equals(action)) {
                    TimeWatcher.this.notifyUpdateTime();
                } else if ("android.intent.action.TIME_SET".equals(action)) {
                    TimeWatcher.this.notifyUpdateTime();
                } else if ("android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                    TimeWatcher.this.notifyUpdateTime();
                }
            }
        };
        private final ContentObserver mObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean z) {
                super.onChange(z);
                TimeWatcher.this.notifyUpdateTime_12_24();
            }
        };
        private final List<WbTimeView> mListView = new CopyOnWriteArrayList<>();
        private boolean isRegister = false;

        static TimeWatcher obtainWatcher(Context context) {
            if (sInstance == null) {
                sInstance = new TimeWatcher(context);
            }
            return sInstance;
        }

        private static void release() {
            sInstance = null;
        }

        public TimeWatcher(Context context) {
            this.mContext = context.getApplicationContext();
        }

        void watch(WbTimeView WbTimeView) {
            this.mListView.add(WbTimeView);
            if (this.mListView.isEmpty() || this.isRegister) {
                return;
            }
            this.isRegister = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.TIME_TICK");
            intentFilter.addAction("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("time_12_24"), false, this.mObserver);
        }

        void unwatch(WbTimeView WbTimeView) {
            this.mListView.remove(WbTimeView);
            if (this.mListView.isEmpty() && this.isRegister) {
                this.mContext.unregisterReceiver(this.mReceiver);
                this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
                this.isRegister = false;
                release();
            }
        }

        protected void notifyUpdateTime_12_24() {
            for (final WbTimeView WbTimeView : this.mListView) {
                Objects.requireNonNull(WbTimeView);
                WbTimeView.post(new Runnable() {
                    @Override
                    public final void run() {
                        WbTimeView.onUpdateTime_12_24();
                    }
                });
            }
        }

        protected void notifyUpdateTime() {
            for (final WbTimeView WbTimeView : this.mListView) {
                Objects.requireNonNull(WbTimeView);
                WbTimeView.post(new Runnable() {
                    @Override
                    public final void run() {
                        WbTimeView.onUpdateTime();
                    }
                });
            }
        }
    }
}

