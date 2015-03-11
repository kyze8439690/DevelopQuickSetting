package me.yugy.github.developquicksetting;

import com.crashlytics.android.Crashlytics;

public class Application extends android.app.Application {

    private static Application sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Crashlytics.start(this);
        sInstance = this;
    }

    public synchronized static Application getInstance() {
        return sInstance;
    }
}
