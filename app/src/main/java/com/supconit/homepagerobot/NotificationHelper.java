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
import android.os.Build;

import org.json.JSONObject;

final class NotificationHelper {
    static final String CHANNEL_ID = "device_status";

    private NotificationHelper() {
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "设备状态通知",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("设备上线、离线或服务端推送提醒");
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
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

        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, intent, flags);
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
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        int color = "offline".equals(type) ? Color.rgb(255, 105, 110) : Color.rgb(13, 114, 255);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(color);
        }

        String notificationKey = type + ":" + key + ":" + title + ":" + body;
        manager.notify(Math.abs(notificationKey.hashCode()), builder.build());
    }
}
