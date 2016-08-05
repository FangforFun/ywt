package com.gkzxhn.gkprison.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.AVChatActivity;
import com.gkzxhn.gkprison.avchat.AVChatProfile;
import com.gkzxhn.gkprison.avchat.DemoCache;
import com.gkzxhn.gkprison.prisonport.activity.DateMeetingListActivity;
import com.gkzxhn.gkprison.userport.activity.MainActivity;
import com.gkzxhn.gkprison.utils.CrashHandler;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.SystemUtil;
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
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatRingerConfig;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengneng on 2015/12/23.
 */
public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Runnable(){
            @Override
            public void run() {
                DemoCache.setContext(getApplicationContext());
                NIMClient.init(MyApplication.this, loginInfo(), options()); // 初始化
                // 初始化全局异常捕获
                CrashHandler crashHandler = CrashHandler.getInstance();
                crashHandler.init(getApplicationContext());

                if (inMainProcess()) {
                    // 初始化UIKit模块
                    initUIKit();
                    // 注册网络通话来电
                    enableAVChat();
                    NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(
                            new Observer<StatusCode>() {
                                public void onEvent(StatusCode status) {
                                    Log.i("tag", "User status changed to: " + status);
                                    switch (status) {
                                        case KICKOUT:
                                            Intent intent;
                                            if ((Boolean)SPUtil.get(getApplicationContext(), "isCommonUser", true)) {
                                                intent = new Intent(getApplicationContext(), MainActivity.class);
                                            } else {
                                                intent = new Intent(getApplicationContext(), DateMeetingListActivity.class);
                                            }
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            break;
                                        case NET_BROKEN:
                                            Toast.makeText(getApplicationContext(), "网络连接已断开，请检查网络", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            }, true);
                    NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(new Observer<CustomNotification>() {
                        @Override
                        public void onEvent(CustomNotification message) {
                            // 在这里处理自定义通知。
                        }
                    }, true);
                }
            }
        }.run();
    }

    /**
     * 音视频通话配置与监听
     */
    private void enableAVChat() {
        setupAVChat();
        registerAVChatIncomingCallObserver(true);
    }

    private void setupAVChat() {
        AVChatRingerConfig config = new AVChatRingerConfig();
        config.res_connecting = R.raw.avchat_connecting;
        config.res_no_response = R.raw.avchat_no_response;
        config.res_peer_busy = R.raw.avchat_peer_busy;
        config.res_peer_reject = R.raw.avchat_peer_reject;
        config.res_ring = R.raw.avchat_ring;
        AVChatManager.getInstance().setRingerConfig(config); // 设置铃声配置
    }

    private void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData data) {
                // 有网络来电打开AVChatActivity
                AVChatProfile.getInstance().setAVChatting(true);
                AVChatActivity.launch(DemoCache.getContext(), data, AVChatActivity.FROM_BROADCASTRECEIVER);
                Log.i("----------------", data.getAccount() + "---" + data.toString());
            }
        }, register);
    }

    private void initUIKit() {
        // 初始化，需要传入用户信息提供者
        NimUIKit.init(this, infoProvider, contactProvider);

        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
//        NimUIKit.setLocationProvider(new NimDemoLocationProvider());

        // 会话窗口的定制初始化。
//        SessionHelper.init();

        // 通讯录列表定制初始化
//        ContactHelper.init();
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
                DemoCache.setAccount(token.toLowerCase());
                return new LoginInfo(token, token);
            } else {
                return null;
            }
        }else {
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(password)) {
                DemoCache.setAccount(token.toLowerCase());
                return new LoginInfo(token, password);
            } else {
                return null;
            }
        }
    }
}
