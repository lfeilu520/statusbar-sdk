package com.webuild.statusbar.ui.observers;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public abstract class StateObserver<T> {
    protected final Context mAppContext;
    private final List<T> mListeners = new ArrayList<>();
    private boolean mIsActive = false;

    public StateObserver(Context context) {
        this.mAppContext = context.getApplicationContext();
    }

    public void addObserver(T listener) {
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
                if (mListeners.size() == 1) {
                    onActive();
                    mIsActive = true;
                } else if (mIsActive) {
                    // If already active, maybe trigger an immediate update for the new listener
                    onNotify(listener);
                }
            }
        }
    }

    public void removeObserver(T listener) {
        synchronized (mListeners) {
            if (mListeners.remove(listener)) {
                if (mListeners.isEmpty()) {
                    onInactive();
                    mIsActive = false;
                }
            }
        }
    }

    protected abstract void onActive();

    protected abstract void onInactive();

    // Optional: method to notify a specific listener (e.g. initial state)
    protected void onNotify(T listener) {
    }

    protected void notifyObservers(Notifier<T> notifier) {
        List<T> snapshot;
        synchronized (mListeners) {
            snapshot = new ArrayList<>(mListeners);
        }
        for (T listener : snapshot) {
            if (listener != null) {
                notifier.notify(listener);
            }
        }
    }

    public interface Notifier<T> {
        void notify(T listener);
    }
}
