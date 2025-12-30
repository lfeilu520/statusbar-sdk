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

public class WbNetworkStateView extends AppCompatImageView {
    private NetworkStateWatcher mWatcher;
    private boolean clickEnabled = false;

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
        setAdjustViewBounds(true);
        setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        setMaxHeight(dpToPx(30));
        setOnClickListener(v -> {
            if (!clickEnabled) return;
            showNetworkDialog();
        });
    }

    private void changeStatusIcon(int i) {
        if (i <= 0) {
            setVisibility(GONE);
        } else {
            setImageResource(i);
            setVisibility(VISIBLE);
        }
    }

    private int dpToPx(int dp) {
        final float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void setClickEnabled(boolean enabled) {
        this.clickEnabled = enabled;
        setClickable(enabled);
    }

    private void showNetworkDialog() {
        android.content.Context ctx = getContext();
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(ctx);
        android.view.View content = inflater.inflate(com.webuild.statusbar.R.layout.dialog_network_info, null);
        android.widget.TextView tvType = content.findViewById(com.webuild.statusbar.R.id.tv_network_type);
        android.widget.TextView tvWifi = content.findViewById(com.webuild.statusbar.R.id.tv_wifi_info);
        android.widget.Switch swWifi = content.findViewById(com.webuild.statusbar.R.id.sw_wifi_toggle);

        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
        Network active = cm != null ? cm.getActiveNetwork() : null;
        NetworkCapabilities caps = cm != null ? cm.getNetworkCapabilities(active) : null;
        boolean wifiEnabled = wifiManager != null && wifiManager.isWifiEnabled();
        boolean wifiTransport = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        boolean ethernet = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        boolean cellular = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);

        String type = ethernet ? "以太网" : (wifiTransport ? "Wi‑Fi" : (cellular ? "蜂窝" : "未连接"));
        tvType.setText("网络类型：" + type);

        String wifiInfoText = "Wi‑Fi：";
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
                int rssi = wifiInfo.getRssi();
                int level = WifiManager.calculateSignalLevel(rssi, 5);
                String ssid = wifiInfo.getSSID();
                wifiInfoText += "已连接 " + ssid + "，强度 " + level + "/5";
            } else {
                wifiInfoText += wifiEnabled ? "未连接" : "已关闭";
            }
        } else {
            wifiInfoText += "不可用";
        }
        tvWifi.setText(wifiInfoText);

        swWifi.setChecked(wifiEnabled);
        swWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (wifiManager == null) return;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.Panel.ACTION_WIFI);
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    ctx.startActivity(intent);
                } catch (Exception e) {
                }
                buttonView.setChecked(wifiManager.isWifiEnabled());
            } else {
                try {
                    wifiManager.setWifiEnabled(isChecked);
                } catch (SecurityException se) {
                }
            }
        });

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(ctx)
                .setTitle("网络状态")
                .setView(content)
                .setPositiveButton("关闭", (d, w) -> d.dismiss())
                .create();
        dialog.show();
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
            IntentFilter filter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
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

        public static int getCurrentNetworkInfo(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Network active = cm != null ? cm.getActiveNetwork() : null;
            NetworkCapabilities caps = cm != null ? cm.getNetworkCapabilities(active) : null;
            boolean wifiEnabled = wifiManager != null && wifiManager.isWifiEnabled();
            boolean wifiTransport = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            if (wifiEnabled) {
                if (wifiTransport) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    int rssi = wifiInfo != null ? wifiInfo.getRssi() : -50;
                    int level = WifiManager.calculateSignalLevel(rssi, 5);
                    switch (level) {
                        case 4:
                            return R.drawable.ic_wifi_full;
                        case 3:
                            return R.drawable.ic_wifi_high;
                        case 2:
                            return R.drawable.ic_wifi_medium;
                        case 1:
                            return R.drawable.ic_wifi_low;
                        default:
                            return R.drawable.ic_wifi_weak;
                    }
                } else {
                    return R.drawable.ic_wifi_off;
                }
            }
            if (cm == null) {
                return R.drawable.ic_wifi_off;
            }
            if (active == null || caps == null) {
                return R.drawable.ic_wifi_off;
            }
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return R.drawable.ic_status_ethernet;
            }
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return R.drawable.ic_status_cellular;
            }
            return R.drawable.ic_wifi_off;
        }
    }
}
