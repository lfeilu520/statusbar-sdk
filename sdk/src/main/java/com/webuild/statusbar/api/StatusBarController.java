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

public final class StatusBarController {
    private StatusBarController() {}

    public static void prepare(@NonNull Activity activity) {
        WindowHelper.prepareForCustomStatusBar(activity);
    }

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

    public static void setLightMode(@NonNull Activity activity, boolean light) {
        WindowHelper.setLightStatusBar(activity, light);
    }

    public static void hideSystemStatusBar(@NonNull Activity activity) {
        WindowHelper.hideSystemStatusBar(activity);
    }

    public static void showSystemStatusBar(@NonNull Activity activity) {
        WindowHelper.showSystemStatusBar(activity);
    }

    public static void hideSystemBars(@NonNull Activity activity) {
        WindowHelper.hideSystemBars(activity);
    }

    public static void showSystemBars(@NonNull Activity activity) {
        WindowHelper.showSystemBars(activity);
    }

    public static void setTitleVisible(@NonNull Activity activity, boolean visible) {
        StatusBarView v = get(activity);
        if (v == null) return;
        android.widget.TextView title = v.findViewById(R.id.sdk_title);
        com.webuild.statusbar.ui.WbNetworkStateView net = v.findViewById(R.id.sdk_network);
        if (title == null && visible) {
            android.widget.RelativeLayout root = findRootRelative(v);
            if (root != null) {
                title = new android.widget.TextView(activity);
                title.setId(R.id.sdk_title);
                title.setTextColor(0xFF000000);
                title.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
                title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
                title.setSingleLine(true);
                android.widget.RelativeLayout.LayoutParams lp = new android.widget.RelativeLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                );
                lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
                lp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
                lp.setMarginStart(0);
                root.addView(title, lp);
            }
        }
        if (title != null) {
            title.setVisibility(visible ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (net != null) {
            android.widget.RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) net.getLayoutParams();
            if (visible) {
                lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START, 0);
                lp.addRule(android.widget.RelativeLayout.END_OF, R.id.sdk_title);
                lp.setMarginStart(dp(activity, 8));
            } else {
                lp.addRule(android.widget.RelativeLayout.END_OF, 0);
                lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
                lp.setMarginStart(0);
            }
            net.setLayoutParams(lp);
        }
    }

    public static void setTitleText(@NonNull Activity activity, @NonNull CharSequence text) {
        StatusBarView v = get(activity);
        if (v == null) return;
        android.widget.TextView title = v.findViewById(R.id.sdk_title);
        if (title == null) {
            setTitleVisible(activity, true);
            title = v.findViewById(R.id.sdk_title);
        }
        if (title != null) {
            title.setText(text);
            title.setVisibility(android.view.View.VISIBLE);
        }
    }

    @Nullable
    public static StatusBarView get(@NonNull Activity activity) {
        Window window = activity.getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        return decorView.findViewById(R.id.sdk_status_bar);
    }

    private static int dp(@NonNull Activity activity, int dp) {
        return Math.round(dp * activity.getResources().getDisplayMetrics().density);
    }

    @Nullable
    private static android.widget.RelativeLayout findRootRelative(@NonNull StatusBarView v) {
        for (int i = 0; i < v.getChildCount(); i++) {
            android.view.View child = v.getChildAt(i);
            if (child instanceof android.widget.RelativeLayout) {
                return (android.widget.RelativeLayout) child;
            }
        }
        return null;
    }
}
