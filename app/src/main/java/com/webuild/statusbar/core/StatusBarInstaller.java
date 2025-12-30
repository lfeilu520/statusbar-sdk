package com.webuild.statusbar.core;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.webuild.statusbar.R;
import com.webuild.statusbar.config.StatusBarConfig;
import com.webuild.statusbar.ui.StatusBarView;

/**
 * 状态栏安装器 - 将自定义状态栏View添加到DecorView
 */
public final class StatusBarInstaller {
    private StatusBarInstaller() {}

    /**
     * 安装自定义状态栏
     */
    public static void install(@NonNull Activity activity, @Nullable StatusBarConfig config) {
        // 1. 立即准备窗口，隐藏系统状态栏
        WindowHelper.prepareForCustomStatusBar(activity);
        
        Window window = activity.getWindow();
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
                if (config.fixedHeightPx > 0) {
                    view.setFixedHeightPx(config.fixedHeightPx);
                }
                view.setUseSystemInsets(config.useSystemInsets);
            }
            decorView.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LayoutInflater.from(activity).inflate(R.layout.sdk_status_bar_content, view, true);
            if (config != null) {
                View net = view.findViewById(R.id.sdk_network);
                if (net instanceof com.webuild.statusbar.ui.WbNetworkStateView) {
                    ((com.webuild.statusbar.ui.WbNetworkStateView) net).setClickEnabled(config.networkClickable);
                }
            }
            view.bringToFront();
        } else if (existing instanceof StatusBarView) {
            StatusBarView view = (StatusBarView) existing;
            if (config != null) {
                if (config.background != null) {
                    view.setBackground(config.background);
                }
                view.setInterceptTouch(config.interceptTouch);
                if (config.fixedHeightPx > 0) {
                    view.setFixedHeightPx(config.fixedHeightPx);
                }
                view.setUseSystemInsets(config.useSystemInsets);
                View net = view.findViewById(R.id.sdk_network);
                if (net instanceof com.webuild.statusbar.ui.WbNetworkStateView) {
                    ((com.webuild.statusbar.ui.WbNetworkStateView) net).setClickEnabled(config.networkClickable);
                }
            }
            if (view.getChildCount() == 0) {
                LayoutInflater.from(activity).inflate(R.layout.sdk_status_bar_content, view, true);
                if (config != null) {
                    View net = view.findViewById(R.id.sdk_network);
                    if (net instanceof com.webuild.statusbar.ui.WbNetworkStateView) {
                        ((com.webuild.statusbar.ui.WbNetworkStateView) net).setClickEnabled(config.networkClickable);
                    }
                }
            }
            view.bringToFront();
        }
        // 2. 设置状态栏图标颜色模式
        if (config != null) {
            WindowHelper.setLightStatusBar(activity, config.lightIcons);
        }
        
        // 3. 确保系统状态栏隐藏
        WindowHelper.hideSystemStatusBar(activity);
    }
}

