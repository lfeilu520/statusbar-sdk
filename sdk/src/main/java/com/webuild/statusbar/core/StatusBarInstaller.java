package com.webuild.statusbar.core;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.webuild.statusbar.R;
import com.webuild.statusbar.config.StatusBarConfig;
import com.webuild.statusbar.ui.BatteryView;
import com.webuild.statusbar.ui.BluetoothView;
import com.webuild.statusbar.ui.NetworkView;
import com.webuild.statusbar.ui.StatusBarView;
import com.webuild.statusbar.ui.TimeView;

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
                    TextView title = view.findViewById(R.id.sdk_title);
                    NetworkView netView = view.findViewById(R.id.sdk_network);
                    if (title != null) {
                        boolean showTitle = config.showTitle && config.titleText != null && config.titleText.length() > 0;
                        title.setVisibility(showTitle ? View.VISIBLE : View.GONE);
                        if (showTitle) {
                            title.setText(config.titleText);
                        }
                        // Removed legacy RelativeLayout parameter adjustment logic which caused ClassCastException
                    }
                    View net = view.findViewById(R.id.sdk_network);
                    if (net instanceof NetworkView) {
                        ((NetworkView) net).setClickEnabled(config.networkClickable);
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
                if (net instanceof NetworkView) {
                    ((NetworkView) net).setClickEnabled(config.networkClickable);
                }
            }
            if (view.getChildCount() == 0) {
                if (config != null && !config.useXmlContent) {
                    setupDynamicContent(activity, view, config);
                } else {
                    LayoutInflater.from(activity).inflate(R.layout.sdk_status_bar_content, view, true);
                    if (config != null) {
                        android.widget.TextView title = view.findViewById(R.id.sdk_title);
                        NetworkView netView = view.findViewById(R.id.sdk_network);
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
                        if (net instanceof NetworkView) {
                            ((NetworkView) net).setClickEnabled(config.networkClickable);
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

        // 4. 确保内容不被状态栏遮挡
        final ViewGroup contentView = activity.findViewById(android.R.id.content);
        final View sbView = decorView.findViewById(R.id.sdk_status_bar);
        if (contentView != null && sbView != null) {
            // 4.1 拦截 WindowInsets，屏蔽系统状态栏的高度，防止子 View 重复处理
            // 注意：不要直接对 contentView 设置 Listener，因为这可能会覆盖系统或其他库（如 AppCompat）的默认行为
            // 导致 fitsSystemWindows 失效或触摸事件分发异常。
            // 更好的做法是包装一层 FrameLayout 或者只处理必要的 Insets
            
            ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, insets) -> {
                // 将状态栏高度设为 0，这样 fitsSystemWindows="true" 的子 View 就不会留出系统状态栏的空间
                // 而是完全由下面的 OnLayoutChangeListener 来控制 padding
                WindowInsetsCompat.Builder builder = new WindowInsetsCompat.Builder(insets);
                builder.setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, 0, 0, 0));
                
                // 继续分发 Insets，不消费它，以保证其他组件（如 NavigationBar）正常工作
                return ViewCompat.onApplyWindowInsets(v, builder.build());
            });

            // 4.2 根据自定义状态栏的实际高度，设置 contentView 的 padding
            sbView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    int height = bottom - top;
                    if (height > 0 && height != contentView.getPaddingTop()) {
                        contentView.setPadding(contentView.getPaddingLeft(), height,
                                contentView.getPaddingRight(), contentView.getPaddingBottom());
                    }
                }
            });
            // 立即触发一次更新（如果已有尺寸）
            if (sbView.getHeight() > 0) {
                int height = sbView.getHeight();
                if (height != contentView.getPaddingTop()) {
                    contentView.setPadding(contentView.getPaddingLeft(), height,
                            contentView.getPaddingRight(), contentView.getPaddingBottom());
                }
            }
        }
    }

    private static void setupDynamicContent(@NonNull Activity activity, @NonNull StatusBarView container, @Nullable StatusBarConfig config) {
        RelativeLayout root = new RelativeLayout(activity);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int margin8 = Math.round(8 * activity.getResources().getDisplayMetrics().density);
        int margin3 = Math.round(3 * activity.getResources().getDisplayMetrics().density);
        root.setPadding(margin8, 0, margin8, 0);
        container.addView(root);

        // 1. 左侧容器 (时间 + 标题)
        LinearLayout leftContainer = new LinearLayout(activity);
        leftContainer.setId(View.generateViewId());
        leftContainer.setOrientation(LinearLayout.HORIZONTAL);
        leftContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams leftLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        leftLp.addRule(RelativeLayout.CENTER_VERTICAL);
        root.addView(leftContainer, leftLp);

        if (config == null || config.showTime) {
            TimeView time = new TimeView(activity, null, 0);
            time.setId(R.id.sdk_time);
            leftContainer.addView(time);
        }

        if (config != null && config.showTitle && config.titleText != null && config.titleText.length() > 0) {
            TextView title = new TextView(activity);
            title.setId(R.id.sdk_title);
            title.setText(config.titleText);
            title.setTextColor(0xFF000000);
            title.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            title.setSingleLine(true);
            LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (config.showTime) {
                titleLp.setMarginStart(margin8);
            }
            leftContainer.addView(title, titleLp);
        }

        // 2. 右侧容器 (蓝牙 -> WiFi -> 电池)
        LinearLayout rightContainer = new LinearLayout(activity);
        rightContainer.setId(View.generateViewId());
        rightContainer.setOrientation(LinearLayout.HORIZONTAL);
        rightContainer.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.END);
        RelativeLayout.LayoutParams rightLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        rightLp.addRule(RelativeLayout.CENTER_VERTICAL);
        // 确保不遮挡左侧
        rightLp.addRule(RelativeLayout.END_OF, leftContainer.getId());
        root.addView(rightContainer, rightLp);

        if (config == null || config.showBluetooth) {
            BluetoothView bluetooth = new BluetoothView(activity);
            bluetooth.setId(R.id.sdk_bluetooth);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(margin3);
            rightContainer.addView(bluetooth, lp);
        }

        if (config == null || config.showNetwork) {
            NetworkView network = new NetworkView(activity);
            network.setId(R.id.sdk_network);
            network.setClickEnabled(config != null && config.networkClickable);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(margin8);
            rightContainer.addView(network, lp);
        }

        if (config == null || config.showBattery) {
            BatteryView battery = new BatteryView(activity, null, 0);
            battery.setId(R.id.sdk_battery);
            // 这里不需要 LinearLayout.LayoutParams，因为 addView 默认使用 WRAP_CONTENT
            rightContainer.addView(battery);
        }
    }

    private static int margin(@NonNull Activity activity, int dp) {
        return Math.round(dp * activity.getResources().getDisplayMetrics().density);
    }
}
