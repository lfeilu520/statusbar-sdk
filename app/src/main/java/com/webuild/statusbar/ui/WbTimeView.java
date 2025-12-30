package com.webuild.statusbar.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatTextView;

import com.webuild.statusbar.R;

import java.util.List;
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
        post(this::onUpdateTime_12_24);
    }

    private void onUpdateTime_12_24() {
        if (DateFormat.is24HourFormat(getContext())) {
            this.mFormatResId = R.string.wb_time_format_24;
        } else {
            this.mFormatResId = R.string.wb_time_format_12;
        }
        onUpdateTime();
    }

    private void onUpdateTime() {
        setText(DateFormat.format(getContext().getText(this.mFormatResId), System.currentTimeMillis()));
    }

    static class TimeWatcher {
        private static TimeWatcher sInstance;
        private final Context mContext;
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_TIME_TICK.equals(action) ||
                    Intent.ACTION_TIME_CHANGED.equals(action) ||
                    Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                    notifyUpdateTime();
                }
            }
        };
        private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                notifyUpdateTime_12_24();
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

        void watch(WbTimeView view) {
            this.mListView.add(view);
            if (this.mListView.isEmpty() || this.isRegister) {
                return;
            }
            this.isRegister = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_TIME_TICK);
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            
            // Android 14+ 需要指定 receiver export 标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                this.mContext.registerReceiver(this.mReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                this.mContext.registerReceiver(this.mReceiver, intentFilter);
            }
            this.mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.TIME_12_24), false, this.mObserver);
        }

        void unwatch(WbTimeView view) {
            this.mListView.remove(view);
            if (this.mListView.isEmpty() && this.isRegister) {
                try {
                    this.mContext.unregisterReceiver(this.mReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
                this.isRegister = false;
                release();
            }
        }

        protected void notifyUpdateTime_12_24() {
            for (final WbTimeView view : this.mListView) {
                if (view != null) {
                    view.post(view::onUpdateTime_12_24);
                }
            }
        }

        protected void notifyUpdateTime() {
            for (final WbTimeView view : this.mListView) {
                if (view != null) {
                    view.post(view::onUpdateTime);
                }
            }
        }
    }
}

