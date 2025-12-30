package com.webuild.statusbar.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.webuild.statusbar.R;
import com.webuild.statusbar.ui.base.BaseWatcher;

import java.lang.reflect.Method;

public class WbBluetoothStateView extends AppCompatImageView {
    private BluetoothStateWatcher mWatcher;

    public WbBluetoothStateView(Context context) {
        super(context);
        init();
    }

    public WbBluetoothStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WbBluetoothStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.ic_status_bluetooth);
        setAdjustViewBounds(true);
        setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        setMaxHeight(dpToPx(30));
        setVisibility(VISIBLE);
    }

    private int dpToPx(int dp) {
        final float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setState(int state) {
        if (state < 0) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
            setSelected(state == 2);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BluetoothStateWatcher watcher = BluetoothStateWatcher.obtainWatcher(getContext());
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

    static class BluetoothStateWatcher extends BaseWatcher<WbBluetoothStateView> {
        private static BluetoothStateWatcher sInstance;
        private boolean isRegister;
        private int mLastState;
        private final BroadcastReceiver mReceiver;

        static BluetoothStateWatcher obtainWatcher(Context context) {
            if (sInstance == null) {
                sInstance = new BluetoothStateWatcher(context.getApplicationContext());
            }
            return sInstance;
        }

        static void release() {
            sInstance = null;
        }

        BluetoothStateWatcher(Context context) {
            super(context);
            this.isRegister = false;
            this.mLastState = -1;
            this.mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                        if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_TURNING_ON) {
                            notifyChange(0);
                        } else {
                            notifyChange(-1);
                        }
                    } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                        int connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                        if (connectionState == BluetoothAdapter.STATE_CONNECTED ||
                            connectionState == BluetoothAdapter.STATE_CONNECTING) {
                            notifyChange(2);
                        } else {
                            notifyChange(0);
                        }
                    }
                }
            };
        }

        @Override
        protected boolean watch(final WbBluetoothStateView view) {
            boolean result = super.watch(view);
            if (!this.isRegister) {
                register();
            }
            view.post(() -> view.setState(mLastState));
            return result;
        }

        @Override
        protected boolean unwatch(WbBluetoothStateView view) {
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
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                this.mContext.registerReceiver(this.mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                this.mContext.registerReceiver(this.mReceiver, filter);
            }
            try {
                this.mLastState = getBluetoothConnectionState(BluetoothAdapter.getDefaultAdapter());
            } catch (Exception e) {
                this.mLastState = -1;
            }
        }

        private void unregister() {
            try {
                this.mContext.unregisterReceiver(this.mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.isRegister = false;
            this.mLastState = -1;
        }

        private void notifyChange(final int state) {
            if (this.mLastState == state) {
                return;
            }
            this.mLastState = state;
            for (final WbBluetoothStateView view : this.mListView) {
                view.post(() -> view.setState(state));
            }
        }
    }

    public static int getBluetoothConnectionState(BluetoothAdapter adapter) {
        if (adapter == null) {
            return -2;
        }
        if (!adapter.isEnabled()) {
            return -1;
        }
        try {
            Method method = adapter.getClass().getMethod("getConnectionState");
            method.setAccessible(true);
            return (Integer) method.invoke(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
