package com.webuild.statusbar.config;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class StatusBarConfig {
    public Drawable background;
    public boolean lightIcons;
    public boolean interceptTouch;

    public StatusBarConfig() {}

    public static StatusBarConfig cameraMode(Context context) {
        StatusBarConfig c = new StatusBarConfig();
        c.background = new ColorDrawable(Color.BLACK);
        c.lightIcons = true;
        c.interceptTouch = true;
        return c;
    }

    public static StatusBarConfig white(Context context) {
        StatusBarConfig c = new StatusBarConfig();
        c.background = new ColorDrawable(Color.WHITE);
        c.lightIcons = false;
        c.interceptTouch = false;
        return c;
    }
}

