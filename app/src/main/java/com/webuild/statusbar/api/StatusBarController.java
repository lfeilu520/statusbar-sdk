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

    /**
     * 准备窗口以使用自定义状态栏
     * 应该在 Activity.onCreate() 中 super.onCreate() 之后、setContentView() 之前调用
     * 这样可以确保系统状态栏从一开始就不显示
     */
    public static void prepare(Activity activity) {
        WindowHelper.prepareForCustomStatusBar(activity);
    }

    /**
     * 安装自定义状态栏
     * 可以在 setContentView() 之前或之后调用
     */
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

