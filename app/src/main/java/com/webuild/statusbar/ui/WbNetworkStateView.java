package com.webuild.statusbar.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import com.webuild.statusbar.R;
import com.webuild.statusbar.ui.base.BaseWatcher;

/**
 * 网络状态显示控件
 * 显示WiFi、以太网、移动网络等状态
 */
public class WbNetworkStateView extends AppCompatImageView {
    private NetworkStateWatcher mWatcher;

    public WbNetworkStateView(Context context) {
        super(context);
        init();
    }

    public WbNetworkStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WbNetworkStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 按照原始实现：空方法
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeStatusIcon(int i) {
        if (i <= 0) {
            setVisibility(GONE);
        } else {
            setImageResource(i);
            setVisibility(VISIBLE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NetworkStateWatcher networkStateWatcherObtainWatcher = NetworkStateWatcher.obtainWatcher(getContext());
        this.mWatcher = networkStateWatcherObtainWatcher;
        networkStateWatcherObtainWatcher.watch(this);
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
     * 网络状态监听器
     * 使用单例模式，多个View共享同一个广播接收器
     */
    public static class NetworkStateWatcher extends BaseWatcher<WbNetworkStateView> {
        private static NetworkStateWatcher sInstance;
        private boolean isRegister;
        private int mLastResId;
        private final BroadcastReceiver mReceiver;

        public static NetworkStateWatcher obtainWatcher(Context context) {
            if (sInstance == null) {
                sInstance = new NetworkStateWatcher(context.getApplicationContext());
            }
            return sInstance;
        }

        static void release() {
            sInstance = null;
        }

        private NetworkStateWatcher(Context context) {
            super(context);
            this.mLastResId = -1;
            this.isRegister = false;
            
            // 创建广播接收器，延迟250ms更新避免频繁刷新
            this.mReceiver = new BroadcastReceiver() {
                private final Handler mHandler = new Handler(Looper.getMainLooper());
                private final Runnable mRunnable = () -> notifyChange();

                @Override
                public void onReceive(Context context, Intent intent) {
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.postDelayed(mRunnable, 250L);
                }
            };
        }

        @Override
        public boolean watch(final WbNetworkStateView WbNetworkStateView) {
            boolean zWatch = super.watch(WbNetworkStateView);  // 修复：移除错误的类型转换
            if (!this.isRegister) {
                register();
            }
            WbNetworkStateView.post(new Runnable() {
                @Override
                public final void run() {
                    NetworkStateWatcher.this.lambda$watch$0$WbNetworkStateView$NetworkStateWatcher(WbNetworkStateView);
                }
            });
            return zWatch;
        }

        /* synthetic */ void lambda$watch$0$WbNetworkStateView$NetworkStateWatcher(WbNetworkStateView WbNetworkStateView) {
            WbNetworkStateView.changeStatusIcon(this.mLastResId);
        }

        @Override // com.launcher.demo.widget.statusbar.base.BaseWatcher
        public boolean unwatch(WbNetworkStateView WbNetworkStateView) {
            boolean zUnwatch = super.unwatch(WbNetworkStateView);  // 修复：移除错误的类型转换
            if (this.isRegister && this.mListView.isEmpty()) {
                unregister();
                release();
            }
            return zUnwatch;
        }

        private void register() {
            this.isRegister = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
            intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            notifyChange();
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
        }

        /**
         * 通知所有View更新状态
         */
        private void notifyChange() {
            int currentResId = getCurrentNetworkInfo(this.mContext);
            if (this.mLastResId == currentResId) {
                return;
            }
            this.mLastResId = currentResId;
            
            for (final WbNetworkStateView view : this.mListView) {
                view.post(() -> view.changeStatusIcon(mLastResId));
            }
        }

        /**
         * 获取当前网络状态对应的图标资源ID
         */
        public static int getCurrentNetworkInfo(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return -1;
            }

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isAvailable()) {
                return -1;
            }

            int type = activeNetwork.getType();
            
            // 以太网
            if (type == ConnectivityManager.TYPE_ETHERNET) {
                return R.drawable.ic_status_ethernet;
            }
            
            // WiFi
            if (type == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        int rssi = wifiInfo.getRssi();
                        
                        // 根据信号强度返回不同图标
                        if (rssi > 0) {
                            return -1;  // 无效信号
                        } else if (rssi >= -50) {
                            return R.drawable.ic_status_wifi_signal_3;  // 强信号
                        } else if (rssi >= -80) {
                            return R.drawable.ic_status_wifi_signal_2;  // 中信号
                        } else if (rssi >= -100) {
                            return R.drawable.ic_status_wifi_signal_1;  // 弱信号
                        }
                    }
                }
                return -1;
            }
            
            // 移动网络
            if (type == ConnectivityManager.TYPE_MOBILE) {
                return R.drawable.ic_status_cellular;
            }
            
            return -1;
        }
    }
}
