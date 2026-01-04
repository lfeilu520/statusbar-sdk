package com.webuild.statusbar.ui.observers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.format.DateFormat;

public class TimeObserver extends StateObserver<TimeObserver.Listener> {
    private static volatile TimeObserver sInstance;
    private BroadcastReceiver mTimeReceiver;
    private ContentObserver mFormatObserver;
    private boolean mIs24HourFormat;

    public interface Listener {
        void onTimeTick();
        void onTimeFormatChanged(boolean is24Hour);
    }

    public static TimeObserver getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TimeObserver.class) {
                if (sInstance == null) {
                    sInstance = new TimeObserver(context);
                }
            }
        }
        return sInstance;
    }

    private TimeObserver(Context context) {
        super(context);
    }

    @Override
    protected void onActive() {
        mTimeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyObservers(Listener::onTimeTick);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mAppContext.registerReceiver(mTimeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            mAppContext.registerReceiver(mTimeReceiver, filter);
        }

        mFormatObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                checkFormat();
            }
        };
        mAppContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.TIME_12_24), false, mFormatObserver);
        
        checkFormat();
    }

    @Override
    protected void onInactive() {
        if (mTimeReceiver != null) {
            try {
                mAppContext.unregisterReceiver(mTimeReceiver);
            } catch (Exception e) {}
            mTimeReceiver = null;
        }
        if (mFormatObserver != null) {
            mAppContext.getContentResolver().unregisterContentObserver(mFormatObserver);
            mFormatObserver = null;
        }
    }

    @Override
    protected void onNotify(Listener listener) {
        listener.onTimeFormatChanged(mIs24HourFormat);
        listener.onTimeTick();
    }

    private void checkFormat() {
        boolean is24 = DateFormat.is24HourFormat(mAppContext);
        if (is24 != mIs24HourFormat) {
            mIs24HourFormat = is24;
            notifyObservers(listener -> listener.onTimeFormatChanged(is24));
        }
    }
}
