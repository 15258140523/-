package com.supconit.homepagerobot;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

final class NotificationHelper {
    private static final String TAG = "HomepageRobot";
    static final String CHANNEL_ID = "device_status";
    static final String KEEP_ALIVE_CHANNEL_ID = "keep_alive";

    private NotificationHelper() {
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel statusChannel = new NotificationChannel(
                CHANNEL_ID,
                "设备状态通知",
                NotificationManager.IMPORTANCE_HIGH
        );
        statusChannel.setDescription("设备上线、离线或服务端推送提醒");
        statusChannel.enableVibration(true);
        statusChannel.enableLights(true);
        statusChannel.setLightColor(Color.rgb(32, 200, 255));
        statusChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationChannel keepAliveChannel = new NotificationChannel(
                KEEP_ALIVE_CHANNEL_ID,
                "后台保活",
                NotificationManager.IMPORTANCE_LOW
        );
        keepAliveChannel.setDescription("保持机器人节点监控在后台运行");
        keepAliveChannel.setShowBadge(false);
        Uri sound = Settings.System.DEFAULT_NOTIFICATION_URI;
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        statusChannel.setSound(sound, attributes);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(statusChannel);
            manager.createNotificationChannel(keepAliveChannel);
        }
    }

    static Notification buildKeepAliveNotification(Context context) {
        Context appContext = context.getApplicationContext();
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 1001, intent, flags);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(appContext, KEEP_ALIVE_CHANNEL_ID)
                : new Notification.Builder(appContext);

        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("机器人节点监控运行中")
                .setContentText("正在保持后台连接，用于接收设备状态推送")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE);

        return builder.build();
    }

    static void showTransmissionNotification(Context context, String payload) {
        String title = "机器人节点监控";
        String body = payload == null || payload.isEmpty() ? "收到一条推送消息" : payload;
        String type = "getui";
        String key = String.valueOf(System.currentTimeMillis());

        try {
            JSONObject json = new JSONObject(payload);
            title = json.optString("title", title);
            body = json.optString("body", json.optString("message", body));
            type = json.optString("type", type);
            key = json.optString("key", key);
        } catch (Exception ignored) {
        }

        showNotification(context, title, body, type, key);
    }

    static void showNotification(Context context, String title, String body, String type, String key) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();
        ensureChannel(appContext);

        if (Build.VERSION.SDK_INT >= 33
                && appContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager manager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        Intent intent = new Intent(appContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        int requestCode = Math.abs((type + ":" + key + ":" + title + ":" + body).hashCode());
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, requestCode, intent, flags);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(appContext, CHANNEL_ID)
                : new Notification.Builder(appContext);

        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        int color = "offline".equals(type) ? Color.rgb(255, 105, 110) : Color.rgb(13, 114, 255);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(color);
        }

        try {
            manager.notify(requestCode, builder.build());
        } catch (Exception error) {
            Log.w(TAG, "show notification failed", error);
            return;
        }

    }
}
