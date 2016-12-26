package com.gkzxhn.gkprison.app.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.app.MyApplication;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.activity.SystemMessageActivity;
import com.gkzxhn.gkprison.userport.bean.SystemMessage;
import com.gkzxhn.gkprison.userport.receiver.AlarmReceiver;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.google.gson.Gson;
import com.keda.vconf.reqs.ExamineEvent;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.greenrobot.event.EventBus;

/**
 * Author: Huang ZN
 * Date: 2016/12/20
 * Email:943852572@qq.com
 * Description:云信sdk相关
 *              sdk初始化、
 *              UI初始化、
 *              监听云信系统通知及后续操作
 */

public class NimInitUtil {

    private static final String TAG = NimInitUtil.class.getName();

    /**
     * 初始化云信sdk相关
     */
    public static void initNim(){
        NIMClient.init(MyApplication.getContext(), loginInfo(), options()); // 初始化
        if (inMainProcess()) {
            observeCustomNotification();
            NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(
                    getObserver(), true);
        }
    }

    /**
     * 观察者
     * @return
     */
    @NonNull
    private static Observer<StatusCode> getObserver() {
        return new Observer<StatusCode>() {
            public void onEvent(StatusCode status) {
                Log.i("tag", "User status changed to: " + status);
                switch (status) {
                    case KICKOUT:
                        toMain();
                        break;
                    case NET_BROKEN:
                        ToastUtil.showShortToast(MyApplication.getContext()
                                .getString(R.string.net_broken));
                        break;
                }
            }
        };
    }

    /**
     * 被踢下线进入主页
     */
    private static void toMain() {
        boolean isCommoner = (boolean) SPUtil.get(MyApplication.getContext(),
                "isCommonUser", true);
        Intent intent = new Intent(MyApplication.getContext(), isCommoner ?
                MainActivity.class : DateMeetingListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getContext().startActivity(intent);
    }

    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private static LoginInfo loginInfo() {
        return getLoginInfo();
    }

    /**
     * // 从本地读取上次登录成功时保存的用户登录信息
     * @return
     */
    private static LoginInfo getLoginInfo() {
        String token = (String) SPUtil.get(MyApplication.getContext(), "token", "");
        String password = (String) SPUtil.get(MyApplication.getContext(), "password", "");
        boolean isCommonUser = (boolean)SPUtil.get(MyApplication.getContext(), "isCommonUser", true);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(token)) {
            return new LoginInfo(token, isCommonUser ? token : password);
        } else {
            return null;
        }
    }

    /**
     * 主进程
     * @return
     */
    private static boolean inMainProcess() {
        String packageName = MyApplication.getContext().getPackageName();
        String processName = SystemUtil.getProcessName(MyApplication.getContext());
        return packageName.equals(processName);
    }

    // 如果返回值为 null，则全部使用默认参数。
    private static SDKOptions options() {
        SDKOptions options = new SDKOptions();

        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        config.notificationEntrance = MainActivity.class; // 点击通知栏跳转到该Activity
        config.notificationSmallIconId = R.mipmap.ic_launcher;
        options.statusBarNotificationConfig = config;

        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用下面代码示例中的位置作为 SDK 的数据目录。
        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        options.sdkStorageRootPath = Environment.getExternalStorageDirectory() + "/" + MyApplication.getContext().getPackageName() + "/nim";

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小，该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        options.thumbnailSize = DensityUtil.getScreenWidthHeight(MyApplication.getContext())[0] / 2;

        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = new UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return null;
            }

            @Override
            public int getDefaultIconResId() {
                return R.drawable.avatar_def;
            }

            @Override
            public Bitmap getTeamIcon(String tid) {
                return null;
            }

            @Override
            public Bitmap getAvatarForMessageNotifier(String account) {
                return null;
            }

            @Override
            public String getDisplayNameForMessageNotifier(String account, String sessionId,
                                                           SessionTypeEnum sessionType) {
                return null;
            }
        };
        return options;
    }


    /**
     * 监听系统通知
     */
    private static void observeCustomNotification() {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(new Observer<CustomNotification>() {
            @Override
            public void onEvent(CustomNotification customNotification) {
                Log.i(TAG, "custom notification ApnsText : " + customNotification.getApnsText());
                Log.i(TAG, "custom notification Content : " + customNotification.getContent());
                Log.i(TAG, "custom notification FromAccount : " + customNotification.getFromAccount());
                Log.i(TAG, "custom notification SessionId : " + customNotification.getSessionId());
                Log.i(TAG, "custom notification Time : " + customNotification.getTime());
                Log.i(TAG, "custom notification SessionType : " + customNotification.getSessionType());
                // 第三方 APP 在此处理自定义通知：存储，处理，展示给用户等
                Log.i(TAG, "receive custom notification: " + customNotification.getContent()
                        + " from :" + customNotification.getSessionId() + "/" + customNotification.getSessionType());
                if(customNotification.getContent().contains("type_id")) {
                    SPUtil.put(MyApplication.getContext(), "has_new_notification", true);
                    sendNotification(MyApplication.getContext(), customNotification.getContent(),
                            customNotification.getSessionId());
                }else if(customNotification.getContent().contains("审核")){
                    doExamineResult(customNotification.getContent());
                }else {
                    Log.e(TAG, "sorry,other type notification");
                }
            }
        }, true);
    }

    /**
     * 操作审核结果
     */
    private static void doExamineResult(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            String result = jsonObject.getString("result");
            if(!TextUtils.isEmpty(result) && result.equals("审核通过")){
                EventBus.getDefault().post(new ExamineEvent("审核通过"));
            }else {
                EventBus.getDefault().post(new ExamineEvent("审核不通过"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ExamineEvent("审核不通过"));
        }
    }

    /**
     * 发送通知
     * @param content
     * @param formId
     */
    public static void sendNotification(Context context, String content, String formId){
        saveToDataBase(context, content);// 系统通知保存至数据库
        if((boolean)SPUtil.get(MyApplication.getContext(), "isMsgRemind", false)) {
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
                .setContentText("您有来自" + SPUtil.get(MyApplication.getContext(), "jail", "德山监狱") +"新的消息，点击查看")
                .setContentIntent(pendingIntent).setNumber(1).build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        manager.notify(1, notification);
    }

    /**
     * 设置闹钟
     * @param context
     * @param content
     */
    private static void setRemindAlarm(Context context, String content) {
        String meeting_date = "";
        long alarm_time = 0;
        try {
            JSONObject jsonObject = new JSONObject(content);
            meeting_date = jsonObject.getString("meeting_date");
            String meeting_time = meeting_date.substring(0, meeting_date.lastIndexOf("-"));
            Log.i("meeting_time", meeting_time);
            String start_time = meeting_time.substring(0, meeting_time.indexOf(" ") + 9);
            Log.i("start_time", start_time);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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
    private static void saveToDataBase(Context context, String content) {
        // 保存至数据库
        SQLiteDatabase db = StringUtils.getSQLiteDB(context);
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
        values.put("user_id", (String) SPUtil.get(MyApplication.getContext(), "username", ""));
        String msg_reveice_time = StringUtils.formatTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
        values.put("receive_time", msg_reveice_time);
        db.insert("sysmsg", null, values);
        db.close();
    }
}
