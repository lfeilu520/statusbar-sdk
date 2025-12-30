package com.webuild.statusbar.api;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.webuild.statusbar.R;
import com.webuild.statusbar.config.StatusBarConfig;
import com.webuild.statusbar.core.StatusBarInstaller;
import com.webuild.statusbar.core.WindowHelper;
import com.webuild.statusbar.ui.StatusBarView;

/**
 * StatusBar SDK 主控制器
 * 使用 WindowInsetsControllerCompat 统一控制系统状态栏
 */
public final class StatusBarController {
    private StatusBarController() {}

    /**
     * 准备窗口以使用自定义状态栏
     * 应该在 Activity.onCreate() 中 super.onCreate() 之后、setContentView() 之前调用
     * 这样可以确保系统状态栏从一开始就不显示
     */
    public static void prepare(@NonNull Activity activity) {
        WindowHelper.prepareForCustomStatusBar(activity);
    }

    /**
     * 安装自定义状态栏
     * 可以在 setContentView() 之前或之后调用
     */
    public static void install(@NonNull Activity activity, @Nullable StatusBarConfig config) {
        StatusBarInstaller.install(activity, config);
    }

    public static void setUseSystemInsets(@NonNull Activity activity, boolean use) {
        StatusBarView v = get(activity);
        if (v != null) {
            v.setUseSystemInsets(use);
        }
    }

    public static void setFixedHeightDp(@NonNull Activity activity, int dp) {
        StatusBarView v = get(activity);
        if (v != null) {
            float d = activity.getResources().getDisplayMetrics().density;
            v.setFixedHeightPx(Math.round(dp * d));
        }
    }

    /**
     * 设置状态栏图标颜色模式
     * @param light true = 深色图标(适合浅色背景), false = 浅色图标(适合深色背景)
     */
    public static void setLightMode(@NonNull Activity activity, boolean light) {
        WindowHelper.setLightStatusBar(activity, light);
    }

    /**
     * 隐藏系统状态栏
     */
    public static void hideSystemStatusBar(@NonNull Activity activity) {
        WindowHelper.hideSystemStatusBar(activity);
    }

    /**
     * 显示系统状态栏
     */
    public static void showSystemStatusBar(@NonNull Activity activity) {
        WindowHelper.showSystemStatusBar(activity);
    }

    /**
     * 隐藏所有系统栏（状态栏 + 导航栏）
     */
    public static void hideSystemBars(@NonNull Activity activity) {
        WindowHelper.hideSystemBars(activity);
    }

    /**
     * 显示所有系统栏
     */
    public static void showSystemBars(@NonNull Activity activity) {
        WindowHelper.showSystemBars(activity);
    }

    /**
     * 获取已安装的自定义状态栏View
     * @return StatusBarView 或 null（如果未安装）
     */
    @Nullable
    public static StatusBarView get(@NonNull Activity activity) {
        Window window = activity.getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        return decorView.findViewById(R.id.sdk_status_bar);
    }
}

