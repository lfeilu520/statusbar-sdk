package com.webuild.statusbar.ui.observers;

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

import androidx.annotation.NonNull;

import com.webuild.statusbar.R;

public class NetworkObserver extends StateObserver<NetworkObserver.Listener> {
    private static volatile NetworkObserver sInstance;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private BroadcastReceiver mWifiReceiver;
    private int mLastIconRes = -1;

    public interface Listener {
        void onNetworkStateChanged(int iconResId);
    }

    public static NetworkObserver getInstance(Context context) {
        if (sInstance == null) {
            synchronized (NetworkObserver.class) {
                if (sInstance == null) {
                    sInstance = new NetworkObserver(context);
                }
            }
        }
        return sInstance;
    }

    private NetworkObserver(Context context) {
        super(context);
    }

    @Override
    protected void onActive() {
        startMonitoring();
    }

    @Override
    protected void onInactive() {
        stopMonitoring();
    }

    @Override
    protected void onNotify(Listener listener) {
        if (mLastIconRes != -1) {
            listener.onNetworkStateChanged(mLastIconRes);
        } else {
            // Calculate current state immediately if not yet cached
            int icon = getCurrentNetworkIcon();
            listener.onNetworkStateChanged(icon);
        }
    }

    private void startMonitoring() {
        ConnectivityManager cm = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    postUpdate();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    postUpdate();
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    postUpdate();
                }
            };
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            cm.registerNetworkCallback(request, mNetworkCallback);
        }

        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                postUpdate();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mAppContext.registerReceiver(mWifiReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            mAppContext.registerReceiver(mWifiReceiver, filter);
        }

        postUpdate();
    }

    private void stopMonitoring() {
        if (mNetworkCallback != null) {
            ConnectivityManager cm = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                try {
                    cm.unregisterNetworkCallback(mNetworkCallback);
                } catch (Exception e) {
                    // Ignore
                }
            }
            mNetworkCallback = null;
        }
        if (mWifiReceiver != null) {
            try {
                mAppContext.unregisterReceiver(mWifiReceiver);
            } catch (Exception e) {
                // Ignore
            }
            mWifiReceiver = null;
        }
        mMainHandler.removeCallbacksAndMessages(null);
    }

    private void postUpdate() {
        mMainHandler.removeCallbacks(mUpdateRunnable);
        mMainHandler.postDelayed(mUpdateRunnable, 200);
    }

    private final Runnable mUpdateRunnable = () -> {
        int icon = getCurrentNetworkIcon();
        if (icon != mLastIconRes) {
            mLastIconRes = icon;
            notifyObservers(listener -> listener.onNetworkStateChanged(icon));
        }
    };

    private int getCurrentNetworkIcon() {
        ConnectivityManager cm = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return 0;

        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) return 0;

        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
        if (caps == null) return 0;

        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return getWifiIcon();
        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return R.drawable.ic_status_ethernet;
        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return R.drawable.ic_status_cellular;
        }

        return 0;
    }

    private int getWifiIcon() {
        WifiManager wm = (WifiManager) mAppContext.getSystemService(Context.WIFI_SERVICE);
        if (wm != null && wm.isWifiEnabled()) {
            WifiInfo info = wm.getConnectionInfo();
            int level = 0;
            if (info != null) {
                level = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            }
            switch (level) {
                case 4: return R.drawable.ic_wifi_full;
                case 3: return R.drawable.ic_wifi_high;
                case 2: return R.drawable.ic_wifi_medium;
                case 1: return R.drawable.ic_wifi_low;
                default: return R.drawable.ic_wifi_weak;
            }
        }
        return 0;
    }
}
