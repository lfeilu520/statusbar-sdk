package com.webuild.statusbar.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.webuild.statusbar.ui.observers.NetworkObserver;

/**
 * 网络状态显示控件
 * 显示WiFi、以太网、移动网络等状态
 */
public class NetworkView extends AppCompatImageView implements NetworkObserver.Listener {
    private boolean clickEnabled = false;

    public NetworkView(Context context) {
        super(context);
        init();
    }

    public NetworkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NetworkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setAdjustViewBounds(true);
        setScaleType(ImageView.ScaleType.FIT_CENTER);
        setMaxHeight(dpToPx(30));
        setVisibility(GONE);
        setOnClickListener(v -> {
            if (!clickEnabled) return;
            showNetworkDialog();
        });
    }

    @Override
    public void onNetworkStateChanged(int iconResId) {
        post(() -> changeStatusIcon(iconResId));
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
        Context ctx = getContext();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View content = inflater.inflate(com.webuild.statusbar.R.layout.dialog_network_info, null);
        TextView tvType = content.findViewById(com.webuild.statusbar.R.id.tv_network_type);
        TextView tvWifi = content.findViewById(com.webuild.statusbar.R.id.tv_wifi_info);
        Switch swWifi = content.findViewById(com.webuild.statusbar.R.id.sw_wifi_toggle);

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
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Intent intent = new Intent(android.provider.Settings.Panel.ACTION_WIFI);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        NetworkObserver.getInstance(getContext()).addObserver(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        NetworkObserver.getInstance(getContext()).removeObserver(this);
        super.onDetachedFromWindow();
    }
}
