package com.webuild.statusbar.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.webuild.statusbar.R;
import com.webuild.statusbar.ui.observers.BluetoothObserver;

/**
 * 蓝牙状态显示控件
 * 显示蓝牙开关和连接状态
 */
public class BluetoothView extends AppCompatImageView implements BluetoothObserver.Listener {

    public BluetoothView(Context context) {
        super(context);
        init();
    }

    public BluetoothView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BluetoothView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.ic_status_bluetooth);
        setAdjustViewBounds(true);
        setScaleType(ScaleType.FIT_CENTER);
        setMaxHeight(dpToPx(25));
        // Default to GONE until state is known to avoid "jumping"
        setVisibility(GONE);
    }

    private int dpToPx(int dp) {
        final float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onBluetoothStateChanged(int state) {
        post(() -> setState(state));
    }

    /**
     * 设置蓝牙状态
     * @param state -1:不可用/关闭(隐藏), 0:开启未连接, 2:已连接
     */
    private void setState(int state) {
        if (state < 0) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
            setSelected(state == 2);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BluetoothObserver.getInstance(getContext()).addObserver(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        BluetoothObserver.getInstance(getContext()).removeObserver(this);
        super.onDetachedFromWindow();
    }
}
