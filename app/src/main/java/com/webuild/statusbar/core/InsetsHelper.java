package com.webuild.statusbar.core;

import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class InsetsHelper {
    private InsetsHelper() {}

    public static int statusBarTopInset(View view) {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(view);
        if (insets == null) return 0;
        return insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
    }
}

