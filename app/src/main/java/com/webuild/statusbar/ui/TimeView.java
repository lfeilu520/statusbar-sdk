package com.webuild.statusbar.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.appcompat.widget.AppCompatTextView;
import com.webuild.statusbar.R;
import com.webuild.statusbar.ui.observers.TimeObserver;

public class TimeView extends AppCompatTextView implements TimeObserver.Listener {
    private int mFormatResId;

    public TimeView(Context context) {
        super(context);
        this.mFormatResId = R.string.wb_time_format_24;
    }

    public TimeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFormatResId = R.string.wb_time_format_24;
    }

    public TimeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFormatResId = R.string.wb_time_format_24;
        setTextColor(0xFF000000);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        setTypeface(getTypeface(), Typeface.BOLD);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateTime();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TimeObserver.getInstance(getContext()).addObserver(this);
        updateFormat(DateFormat.is24HourFormat(getContext()));
        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        TimeObserver.getInstance(getContext()).removeObserver(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onTimeTick() {
        post(this::updateTime);
    }

    @Override
    public void onTimeFormatChanged(boolean is24Hour) {
        post(() -> {
            updateFormat(is24Hour);
            updateTime();
        });
    }

    private void updateFormat(boolean is24Hour) {
        this.mFormatResId = is24Hour ? R.string.wb_time_format_24 : R.string.wb_time_format_12;
    }

    private void updateTime() {
        setText(DateFormat.format(getContext().getText(this.mFormatResId), System.currentTimeMillis()));
    }
}
