package com.gkzxhn.gkprison.userport.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

public class CustomNotificationReceiver extends BroadcastReceiver {

    public CustomNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = context.getPackageName() + NimIntent.ACTION_RECEIVE_CUSTOM_NOTIFICATION;
        if (action.equals(intent.getAction())) {
            // 从 intent 中取出自定义通知， intent 中只包含了一个 CustomNotification 对象
            CustomNotification notification = (CustomNotification)
                    intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
            // 第三方 APP 在此处理自定义通知：存储，处理，展示给用户等
            Log.i("收到通知啦....", "receive custom notification: " + notification.getContent()
                    + " from :" + notification.getSessionId() + "/" + notification.getSessionType());
        }
    }
}
