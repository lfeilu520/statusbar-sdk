package com.webuild.statusbar.ui.base;

import android.content.Context;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BaseWatcher<T> {
    protected final Context mContext;
    protected final List<T> mListView = new CopyOnWriteArrayList<>();

    public BaseWatcher(Context context) {
        this.mContext = context.getApplicationContext();
    }

    protected boolean watch(T t) {
        return this.mListView.add(t);
    }

    protected boolean unwatch(T t) {
        return this.mListView.remove(t);
    }
}
