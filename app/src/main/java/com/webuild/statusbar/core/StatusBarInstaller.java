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
            if (config != null && !config.useXmlContent) {
                setupDynamicContent(activity, view, config);
            } else {
                LayoutInflater.from(activity).inflate(R.layout.sdk_status_bar_content, view, true);
                if (config != null) {
                    android.widget.TextView title = view.findViewById(R.id.sdk_title);
                    com.webuild.statusbar.ui.WbNetworkStateView netView = view.findViewById(R.id.sdk_network);
                    if (title != null) {
                        boolean showTitle = config.showTitle && config.titleText != null && config.titleText.length() > 0;
                        title.setVisibility(showTitle ? View.VISIBLE : View.GONE);
                        if (showTitle) {
                            title.setText(config.titleText);
                        }
                        if (netView != null) {
                            android.widget.RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) netView.getLayoutParams();
                            if (showTitle) {
                                lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START, 0);
                                lp.addRule(android.widget.RelativeLayout.END_OF, R.id.sdk_title);
                                lp.setMarginStart(margin(activity, 8));
                            } else {
                                lp.addRule(android.widget.RelativeLayout.END_OF, 0);
                                lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
                                lp.setMarginStart(0);
                            }
                            netView.setLayoutParams(lp);
                        }
                    }
                    View net = view.findViewById(R.id.sdk_network);
                    if (net instanceof com.webuild.statusbar.ui.WbNetworkStateView) {
                        ((com.webuild.statusbar.ui.WbNetworkStateView) net).setClickEnabled(config.networkClickable);
                    }
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
                if (config != null && !config.useXmlContent) {
                    setupDynamicContent(activity, view, config);
                } else {
                    LayoutInflater.from(activity).inflate(R.layout.sdk_status_bar_content, view, true);
                    if (config != null) {
                        android.widget.TextView title = view.findViewById(R.id.sdk_title);
                        com.webuild.statusbar.ui.WbNetworkStateView netView = view.findViewById(R.id.sdk_network);
                        if (title != null) {
                            boolean showTitle = config.showTitle && config.titleText != null && config.titleText.length() > 0;
                            title.setVisibility(showTitle ? View.VISIBLE : View.GONE);
                            if (showTitle) {
                                title.setText(config.titleText);
                            }
                            if (netView != null) {
                                android.widget.RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) netView.getLayoutParams();
                                if (showTitle) {
                                    lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START, 0);
                                    lp.addRule(android.widget.RelativeLayout.END_OF, R.id.sdk_title);
                                    lp.setMarginStart(margin(activity, 8));
                                } else {
                                    lp.addRule(android.widget.RelativeLayout.END_OF, 0);
                                    lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
                                    lp.setMarginStart(0);
                                }
                                netView.setLayoutParams(lp);
                            }
                        }
                        View net = view.findViewById(R.id.sdk_network);
                        if (net instanceof com.webuild.statusbar.ui.WbNetworkStateView) {
                            ((com.webuild.statusbar.ui.WbNetworkStateView) net).setClickEnabled(config.networkClickable);
                        }
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

    private static void setupDynamicContent(@NonNull Activity activity, @NonNull StatusBarView container, @Nullable StatusBarConfig config) {
        android.widget.RelativeLayout root = new android.widget.RelativeLayout(activity);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int margin8 = Math.round(8 * activity.getResources().getDisplayMetrics().density);
        root.setPadding(margin8, 0, margin8, 0);
        container.addView(root);
        android.widget.RelativeLayout.LayoutParams lp;
        int leftAnchorId = 0;

        if (config != null && config.showTitle && config.titleText != null && config.titleText.length() > 0) {
            android.widget.TextView title = new android.widget.TextView(activity);
            title.setId(R.id.sdk_title);
            title.setText(config.titleText);
            title.setTextColor(0xFF000000);
            title.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            title.setSingleLine(true);
            lp = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
            lp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
            lp.setMarginStart(0);
            root.addView(title, lp);
            leftAnchorId = R.id.sdk_title;
        }

        if (config == null || config.showNetwork) {
            com.webuild.statusbar.ui.WbNetworkStateView network = new com.webuild.statusbar.ui.WbNetworkStateView(activity);
            network.setId(R.id.sdk_network);
            lp = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (leftAnchorId == 0) {
                lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
            } else {
                lp.addRule(android.widget.RelativeLayout.END_OF, leftAnchorId);
                lp.setMarginStart(margin8);
            }
            lp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
            root.addView(network, lp);
            network.setClickEnabled(config != null && config.networkClickable);
            leftAnchorId = R.id.sdk_network;

            if (config == null || config.showBluetooth) {
                com.webuild.statusbar.ui.WbBluetoothStateView bluetooth = new com.webuild.statusbar.ui.WbBluetoothStateView(activity);
                bluetooth.setId(R.id.sdk_bluetooth);
                lp = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.addRule(android.widget.RelativeLayout.END_OF, leftAnchorId);
                lp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
                lp.setMarginStart(margin8);
                root.addView(bluetooth, lp);
                leftAnchorId = R.id.sdk_bluetooth;
            }
        } else if (config == null || config.showBluetooth) {
            com.webuild.statusbar.ui.WbBluetoothStateView bluetooth = new com.webuild.statusbar.ui.WbBluetoothStateView(activity);
            bluetooth.setId(R.id.sdk_bluetooth);
            lp = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (leftAnchorId == 0) {
                lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
            } else {
                lp.addRule(android.widget.RelativeLayout.END_OF, leftAnchorId);
                lp.setMarginStart(margin8);
            }
            lp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
            root.addView(bluetooth, lp);
            leftAnchorId = R.id.sdk_bluetooth;
        }

        if (config == null || config.showTime) {
            com.webuild.statusbar.ui.WbTimeView time = new com.webuild.statusbar.ui.WbTimeView(activity, null, 0);
            time.setId(R.id.sdk_time);
            lp = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL);
            root.addView(time, lp);
        }

        if (config == null || config.showBattery) {
            com.webuild.statusbar.ui.WbBatterStateView battery = new com.webuild.statusbar.ui.WbBatterStateView(activity, null, 0);
            battery.setId(R.id.sdk_battery);
            lp = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
            lp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
            root.addView(battery, lp);
        }
    }

    private static int margin(@NonNull Activity activity, int dp) {
        return Math.round(dp * activity.getResources().getDisplayMetrics().density);
    }
}
