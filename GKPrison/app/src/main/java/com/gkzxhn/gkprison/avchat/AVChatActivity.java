package com.gkzxhn.gkprison.avchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.event.ExamineEvent;
import com.gkzxhn.gkprison.userport.event.MeetingTimeEvent;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.megvii.licensemanager.Manager;
import com.megvii.livenessdetection.LivenessLicenseManager;
import com.megvii.livenesslib.LivenessActivity2;
import com.megvii.livenesslib.util.ConUtil;
import com.netease.nim.uikit.common.activity.TActivity;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatOnlineAckEvent;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * 音视频界面
 * Created by huangzhengneng on 2016/1/5.
 */
public class AVChatActivity extends TActivity implements AVChatUI.AVChatListener, AVChatStateObserver {
    // constant
    private static final String TAG = "AVChatActivity";
    private static final String KEY_IN_CALLING = "KEY_IN_CALLING";
    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    private static final String KEY_CALL_TYPE = "KEY_CALL_TYPE";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_CALL_CONFIG = "KEY_CALL_CONFIG";
    public static final String INTENT_ACTION_AVCHAT = "INTENT_ACTION_AVCHAT";

    /**
     * 来自广播
     */
    public static final int FROM_BROADCASTRECEIVER = 0;
    /**
     * 来自发起方
     */
    public static final int FROM_INTERNAL = 1;
    /**
     * 来自通知栏
     */
    public static final int FROM_NOTIFICATION = 2;
    /**
     * 未知的入口
     */
    public static final int FROM_UNKNOWN = -1;

    // data
    private AVChatUI avChatUI; // 音视频总管理器
    private AVChatData avChatData; // config for connect video server
    private int state; // calltype 音频或视频
    private String receiverId; // 对方的account

    // state
    private boolean isUserFinish = false;
    private boolean mIsInComingCall = false;// is incoming call or outgoing call
    private boolean isCallEstablished = false; // 电话是否接通
    private static boolean needFinish = true; // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
    private boolean hasOnpause = false; // 是否暂停音视频

    // notification
    private AVChatNotification notifier;
    private SharedPreferences sp;

    public static void start(Context context, String account, int callType, int source) {
        Log.i("AVChatActivity ---> ", account + "==");
        needFinish = false;
        Intent intent = new Intent();
        intent.setClass(context, AVChatActivity.class);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_IN_CALLING, false);
        intent.putExtra(KEY_CALL_TYPE, callType);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    /**
     * incoming call
     * 接收方收到视频页面
     * @param context
     */
    public static void launch(Context context, AVChatData config, int source) {
        Log.i("AVChatActivity ---> ", config.getAccount() + "==");
        needFinish = false;
        Intent intent = new Intent();
        intent.setClass(context, AVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CALL_CONFIG, config);
        intent.putExtra(KEY_IN_CALLING, true);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (needFinish || !checkSource()) {
            finish();
            return;
        }
        sp = getSharedPreferences("config", MODE_PRIVATE);
        View root = View.inflate(this, R.layout.avchat_activity, null);
        setContentView(root);
        mIsInComingCall = getIntent().getBooleanExtra(KEY_IN_CALLING, false);
        avChatUI = new AVChatUI(this, root, this);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!avChatUI.initiation()) {
            this.finish();
            return;
        }
        EventBus.getDefault().register(this);
        if (mIsInComingCall) {
            inComingCalling();
        } else {
            outgoingCalling();
        }
        registerNetCallObserver(true);

        notifier = new AVChatNotification(this);
        notifier.init(receiverId != null ? receiverId : avChatData.getAccount());
        isCallEstablished = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        AVChatManager.getInstance().pauseVideo(); // 暂停视频聊天（用于在视频聊天过程中，APP退到后台时必须调用）
        hasOnpause = true;
    }

    public void onEvent(final ExamineEvent examineEvent){
        avChatUI.setExamine(examineEvent.getMsg());
        Handler handler = new Handler(getMainLooper());
        if(examineEvent.getMsg().contains("发送审核状态异常")){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AVChatActivity.this, "服务器异常", Toast.LENGTH_LONG).show();
                }
            });
        }else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AVChatActivity.this, examineEvent.getMsg(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        activeCallingNotifier();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelCallingNotifier();
        if (hasOnpause) {
            avChatUI.resumeVideo();
            hasOnpause = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AVChatProfile.getInstance().setAVChatting(false);
        registerNetCallObserver(false);
        cancelCallingNotifier();
        needFinish = true;
        if((boolean)SPUtil.get(AVChatActivity.this, "isCommonUser", true)){
            // 如果是普通用户  视频结束恢复未审查状态
            SPUtil.put(AVChatActivity.this, "is_can_video", false);
            SPUtil.put(AVChatActivity.this, "last_meeting_time",
                    StringUtils.formatTime(System.currentTimeMillis(), "yyyy-MM-dd"));
            //通知RemoteMeetingPager修改上次会见时间文本
            EventBus.getDefault().post(new MeetingTimeEvent());
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
    }

    /**
     * 判断来电还是去电
     *
     * @return
     */
    private boolean checkSource() {
        switch (getIntent().getIntExtra(KEY_SOURCE, FROM_UNKNOWN)) {
            case FROM_BROADCASTRECEIVER: // incoming call
                parseIncomingIntent();
                return true;
            case FROM_INTERNAL: // outgoing call
                parseOutgoingIntent();
                if (state == AVChatType.VIDEO.getValue() || state == AVChatType.AUDIO.getValue()) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * 来电参数解析
     */
    private void parseIncomingIntent() {
        avChatData = (AVChatData) getIntent().getSerializableExtra(KEY_CALL_CONFIG);
        Log.i("AVChatActivity avChatData---> ", avChatData.getAccount() + "---");
        state = avChatData.getChatType().getValue();
    }

    /**
     * 去电参数解析
     */
    private void parseOutgoingIntent() {
        receiverId = getIntent().getStringExtra(KEY_ACCOUNT);
        Log.i("AVChatActivity ---> ", receiverId + "---");
        state = getIntent().getIntExtra(KEY_CALL_TYPE, -1);
    }

    /**
     * 注册监听
     *
     * @param register
     */
    private void registerNetCallObserver(boolean register) {
        AVChatManager.getInstance().observeAVChatState(this, register);
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);
        AVChatManager.getInstance().observeControlNotification(callControlObserver, register);
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);
        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register);
        AVChatManager.getInstance().observeTimeoutNotification(timeoutObserver, register);
        AVChatManager.getInstance().observeAutoHangUpForLocalPhone(autoHangUpForLocalPhoneObserver, register);
    }

    /**
     * 注册/注销网络通话被叫方的响应（接听、拒绝、忙）
     */
    Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent ackInfo) {
            if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                avChatUI.closeSessions(AVChatExitCode.PEER_BUSY);
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                avChatUI.closeSessions(AVChatExitCode.REJECT);
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                if (ackInfo.isDeviceReady()) {
                    avChatUI.isCallEstablish.set(true);
                    avChatUI.canSwitchCamera = true;
                } else {
                    // 设备初始化失败
                    Toast.makeText(AVChatActivity.this, R.string.avchat_device_no_ready, Toast.LENGTH_SHORT).show();
                    avChatUI.closeSessions(AVChatExitCode.OPEN_DEVICE_ERROR);
                }
            }
        }
    };

    Observer<AVChatTimeOutEvent> timeoutObserver = new Observer<AVChatTimeOutEvent>() {
        @Override
        public void onEvent(AVChatTimeOutEvent event) {
            if (event == AVChatTimeOutEvent.NET_BROKEN_TIMEOUT) {
                avChatUI.closeSessions(AVChatExitCode.NET_ERROR);
            } else {
                avChatUI.closeSessions(AVChatExitCode.PEER_NO_RESPONSE);
            }

            // 来电超时，自己未接听
            if (event == AVChatTimeOutEvent.INCOMING_TIMEOUT) {
                activeMissCallNotifier();
            }
        }
    };

    Observer<Integer> autoHangUpForLocalPhoneObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {
            avChatUI.closeSessions(AVChatExitCode.PEER_BUSY);
        }
    };

    /**
     * 注册/注销网络通话控制消息（音视频模式切换通知）
     */
    Observer<AVChatControlEvent> callControlObserver = new Observer<AVChatControlEvent>() {
        @Override
        public void onEvent(AVChatControlEvent netCallControlNotification) {
            handleCallControl(netCallControlNotification);
        }
    };

    /**
     * 注册/注销网络通话对方挂断的通知
     */
    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
            avChatUI.closeSessions(AVChatExitCode.HANGUP);
            cancelCallingNotifier();
            // 如果是incoming call主叫方挂断，那么通知栏有通知
            if (mIsInComingCall && !isCallEstablished) {
                activeMissCallNotifier();
            }
        }
    };

    /**
     * 注册/注销同时在线的其他端对主叫方的响应
     */
    Observer<AVChatOnlineAckEvent> onlineAckObserver = new Observer<AVChatOnlineAckEvent>() {
        @Override
        public void onEvent(AVChatOnlineAckEvent ackInfo) {
            if (ackInfo.getClientType() != ClientType.Android) {
                String client = null;
                switch (ackInfo.getClientType()) {
                    case ClientType.Web:
                        client = "Web";
                        break;
                    case ClientType.Windows:
                        client = "Windows";
                        break;
                    default:
                        break;
                }
                if (client != null) {
                    String option = ackInfo.getEvent() == AVChatEventType.CALLEE_ONLINE_CLIENT_ACK_AGREE ? "接听！" : "拒绝！";
                    Toast.makeText(AVChatActivity.this, "通话已在" + client + "端被" + option, Toast.LENGTH_SHORT).show();
                }
                avChatUI.closeSessions(-1);
            }
        }
    };


    /**
     * 接听
     */
    private void inComingCalling() {
        avChatUI.inComingCalling(avChatData);
    }

    /**
     * 拨打
     */
    private void outgoingCalling() {
        if (!NetworkUtil.isNetAvailable(AVChatActivity.this)) { // 网络不可用
            Toast.makeText(this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        avChatUI.outGoingCalling(receiverId, AVChatType.typeOfValue(state));
    }

    /**
     * *************************** AVChatListener *********************************
     */

    @Override
    public void uiExit() {
        finish();
    }


    /**
     * ************************ AVChatStateObserver ****************************
     */

    @Override
    public void onConnectedServer(int res) {
        handleWithConnectServerResult(res);
    }

    @Override
    public void onUserJoin(String account) {
        Log.i(TAG, "onUserJoin  " + account + "----" + getIntent().getStringExtra(KEY_ACCOUNT));
        avChatUI.setVideoAccount(getIntent().getStringExtra(KEY_ACCOUNT));

        avChatUI.initSurfaceView(avChatUI.getVideoAccount());
    }

    @Override
    public void onUserLeave(String account, int event) {

    }

    @Override
    public void onProtocolIncompatible(int status) {

    }

    @Override
    public void onDisconnectServer() {

    }

    @Override
    public void onNetworkStatusChange(int value) {

    }

    @Override
    public void onCallEstablished() {
        Log.d(TAG, "onCallEstablished");
        if (avChatUI.getTimeBase() == 0)
            avChatUI.setTimeBase(SystemClock.elapsedRealtime());
        if (state == AVChatType.AUDIO.getValue()) {
            avChatUI.onCallStateChange(CallStateEnum.AUDIO);
        } else {
//            avChatUI.initSurfaceView(avChatUI.getVideoAccount());
            avChatUI.initLocalSurfaceView();
            avChatUI.onCallStateChange(CallStateEnum.VIDEO);
        }
        isCallEstablished = true;
    }

    @Override
    public void onOpenDeviceError(int code) {

    }

    @Override
    public void onRecordEnd(String[] files, int event) {
        if(files != null && files.length > 0) {

            // test
            for(String file : files){

//                String file = files[0];
                String parent = new File(file).getParent();
                String msg;
                if(event == 0) {
                    msg = "录制已结束";
                } else {
                    msg = "你的手机内存不足, 录制已结束";
                }
                Log.i("record files save path:", msg += ", 录制文件已保存至：" + parent);
            }

            String file = files[0];
            String parent = new File(file).getParent();
            String msg;
            if(event == 0) {
                msg = "录制已结束";
            } else {
                msg = "你的手机内存不足, 录制已结束";
            }

            if(!TextUtils.isEmpty(parent)) {
                msg += ", 录制文件已保存至：" + parent;
            }

            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } else {
            if(event == 1) {
                Toast.makeText(this, "你的手机内存不足, 录制已结束.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "录制已结束.", Toast.LENGTH_SHORT).show();
            }
        }

        if(event == 1) {
            if(avChatUI != null) {
                avChatUI.resetRecordTip();
            }
        }
    }

    /****************************** 连接建立处理 ********************/

    /**
     * 处理连接服务器的返回值
     *
     * @param auth_result
     */
    protected void handleWithConnectServerResult(int auth_result) {
        LogUtil.i(TAG, "result code->" + auth_result);
        if (auth_result == 200) {
            Log.d(TAG, "onConnectServer success");
        } else if (auth_result == 101) { // 连接超时
            avChatUI.closeSessions(AVChatExitCode.PEER_NO_RESPONSE);
        } else if (auth_result == 401) { // 验证失败
            avChatUI.closeSessions(AVChatExitCode.CONFIG_ERROR);
        } else if (auth_result == 417) { // 无效的channelId
            avChatUI.closeSessions(AVChatExitCode.INVALIDE_CHANNELID);
        } else { // 连接服务器错误，直接退出
            avChatUI.closeSessions(AVChatExitCode.CONFIG_ERROR);
        }
    }

    public void startVerification(){
        new WarrantyTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, requestCode + "-------" + resultCode);
        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                boolean verify = data.getBooleanExtra(LivenessActivity2.CONFIDENCE_RESULT, false);
                double value = data.getDoubleExtra(LivenessActivity2.CONFIDENCE_VALUE, 0);
                Log.i(TAG, verify + "-----------" + value);
                if (verify){
                    avChatUI.receiver();
                }else {
                    avChatUI.refuce();
                }
            }
        }else {
            avChatUI.refuce();
        }
    }

    class WarrantyTask extends AsyncTask<Void, Void, Integer> {

        private ProgressDialog mProgressDialog = new ProgressDialog(AVChatActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog.setTitle("授权");
            mProgressDialog.setMessage("正在联网授权中...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Integer doInBackground(Void... params) {


            Manager manager = new Manager(AVChatActivity.this);
            LivenessLicenseManager licenseManager = new LivenessLicenseManager(
                    AVChatActivity.this);
            manager.registerLicenseManager(licenseManager);

            manager.takeLicenseFromNetwork(ConUtil.getUUIDString(AVChatActivity.this));
            if (licenseManager.checkCachedLicense() > 0)
                return 1;
            else
                return 0;
        }


        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mProgressDialog.dismiss();
            if (integer == 1) {
                Intent intent = new Intent(AVChatActivity.this, LivenessActivity2.class);
                intent.putExtra(LivenessActivity2.UUID, "xxx");
                intent.putExtra(LivenessActivity2.IMAGE_REF_PATH, Environment.getExternalStorageDirectory() + "/avatar.png");
                startActivityForResult(intent, 1);
            } else if (integer == 0) {
                // 授权失败
                ToastUtil.showShortToast(AVChatActivity.this, "授权失败");
                avChatUI.refuce();
            }
        }
    }

    /**************************** 处理音视频切换 *********************************/

    /**
     * 处理音视频切换请求
     *
     * @param notification
     */
    private void handleCallControl(AVChatControlEvent notification) {
        switch (notification.getControlCommand()) {
            case SWITCH_AUDIO_TO_VIDEO:
                avChatUI.incomingAudioToVideo();
                break;
            case SWITCH_AUDIO_TO_VIDEO_AGREE:
                onAudioToVideo();
                break;
            case SWITCH_AUDIO_TO_VIDEO_REJECT:
                avChatUI.onCallStateChange(CallStateEnum.AUDIO);
                Toast.makeText(AVChatActivity.this, R.string.avchat_switch_video_reject, Toast.LENGTH_SHORT).show();
                break;
            case SWITCH_VIDEO_TO_AUDIO:
                onVideoToAudio();
                break;
            case NOTIFY_VIDEO_OFF:
                avChatUI.peerVideoOff();
                break;
            case NOTIFY_VIDEO_ON:
                avChatUI.peerVideoOn();
                break;
            case NOTIFY_RECORD_START:
                Toast.makeText(this, "对方开始了通话录制", Toast.LENGTH_SHORT).show();
                break;
            case NOTIFY_RECORD_STOP:
                Toast.makeText(this, "对方结束了通话录制", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * 音频切换为视频
     */
    private void onAudioToVideo() {
        avChatUI.onAudioToVideo();
        avChatUI.initSurfaceView(avChatUI.getVideoAccount());
    }

    /**
     * 视频切换为音频
     */
    private void onVideoToAudio() {
        avChatUI.onCallStateChange(CallStateEnum.AUDIO);
        avChatUI.onVideoToAudio();
    }

    /**
     * 通知栏
     */
    private void activeCallingNotifier() {
        if (notifier != null && !isUserFinish) {
            notifier.activeCallingNotification(true);
        }
    }

    private void cancelCallingNotifier() {
        if (notifier != null) {
            notifier.activeCallingNotification(false);
        }
    }

    private void activeMissCallNotifier() {
        if (notifier != null) {
            notifier.activeMissCallNotification(true);
        }
    }

    @Override
    public void finish() {
        isUserFinish = true;
        super.finish();
    }
}


