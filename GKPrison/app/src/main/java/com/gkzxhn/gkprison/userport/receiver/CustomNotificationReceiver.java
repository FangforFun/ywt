package com.gkzxhn.gkprison.userport.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.activity.SystemMessageActivity;
import com.gkzxhn.gkprison.userport.bean.SystemMessage;
import com.gkzxhn.gkprison.utils.Utils;
import com.google.gson.Gson;
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
            notification.getFromAccount();
            sendNotification(context, notification.getContent(), notification.getSessionId());
            Log.i("接受者的通知", notification.getContent());
        }
    }

    /**
     * 发送通知
     * @param content
     * @param formId
     */
    public static void sendNotification(Context context, String content, String formId){
        saveToDataBase(content);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, SystemMessageActivity.class);
        Log.i("gongju通知", content);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("您有新的消息，点击查看")
                .setContentTitle(formId)
                .setContentText(content)
                .setContentIntent(pendingIntent).setNumber(1).build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        manager.notify(1, notification);
    }

    /**
     * 保存至数据库
     * @param content
     */
    private static void saveToDataBase(String content) {
        // 保存至数据库
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
        Gson gson = new Gson();
        SystemMessage systemMessage = gson.fromJson(content, SystemMessage.class);
        ContentValues values = new ContentValues();
        values.put("apply_date", systemMessage.getApply_date());
        values.put("type_id", systemMessage.getType_id());
        values.put("name", systemMessage.getName());
        values.put("is_read", systemMessage.is_read());
        values.put("result", systemMessage.getResult());
        values.put("meeting_date", systemMessage.getMeeting_date());
        values.put("reason", systemMessage.getReason());
        db.insert("sysmsg", null, values);
        db.close();
    }
}
