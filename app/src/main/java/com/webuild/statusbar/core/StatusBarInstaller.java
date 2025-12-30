package com.webuild.statusbar.core;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.view.WindowCompat;

import com.webuild.statusbar.R;
import com.webuild.statusbar.config.StatusBarConfig;
import com.webuild.statusbar.ui.StatusBarView;

public final class StatusBarInstaller {
    private StatusBarInstaller() {}

    public static void install(Activity activity, StatusBarConfig config) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        View existing = decorView.findViewById(R.id.sdk_status_bar);
        if (existing == null) {
            StatusBarView view = new StatusBarView(activity);
            view.setId(R.id.sdk_status_bar);
            if (config != null) {
                if (config.background != null) {
                    view.setBackground(config.background);
                }
                view.setInterceptTouch(config.interceptTouch);
            }
            decorView.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            android.view.LayoutInflater.from(activity).inflate(R.layout.sdk_status_bar_content, view, true);
            view.bringToFront();
        } else if (existing instanceof StatusBarView) {
            StatusBarView view = (StatusBarView) existing;
            if (config != null) {
                if (config.background != null) {
                    view.setBackground(config.background);
                }
                view.setInterceptTouch(config.interceptTouch);
            }
            if (view.getChildCount() == 0) {
                android.view.LayoutInflater.from(activity).inflate(R.layout.sdk_status_bar_content, view, true);
            }
            view.bringToFront();
        }
        if (config != null) {
            WindowHelper.setLightStatusBar(activity, config.lightIcons);
        }
        WindowHelper.hideSystemStatusBar(activity);
    }
}

