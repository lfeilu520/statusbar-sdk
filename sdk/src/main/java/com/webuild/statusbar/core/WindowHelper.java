package com.webuild.statusbar.core;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public final class WindowHelper {
    private WindowHelper() {}

    @NonNull
    private static WindowInsetsControllerCompat getInsetsController(@NonNull Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        return WindowCompat.getInsetsController(window, decorView);
    }

    public static void setDecorFitsSystemWindows(@NonNull Activity activity, boolean fits) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), fits);
    }

    public static void setLightStatusBar(@NonNull Activity activity, boolean light) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.setAppearanceLightStatusBars(light);
    }

    public static void hideSystemStatusBar(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.hide(WindowInsetsCompat.Type.statusBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    public static void showSystemStatusBar(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.show(WindowInsetsCompat.Type.statusBars());
    }

    public static void prepareForCustomStatusBar(@NonNull Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        WindowCompat.setDecorFitsSystemWindows(window, false);
        hideSystemStatusBar(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }

    public static void hideSystemNavigationBar(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.hide(WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    public static void hideSystemBars(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    public static void showSystemBars(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.show(WindowInsetsCompat.Type.systemBars());
    }
}
