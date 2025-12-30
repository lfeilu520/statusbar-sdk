package com.webuild.statusbar.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
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

    private void changeStatusIcon(int i) {
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
     * 使用单例模式，多个View共享同一个NetworkCallback
     */
    public static class NetworkStateWatcher extends BaseWatcher<WbNetworkStateView> {
        private static NetworkStateWatcher sInstance;
        private boolean isRegister;
        private int mLastResId;
        private final Handler mHandler = new Handler(Looper.getMainLooper());
        private final Runnable mUpdateRunnable = this::notifyChange;
        private ConnectivityManager.NetworkCallback mNetworkCallback;
        private final BroadcastReceiver mWifiRssiReceiver;

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
            
            // WiFi信号强度变化广播接收器
            this.mWifiRssiReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    scheduleUpdate();
                }
            };
        }

        private void scheduleUpdate() {
            mHandler.removeCallbacks(mUpdateRunnable);
            mHandler.postDelayed(mUpdateRunnable, 250L);
        }

        @Override
        public boolean watch(final WbNetworkStateView view) {
            boolean result = super.watch(view);
            if (!this.isRegister) {
                register();
            }
            view.post(() -> view.changeStatusIcon(this.mLastResId));
            return result;
        }

        @Override
        public boolean unwatch(WbNetworkStateView view) {
            boolean result = super.unwatch(view);
            if (this.isRegister && this.mListView.isEmpty()) {
                unregister();
                release();
            }
            return result;
        }

        private void register() {
            this.isRegister = true;
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                // 使用现代 NetworkCallback API
                mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        scheduleUpdate();
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        scheduleUpdate();
                    }

                    @Override
                    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
                        scheduleUpdate();
                    }
                };
                
                NetworkRequest request = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();
                cm.registerNetworkCallback(request, mNetworkCallback);
            }
            
            // 注册WiFi RSSI变化广播 (用于信号强度更新)
            IntentFilter filter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                mContext.registerReceiver(mWifiRssiReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                mContext.registerReceiver(mWifiRssiReceiver, filter);
            }
            
            notifyChange();
        }

        private void unregister() {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && mNetworkCallback != null) {
                try {
                    cm.unregisterNetworkCallback(mNetworkCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                mContext.unregisterReceiver(mWifiRssiReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mHandler.removeCallbacks(mUpdateRunnable);
            this.isRegister = false;
        }

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
         * 获取当前网络状态对应的图标资源ID（使用现代API）
         */
        public static int getCurrentNetworkInfo(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return -1;
            }

            Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork == null) {
                return -1;
            }

            NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
            if (caps == null) {
                return -1;
            }

            // 以太网
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return R.drawable.ic_status_ethernet;
            }
            
            // WiFi
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        int rssi = wifiInfo.getRssi();
                        
                        if (rssi > 0) {
                            return -1;
                        } else if (rssi >= -50) {
                            return R.drawable.ic_status_wifi_signal_3;
                        } else if (rssi >= -80) {
                            return R.drawable.ic_status_wifi_signal_2;
                        } else if (rssi >= -100) {
                            return R.drawable.ic_status_wifi_signal_1;
                        }
                    }
                }
                return -1;
            }
            
            // 移动网络
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return R.drawable.ic_status_cellular;
            }
            
            return -1;
        }
    }
}
