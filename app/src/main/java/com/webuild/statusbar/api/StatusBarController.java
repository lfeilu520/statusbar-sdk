package com.webuild.statusbar.api;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;

import com.webuild.statusbar.R;
import com.webuild.statusbar.config.StatusBarConfig;
import com.webuild.statusbar.core.StatusBarInstaller;
import com.webuild.statusbar.core.WindowHelper;
import com.webuild.statusbar.ui.StatusBarView;

public final class StatusBarController {
    private StatusBarController() {}

    public static void install(Activity activity, StatusBarConfig config) {
        StatusBarInstaller.install(activity, config);
    }

    public static void setLightMode(Activity activity, boolean light) {
        WindowHelper.setLightStatusBar(activity, light);
    }

    public static StatusBarView get(Activity activity) {
        Window window = activity.getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        return decorView.findViewById(R.id.sdk_status_bar);
    }
}

