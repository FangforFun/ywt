package com.gkzxhn.gkprison.userport.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.event.ExamineEvent;
import com.gkzxhn.gkprison.userport.activity.SystemMessageActivity;
import com.gkzxhn.gkprison.userport.bean.SystemMessage;
import com.gkzxhn.gkprison.utils.tool.Log;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.greenrobot.event.EventBus;

/**
 * created by huangzhengneng on 2016/1/15
 * 自定义通知接收者
 */
public class CustomNotificationReceiver extends BroadcastReceiver {

    private SharedPreferences sp;

    public CustomNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = context.getPackageName() + NimIntent.ACTION_RECEIVE_CUSTOM_NOTIFICATION;
        if (action.equals(intent.getAction())) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("has_new_notification", true);
            editor.commit();
            // 从 intent 中取出自定义通知， intent 中只包含了一个 CustomNotification 对象
            CustomNotification notification = (CustomNotification)
                    intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
            // 第三方 APP 在此处理自定义通知：存储，处理，展示给用户等
            Log.i("收到通知啦....", "receive custom notification: " + notification.getContent()
                    + " from :" + notification.getSessionId() + "/" + notification.getSessionType());
            notification.getFromAccount();
            if(notification.getContent().contains("type_id")) {
                sendNotification(context, notification.getContent(), notification.getSessionId());
            }else if(notification.getContent().contains("审核")){
                doExamineResult(notification.getContent());
            }
            Log.i("接受者的通知", notification.getContent());
        }
    }

    /**
     * 操作审核结果
     */
    private void doExamineResult(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            String result = jsonObject.getString("result");
            if(!TextUtils.isEmpty(result) && result.equals("审核通过")){
                EventBus.getDefault().post(new ExamineEvent("审核通过"));
            }else {
                EventBus.getDefault().post(new ExamineEvent("审核未通过"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送通知
     * @param content
     * @param formId
     */
    public void sendNotification(Context context, String content, String formId){
        saveToDataBase(content);// 系统通知保存至数据库
        if(sp.getBoolean("isMsgRemind", false)) {
            setRemindAlarm(context, content);
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, SystemMessageActivity.class);
        Log.i("gongju通知", content);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("您有新的消息，点击查看")
                .setContentTitle("狱务通提醒")
                .setContentText("您有来自" + sp.getString("jail", "德山监狱") +"新的消息，点击查看")
                .setContentIntent(pendingIntent).setNumber(1).build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        manager.notify(1, notification);
//        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    /**
     * 设置闹钟
     */
    private void setRemindAlarm(Context context, String content) {
        String meeting_date = "";
        long alarm_time = 0;
        try {
            JSONObject jsonObject = new JSONObject(content);
            meeting_date = jsonObject.getString("meeting_date");
            String meeting_time = meeting_date.substring(0, meeting_date.lastIndexOf("-"));
            Log.i("meeting_time", meeting_time);
            String start_time = meeting_time.substring(0, meeting_time.indexOf(" ") + 9);
            Log.i("start_time", start_time);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long pre_alarm_time = format.parse(start_time).getTime();
            alarm_time = pre_alarm_time - 1800000;
            Log.i("alarm_time", alarm_time + "---" + StringUtils.formatTime(alarm_time, "yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String time = StringUtils.formatTime(System.currentTimeMillis(), "HH:mm:ss");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, alarm_time, sender);
    }

    /**
     * 保存至数据库
     * @param content
     */
    private void saveToDataBase(String content) {
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
        values.put("user_id", sp.getString("username", ""));
        String msg_reveice_time = StringUtils.formatTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
        values.put("receive_time", msg_reveice_time);
        db.insert("sysmsg", null, values);
        db.close();
    }
}
