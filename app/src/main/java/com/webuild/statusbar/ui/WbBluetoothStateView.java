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

/**
 * 蓝牙状态显示控件
 * 显示蓝牙开关和连接状态
 */
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
        setMaxHeight(dpToPx(16));
        setVisibility(VISIBLE);  // 默认显示
    }

    private int dpToPx(int dp) {
        final float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * 设置蓝牙状态
     * @param state -1:不可用/关闭(隐藏), 0:开启未连接, 2:已连接
     */
    private void setState(int state) {
        if (state < 0) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
            setSelected(state == 2);  // 连接状态时选中
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

    /**
     * 蓝牙状态监听器
     * 使用单例模式，多个View共享同一个广播接收器
     */
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
            
            // 创建广播接收器
            this.mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    
                    // 蓝牙开关状态变化
                    if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                        if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_TURNING_ON) {
                            notifyChange(0);  // 蓝牙开启，未连接
                        } else {
                            notifyChange(-1);  // 蓝牙关闭
                        }
                    }
                    
                    // 蓝牙连接状态变化
                    else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                        int connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                        if (connectionState == BluetoothAdapter.STATE_CONNECTED ||
                            connectionState == BluetoothAdapter.STATE_CONNECTING) {
                            notifyChange(2);  // 已连接或连接中
                        } else {
                            notifyChange(0);  // 已断开，但蓝牙仍开启
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
            // 立即更新一次当前状态
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

        /**
         * 注册广播接收器
         */
        private void register() {
            this.isRegister = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            
            // Android 14+ 需要指定 receiver export 标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                this.mContext.registerReceiver(this.mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                this.mContext.registerReceiver(this.mReceiver, filter);
            }
            
            // 初始化当前状态
            try {
                this.mLastState = getBluetoothConnectionState(BluetoothAdapter.getDefaultAdapter());
            } catch (Exception e) {
                this.mLastState = -1;
            }
        }

        /**
         * 注销广播接收器
         */
        private void unregister() {
            try {
                this.mContext.unregisterReceiver(this.mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.isRegister = false;
            this.mLastState = -1;
        }

        /**
         * 通知所有View更新状态
         */
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

    /**
     * 获取蓝牙连接状态
     * @return -2:无蓝牙, -1:关闭, 0:开启未连接, 2:已连接
     */
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
            return 0;  // 默认认为已开启但未连接
        }
    }
}
