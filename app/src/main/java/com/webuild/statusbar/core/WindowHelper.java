package com.webuild.statusbar.core;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsCompat;

public final class WindowHelper {
    private WindowHelper() {}

    public static void setDecorFits(Window window, boolean fits) {
        WindowCompat.setDecorFitsSystemWindows(window, fits);
    }

    public static void makeStatusBarTransparent(Window window) {
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    public static void setLightStatusBar(Activity activity, boolean light) {
        Window window = activity.getWindow();
        View decor = window.getDecorView();
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, decor);
        controller.setAppearanceLightStatusBars(light);
    }

    public static void hideSystemStatusBar(Activity activity) {
        Window window = activity.getWindow();
        View decor = window.getDecorView();
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, decor);
        controller.hide(WindowInsetsCompat.Type.statusBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE);
    }
}

