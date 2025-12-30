package com.webuild.statusbar.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.webuild.statusbar.R;

public class WbStatusBar extends RelativeLayout {
    protected View tv_time;
    protected TextView tv_title;

    public WbStatusBar(Context context) {
        this(context, null);
    }

    public WbStatusBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.wb_statusBarStyle);
    }

    public WbStatusBar(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, R.style.StatusBarStyle);
    }

    public WbStatusBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 按照原始实现：在onFinishInflate中加载内容布局
        LayoutInflater.from(getContext()).inflate(R.layout.wb_status_bar_content, (ViewGroup) this, true);
        this.tv_title = (TextView) findViewById(R.id.tv_title);
        this.tv_time = findViewById(R.id.tv_time);
    }

    public void setTitle(CharSequence charSequence) {
        TextView textView = this.tv_title;
        if (textView != null) {
            textView.setText(charSequence);
        }
    }

    public void setTitle(int i) {
        TextView textView = this.tv_title;
        if (textView != null) {
            textView.setText(i);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 按照原始实现：处理标题和时间重叠
        TextView textView = this.tv_title;
        if (textView == null || this.tv_time == null || textView.getRight() <= this.tv_time.getLeft()) {
            return;
        }
        View view = this.tv_time;
        view.layout(view.getLeft(), getMeasuredHeight(), this.tv_time.getRight(), this.tv_time.getBottom());
    }
}
