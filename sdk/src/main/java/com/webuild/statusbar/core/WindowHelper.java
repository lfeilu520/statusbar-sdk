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

/**
 * 窗口辅助类 - 使用 WindowInsetsControllerCompat 统一控制系统状态栏
 * 适用于 Android 11+ (API 30+)，通过 AndroidX 向后兼容
 */
public final class WindowHelper {
    private WindowHelper() {}

    /**
     * 获取 WindowInsetsControllerCompat 实例
     */
    @NonNull
    private static WindowInsetsControllerCompat getInsetsController(@NonNull Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        return WindowCompat.getInsetsController(window, decorView);
    }

    /**
     * 设置是否让内容延伸到系统栏区域
     */
    public static void setDecorFitsSystemWindows(@NonNull Activity activity, boolean fits) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), fits);
    }

    /**
     * 设置状态栏图标颜色模式
     * @param light true = 深色图标(适合浅色背景), false = 浅色图标(适合深色背景)
     */
    public static void setLightStatusBar(@NonNull Activity activity, boolean light) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.setAppearanceLightStatusBars(light);
    }

    /**
     * 隐藏系统状态栏
     */
    public static void hideSystemStatusBar(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.hide(WindowInsetsCompat.Type.statusBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    /**
     * 显示系统状态栏
     */
    public static void showSystemStatusBar(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.show(WindowInsetsCompat.Type.statusBars());
    }

    /**
     * 初始化窗口以支持自定义状态栏
     * 应该在 Activity.onCreate() 中 super.onCreate() 之后、setContentView() 之前调用
     */
    public static void prepareForCustomStatusBar(@NonNull Activity activity) {
        Window window = activity.getWindow();
        
        // 1. 设置状态栏透明 (仍需要此设置以确保透明)
        window.setStatusBarColor(Color.TRANSPARENT);
        
        // 2. Edge-to-edge: 内容延伸到系统栏区域
        WindowCompat.setDecorFitsSystemWindows(window, false);
        
        // 3. 使用 WindowInsetsControllerCompat 隐藏系统状态栏
        hideSystemStatusBar(activity);
        
        // 4. 处理刘海屏/挖孔屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }

    /**
     * 隐藏系统导航栏
     */
    public static void hideSystemNavigationBar(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.hide(WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    /**
     * 隐藏所有系统栏（状态栏 + 导航栏）
     */
    public static void hideSystemBars(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    /**
     * 显示所有系统栏
     */
    public static void showSystemBars(@NonNull Activity activity) {
        WindowInsetsControllerCompat controller = getInsetsController(activity);
        controller.show(WindowInsetsCompat.Type.systemBars());
    }
}
