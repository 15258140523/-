package com.supconit.homepagerobot;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class KeepAliveService extends Service {
    private static final String TAG = "HomepageRobot";
    private static final int NOTIFICATION_ID = 20260529;

    static void start(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context.getApplicationContext(), KeepAliveService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getApplicationContext().startForegroundService(intent);
            } else {
                context.getApplicationContext().startService(intent);
            }
        } catch (Exception error) {
            Log.w(TAG, "start keep-alive service failed", error);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.ensureChannel(this);
        startForeground(NOTIFICATION_ID, buildNotification());
        Log.i(TAG, "keep-alive service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification());
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "keep-alive service destroyed");
    }

    private Notification buildNotification() {
        return NotificationHelper.buildKeepAliveNotification(this);
    }
}
