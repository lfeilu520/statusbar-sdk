package com.webuild.statusbar.ui.observers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

public class BatteryObserver extends StateObserver<BatteryObserver.Listener> {
    private static volatile BatteryObserver sInstance;
    private BroadcastReceiver mReceiver;
    private boolean mLastCharging;
    private int mLastLevel = -1;

    public interface Listener {
        void onBatteryChanged(boolean charging, int level);
    }

    public static BatteryObserver getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BatteryObserver.class) {
                if (sInstance == null) {
                    sInstance = new BatteryObserver(context);
                }
            }
        }
        return sInstance;
    }

    private BatteryObserver(Context context) {
        super(context);
    }

    @Override
    protected void onActive() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    parseIntent(intent);
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mAppContext.registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            mAppContext.registerReceiver(mReceiver, filter);
        }

        // Get initial state
        Intent sticky = mAppContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (sticky != null) {
            parseIntent(sticky);
        }
    }

    @Override
    protected void onInactive() {
        if (mReceiver != null) {
            try {
                mAppContext.unregisterReceiver(mReceiver);
            } catch (Exception e) {}
            mReceiver = null;
        }
    }

    @Override
    protected void onNotify(Listener listener) {
        if (mLastLevel != -1) {
            listener.onBatteryChanged(mLastCharging, mLastLevel);
        }
    }

    private void parseIntent(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

        int percentage = (int) ((level / (float) scale) * 100);
        boolean charging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                (status == BatteryManager.BATTERY_STATUS_FULL) ||
                (plugged != 0);

        if (charging != mLastCharging || percentage != mLastLevel) {
            mLastCharging = charging;
            mLastLevel = percentage;
            notifyObservers(listener -> listener.onBatteryChanged(charging, percentage));
        }
    }
}
