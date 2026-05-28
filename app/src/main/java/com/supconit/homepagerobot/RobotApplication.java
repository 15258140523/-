package com.supconit.homepagerobot;

import android.app.Application;
import android.util.Log;

import com.igexin.sdk.IUserLoggerInterface;
import com.igexin.sdk.PushManager;

public class RobotApplication extends Application {
    private static final String TAG = "HomepageRobot";

    @Override
    public void onCreate() {
        super.onCreate();
        initGetui();
    }

    private void initGetui() {
        Log.i(TAG, "initializing Getui SDK");
        PushManager.getInstance().preInit(this);
        PushManager.getInstance().initialize(this);

        if (BuildConfig.DEBUG) {
            PushManager.getInstance().setDebugLogger(this, new IUserLoggerInterface() {
                @Override
                public void log(String message) {
                    Log.d(TAG, "Getui: " + message);
                }
            });
        }
    }
}
