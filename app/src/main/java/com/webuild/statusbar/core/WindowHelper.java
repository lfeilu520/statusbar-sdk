package com.webuild.statusbar.core;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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

    /**
     * 隐藏系统状态栏，确保从一开始就不显示
     */
    public static void hideSystemStatusBar(Activity activity) {
        Window window = activity.getWindow();
        View decor = window.getDecorView();
        
        // 使用 WindowInsetsController 隐藏状态栏
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decor);
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.statusBars());
            // 使用 BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE 防止系统栏永久显示
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    /**
     * 初始化窗口以支持自定义状态栏
     * 应该在 Activity.onCreate() 中 setContentView() 之前调用
     */
    public static void prepareForCustomStatusBar(Activity activity) {
        Window window = activity.getWindow();
        
        // 设置窗口标志
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
        
        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false);
        
        // 立即隐藏系统状态栏
        hideSystemStatusBar(activity);
        
        // 处理刘海屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }
}

