package com.webuild.statusbar.config;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class StatusBarConfig {
    public Drawable background;
    public boolean lightIcons;
    public boolean interceptTouch;
    public int fixedHeightPx;
    public boolean useSystemInsets = true;
    public boolean networkClickable = false;
    public boolean useXmlContent = true;
    public boolean showNetwork = true;
    public boolean showBluetooth = true;
    public boolean showTime = true;
    public boolean showBattery = true;
    public boolean showTitle = false;
    public CharSequence titleText;

    public StatusBarConfig() {}

    public static StatusBarConfig cameraMode(Context context) {
        StatusBarConfig c = new StatusBarConfig();
        c.background = new ColorDrawable(Color.BLACK);
        c.lightIcons = true;
        c.interceptTouch = true;
        c.useSystemInsets = true;
        c.networkClickable = false;
        c.useXmlContent = true;
        c.showNetwork = true;
        c.showBluetooth = true;
        c.showTime = true;
        c.showBattery = true;
        c.showTitle = false;
        return c;
    }

    public static StatusBarConfig white(Context context) {
        StatusBarConfig c = new StatusBarConfig();
        c.background = new ColorDrawable(Color.WHITE);
        c.lightIcons = false;
        c.interceptTouch = false;
        c.useSystemInsets = true;
        c.networkClickable = false;
        c.useXmlContent = true;
        c.showNetwork = true;
        c.showBluetooth = true;
        c.showTime = true;
        c.showBattery = true;
        c.showTitle = false;
        return c;
    }

    public static StatusBarConfig whiteFixed(Context context, int dp) {
        StatusBarConfig c = white(context);
        float d = context.getResources().getDisplayMetrics().density;
        c.fixedHeightPx = Math.round(dp * d);
        c.useSystemInsets = false;
        c.networkClickable = false;
        c.useXmlContent = true;
        c.showTitle = false;
        return c;
    }
}
