package com.gkzxhn.gkprison.application;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.userport.activity.SystemMessageActivity;
import com.gkzxhn.gkprison.userport.bean.SystemMessage;
import com.gkzxhn.gkprison.userport.receiver.AlarmReceiver;
import com.gkzxhn.gkprison.utils.CrashHandler;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.google.gson.Gson;
import com.keda.callback.MyMtcCallback;
import com.keda.sky.app.TruetouchGlobal;
import com.keda.vconf.reqs.ExamineEvent;
import com.kedacom.kdv.mt.api.Base;
import com.kedacom.kdv.mt.api.Configure;
import com.kedacom.kdv.mt.bean.TMtH323PxyCfg;
import com.kedacom.kdv.mt.bean.TagTNetUsedInfoApi;
import com.kedacom.kdv.mt.constant.EmMtModel;
import com.kedacom.kdv.mt.constant.EmNetAdapterWorkType;
import com.kedacom.truetouch.audio.AudioDeviceAndroid;
import com.netease.nim.uikit.BuildConfig;
import com.netease.nim.uikit.ImageLoaderKit;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.contact.ContactProvider;
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
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.pc.utils.FormatTransfer;
import com.pc.utils.NetWorkUtils;
import com.pc.utils.VConfStaticPic;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

/**
 * Created by zhengneng on 2015/12/23.
 */
public class MyApplication extends MultiDexApplication {

    private static final String TAG = "MyApplication";

    public final static String ID = "id";
    public final static String JID = "jid";
    public final static String NAME = "name";
    public final static String IPAddr = "ipAddr";
    public final static String ALIAS = "alias";
    public final static String E164NUM = "e164Num";
    public final static String USER_NAME = "username";
    public final static String RESULT = "result";

    public boolean isH323;

    public static MyApplication mOurApplication;

    public static Context getContext() {
        return mOurApplication.getApplicationContext();
    }

    public static MyApplication getApplication() {
        return (MyApplication) mOurApplication;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        new Runnable(){
            @Override
            public void run() {
                NIMClient.init(MyApplication.this, loginInfo(), options()); // 初始化
                // 初始化全局异常捕获
                CrashHandler crashHandler = CrashHandler.getInstance();
                crashHandler.init(getApplicationContext());

                if(!BuildConfig.BUILD_TYPE.equals("debug")){
                    Log.isDebug = true;
                }else {
                    Log.isDebug = false;
                }

                // 初始化UIKit模块
                initUIKit();
                NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(
                        new Observer<StatusCode>() {
                            public void onEvent(StatusCode status) {
                                Log.i("tag", "User status changed to: " + status);
                                switch (status) {
                                    case KICKOUT:
                                        toMain();
                                        break;
                                    case NET_BROKEN:
                                        ToastUtil.showShortToast(getApplicationContext(), getString(R.string.net_broken));
                                        break;
                                }
                            }
                        }, true);
                observeCustomNotification();
            }
        }.run();

        mOurApplication = this;
        android.util.Log.i(MyApplication.class.getSimpleName(), "onCreate..." + android.os.Build.MODEL + "  package:" + getPackageName());
        Base.mtStart(EmMtModel.emSkyAndroidPhone, TruetouchGlobal.MTINFO_SKYWALKER, "5.0", getMediaLibDir()
                + File.separator, MyMtcCallback.getInstance(), "kedacom"); // 启动业务终端，开始接受回调
        new Thread(new Runnable() {

            @Override
            public void run() {
                parseH323();
                // 设音视频上下文置
                AudioDeviceAndroid.initialize(getContext());
                setUserdNetInfo();
                // 启动Service
                Base.initService();
                android.util.Log.w("Test", "开始终端服务 SYSStartService: agent/misc/mtmp/rest/upgrade/im/mtpa");
                VConfStaticPic.checkStaticPic(MyApplication.getContext(), getTempDir() + File.separator);
            }
        }).start();
    }

    private void toMain() {
        Intent intent;
        if ((Boolean) SPUtil.get(getApplicationContext(), "isCommonUser", true)) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), DateMeetingListActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 监听系统通知
     */
    private void observeCustomNotification() {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(new Observer<CustomNotification>() {
            @Override
            public void onEvent(CustomNotification customNotification) {
                Log.i(TAG, "custom notification ApnsText : " + customNotification.getApnsText());
                Log.i(TAG, "custom notification Content : " + customNotification.getContent());
                Log.i(TAG, "custom notification FromAccount : " + customNotification.getFromAccount());
                Log.i(TAG, "custom notification SessionId : " + customNotification.getSessionId());
                Log.i(TAG, "custom notification Time : " + customNotification.getTime());
                Log.i(TAG, "custom notification SessionType : " + customNotification.getSessionType());
                Log.i(TAG, "custom notification PushPayload : " + customNotification.getPushPayload().size());
                Log.i(TAG, "custom notification enableUnreadCount : " + customNotification.getConfig().enableUnreadCount);
                Log.i(TAG, "custom notification enablePush : " + customNotification.getConfig().enablePush);
                Log.i(TAG, "custom notification enablePushNick : " + customNotification.getConfig().enablePushNick);

                // 第三方 APP 在此处理自定义通知：存储，处理，展示给用户等
                Log.i("收到通知啦....", "receive custom notification: " + customNotification.getContent()
                        + " from :" + customNotification.getSessionId() + "/" + customNotification.getSessionType());
                customNotification.getFromAccount();
                if(customNotification.getContent().contains("type_id")) {
                    SPUtil.put(MyApplication.this, "has_new_notification", true);
                    sendNotification(MyApplication.this, customNotification.getContent(), customNotification.getSessionId());
                }else if(customNotification.getContent().contains("审核")){
                    doExamineResult(customNotification.getContent());
                }
                Log.i("接受者的通知", customNotification.getContent());
            }
        }, true);
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
    public void sendNotification(Context context, String content, String formId){
        saveToDataBase(context, content);// 系统通知保存至数据库
        if((boolean)SPUtil.get(MyApplication.this, "isMsgRemind", false)) {
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
                .setContentText("您有来自" + SPUtil.get(MyApplication.this, "jail", "德山监狱") +"新的消息，点击查看")
                .setContentIntent(pendingIntent).setNumber(1).build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        manager.notify(1, notification);
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
    private void saveToDataBase(Context context, String content) {
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
        values.put("user_id", (String) SPUtil.get(MyApplication.this, "username", ""));
        String msg_reveice_time = StringUtils.formatTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
        values.put("receive_time", msg_reveice_time);
        db.insert("sysmsg", null, values);
        db.close();
    }

    private void initUIKit() {
        NimUIKit.init(this, infoProvider, contactProvider);
    }

    private UserInfoProvider infoProvider = new UserInfoProvider(){

        @Override
        public UserInfo getUserInfo(String s) {
            UserInfo user = NimUserInfoCache.getInstance().getUserInfo(s);
            if (user == null) {
                NimUserInfoCache.getInstance().getUserInfoFromRemote(s, null);
            }
            return user;
        }

        @Override
        public int getDefaultIconResId() {
            return R.drawable.avatar_def;
        }

        @Override
        public Bitmap getTeamIcon(String s) {
            Drawable drawable = getResources().getDrawable(R.drawable.nim_avatar_group);
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
            return null;
        }

        @Override
        public Bitmap getAvatarForMessageNotifier(String s) {
            UserInfo user = getUserInfo(s);
            return (user != null) ? ImageLoaderKit.getNotificationBitmapFromCache(user) : null;
        }

        @Override
        public String getDisplayNameForMessageNotifier(String account, String sessionId, SessionTypeEnum sessionTypeEnum) {
            String nick = null;
            if (sessionTypeEnum == SessionTypeEnum.P2P) {
                nick = NimUserInfoCache.getInstance().getAlias(account);
            } else if (sessionTypeEnum == SessionTypeEnum.Team) {
                nick = TeamDataCache.getInstance().getTeamNick(sessionId, account);
                if (TextUtils.isEmpty(nick)) {
                    nick = NimUserInfoCache.getInstance().getAlias(account);
                }
            }
            // 返回null，交给sdk处理。如果对方有设置nick，sdk会显示nick
            if (TextUtils.isEmpty(nick)) {
                return null;
            }

            return nick;
        }
    };

    private ContactProvider contactProvider = new ContactProvider() {
        @Override
        public List<UserInfoProvider.UserInfo> getUserInfoOfMyFriends() {
            List<NimUserInfo> nimUsers = NimUserInfoCache.getInstance().getAllUsersOfMyFriend();
            List<UserInfoProvider.UserInfo> users = new ArrayList<>(nimUsers.size());
            if (!nimUsers.isEmpty()) {
                users.addAll(nimUsers);
            }

            return users;
        }

        @Override
        public int getMyFriendsCount() {
            return FriendDataCache.getInstance().getMyFriendCounts();
        }

        @Override
        public String getUserDisplayName(String account) {
            return NimUserInfoCache.getInstance().getUserDisplayName(account);
        }
    };

    public boolean inMainProcess() {
        String packageName = getPackageName();
        String processName = SystemUtil.getProcessName(this);
        return packageName.equals(processName);
    }

    // 如果返回值为 null，则全部使用默认参数。
    private SDKOptions options() {
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
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小，该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        options.thumbnailSize = DensityUtil.getScreenWidthHeight(this)[0] / 2;

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

    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private LoginInfo loginInfo() {
        Log.i("application启动了", "hahahahah");
        if(getLoginInfo() != null) {
            Log.i("自动登录。。。", getLoginInfo().getAccount() + "---" + getLoginInfo().getToken());
        }
        return getLoginInfo();
    }

    private LoginInfo getLoginInfo() {
        // 从本地读取上次登录成功时保存的用户登录信息
        String token = (String) SPUtil.get(getApplicationContext(), "token", "");
        String password = (String) SPUtil.get(getApplicationContext(), "password", "");
        if((Boolean)SPUtil.get(getApplicationContext(), "isCommonUser", true)) {
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(token)) {
                return new LoginInfo(token, token);
            } else {
                return null;
            }
        }else {
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(password)) {
                return new LoginInfo(token, password);
            } else {
                return null;
            }
        }
    }

    public void parseH323() {
        // 从数据库获取当前 是否注册了代理
        StringBuffer H323PxyStringBuf = new StringBuffer();
        Configure.getH323PxyCfg(H323PxyStringBuf);
        String h323Pxy = H323PxyStringBuf.toString();
        TMtH323PxyCfg tmtH323Pxy = new Gson().fromJson(h323Pxy, TMtH323PxyCfg.class);
        // { "achNumber" : "", "achPassword" : "", "bEnable" : true, "dwSrvIp" : 1917977712, "dwSrvPort" : 2776 }
        if (null != tmtH323Pxy) {
            isH323 = tmtH323Pxy.bEnable;
//            isH323 = true;
        }
    }

    public String getMediaLibDir() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/mediaLib" + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    public String getTempDir() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/mediaLib/temp" + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    public static String getTmpDir() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/.tmp" + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.getAbsolutePath();
    }

    // 保存截图的路径(绝对路径)
    public static String getPictureDir() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/.picture" + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.getAbsolutePath();
    }

    // 图片保存文件夹绝对路径
    public static String getSaveDir() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "kedacom/sky_Demo/save" + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.getAbsolutePath();
    }

    /**
     * 设置正在使用的网络信息
     */
    public void setUserdNetInfo() {
        String ip = NetWorkUtils.getIpAddr(MyApplication.getContext(), true);

        TagTNetUsedInfoApi netInfo = new TagTNetUsedInfoApi();
        netInfo.emUsedType = EmNetAdapterWorkType.emNetAdapterWorkType_Wifi_Api;
        // netInfo.dwIp = NetWorkUtils.getFirstWiFiIpAddres(TruetouchApplication.getContext());
        try {
            netInfo.dwIp = FormatTransfer.lBytesToLong(InetAddress.getByName(ip).getAddress());
        } catch (Exception e) {
            netInfo.dwIp = FormatTransfer.reverseInt((int) NetWorkUtils.ip2int(ip));
        }
        if (NetWorkUtils.isMobile(MyApplication.getContext())) {
            netInfo.emUsedType = EmNetAdapterWorkType.emNetAdapterWorkType_MobileData_Api;
        }
        String dns = NetWorkUtils.getDns(MyApplication.getContext());
        try {
            if (!com.pc.utils.StringUtils.isNull(dns)) {
                netInfo.dwDns = FormatTransfer.lBytesToLong(InetAddress.getByName(dns).getAddress());
            } else {
                netInfo.dwDns = 0;
            }
        } catch (UnknownHostException e) {
            android.util.Log.e("Test", "dwDns: " + dns + "--" + netInfo.dwDns);
        }

        android.util.Log.e("Test", "ip: " + ip + "--" + netInfo.dwIp);

        Configure.sendUsedNetInfoNtf(netInfo);
    }
}
