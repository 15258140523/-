package com.supconit.homepagerobot;

import android.content.Context;
import android.util.Log;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;

public class GetuiIntentService extends GTIntentService {
    private static final String TAG = "HomepageRobot";

    @Override
    public void onReceiveServicePid(Context context, int pid) {
        Log.d(TAG, "Getui service pid = " + pid);
    }

    @Override
    public void onReceiveClientId(Context context, String clientId) {
        Log.i(TAG, "Getui CID = " + clientId);
        MainActivity.saveGetuiClientId(context, clientId);
        MainActivity.onGetuiClientId(clientId);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage message) {
        String taskId = message.getTaskId();
        String messageId = message.getMessageId();
        byte[] payload = message.getPayload();

        boolean feedback = PushManager.getInstance().sendFeedbackMessage(context, taskId, messageId, 90001);
        Log.d(TAG, "Getui feedback = " + feedback + ", taskId = " + taskId + ", messageId = " + messageId);

        if (payload == null) {
            Log.w(TAG, "Getui transmission payload is null");
            return;
        }

        String data = new String(payload);
        Log.i(TAG, "Getui transmission = " + data);
        NotificationHelper.showTransmissionNotification(context, data);
        MainActivity.onGetuiTransmission(data);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
        Log.i(TAG, "Getui online state = " + online);
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage message) {
        Log.d(TAG, "Getui command result = " + message);
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage message) {
        Log.i(TAG, "Getui notification arrived, cid = " + message.getClientId()
                + ", title = " + message.getTitle()
                + ", content = " + message.getContent());
        NotificationHelper.showNotification(
                context,
                message.getTitle(),
                message.getContent(),
                "getui-notification",
                message.getTaskId()
        );
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage message) {
        Log.i(TAG, "Getui notification clicked, cid = " + message.getClientId()
                + ", title = " + message.getTitle()
                + ", content = " + message.getContent());
    }
}
