package com.webuild.statusbar.ui.observers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.lang.reflect.Method;

public class BluetoothObserver extends StateObserver<BluetoothObserver.Listener> {
    private static volatile BluetoothObserver sInstance;
    private BroadcastReceiver mReceiver;
    private int mLastState = -1;

    public interface Listener {
        // -1: off, 0: on/disconnected, 2: connected
        void onBluetoothStateChanged(int state);
    }

    public static BluetoothObserver getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BluetoothObserver.class) {
                if (sInstance == null) {
                    sInstance = new BluetoothObserver(context);
                }
            }
        }
        return sInstance;
    }

    private BluetoothObserver(Context context) {
        super(context);
    }

    @Override
    protected void onActive() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    updateState();
                } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                    updateState();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mAppContext.registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            mAppContext.registerReceiver(mReceiver, filter);
        }
        updateState();
    }

    @Override
    protected void onInactive() {
        if (mReceiver != null) {
            try {
                mAppContext.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                // ignore
            }
            mReceiver = null;
        }
    }

    @Override
    protected void onNotify(Listener listener) {
        listener.onBluetoothStateChanged(mLastState);
    }

    private void updateState() {
        int newState = getBluetoothState();
        if (newState != mLastState) {
            mLastState = newState;
            notifyObservers(listener -> listener.onBluetoothStateChanged(newState));
        }
    }

    private int getBluetoothState() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) return -1;
        if (!adapter.isEnabled()) return -1;

        int connState = 0;
        try {
            // Using reflection as getConnectionState() might be hidden or unavailable in some SDKs
            Method method = BluetoothAdapter.class.getMethod("getConnectionState");
            connState = (Integer) method.invoke(adapter);
        } catch (Exception e) {
            // fallback
            connState = BluetoothAdapter.STATE_DISCONNECTED;
        }

        if (connState == BluetoothAdapter.STATE_CONNECTED || connState == BluetoothAdapter.STATE_CONNECTING) {
            return 2;
        }
        return 0;
    }
}
