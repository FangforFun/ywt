package com.gkzxhn.gkprison.avchat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.event.ExamineEvent;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.VideoChatParam;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;

/**
 * 音视频管理器, 音视频相关功能管理
 * Created by huangzhengneng on 2016/1/23.
 */
public class AVChatUI implements AVChatUIListener {
    // constant
    private static final String TAG = "AVChatUI";

    // data
    private Context context;
    private AVChatData avChatData;
    private final AVChatListener aVChatListener;
    private String receiverId;
    private AVChatAudio avChatAudio;
    private AVChatVideo avChatVideo;
    private AVChatSurface avChatSurface;
    private VideoChatParam videoParam; // 视频采集参数
    private String videoAccount; // 发送视频请求，onUserJoin回调的user account

    private CallStateEnum callingState = CallStateEnum.INVALID;

    private long timeBase = 0;
    private SharedPreferences sp;

    // view
    private View root;

    // state
    public boolean canSwitchCamera = false;
    private boolean isClosedCamera = false;
    public AtomicBoolean isCallEstablish = new AtomicBoolean(false);

    // 检查存储
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean recordWarning = false;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            File dir = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(dir.getPath());
            long blockSize;
            if( Build.VERSION.SDK_INT >= 18) {
                blockSize = stat.getBlockSizeLong();
            } else {
                blockSize = stat.getBlockSize();
            }
            long availableBlocks;
            if(Build.VERSION.SDK_INT >= 18) {
                availableBlocks = stat.getAvailableBlocksLong();
            } else {
                availableBlocks = stat.getAvailableBlocks();
            }

            long size = availableBlocks * blockSize;

            if(size <= 10 * 1024 * 1024) {
                recordWarning = true;
                updateRecordTip();
            } else {
                uiHandler.postDelayed(this, 1000);
            }
        }
    };

    public interface AVChatListener {
        void uiExit();
    }

    public AVChatUI(Context context, View root, AVChatListener listener) {
        this.context = context;
        this.root = root;
        this.aVChatListener = listener;
        sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
    }

    /**
     * ******************************初始化******************************
     */

    /**
     * 初始化，包含初始化音频管理器， 视频管理器和视频界面绘制管理器。
     *
     * @return boolean
     */
    public boolean initiation() {
        AVChatProfile.getInstance().setAVChatting(true);
        avChatAudio = new AVChatAudio(root.findViewById(R.id.avchat_audio_layout), this, this);
        avChatVideo = new AVChatVideo(context, root.findViewById(R.id.avchat_video_layout), this, this);
        avChatSurface = new AVChatSurface(context, this, root.findViewById(R.id.avchat_surface_layout));
        return true;
    }

    /**
     * ******************************拨打和接听***************************
     */

    /**
     * 来电
     */
    public void inComingCalling(AVChatData avChatData) {
        Log.i(TAG, DensityUtil.getScreenWidthHeight(context)[0] + "---" + DensityUtil.getScreenWidthHeight(context)[1]);
        this.avChatData = avChatData;
        receiverId = avChatData.getAccount();
        if (avChatData.getChatType() == AVChatType.AUDIO) {
            onCallStateChange(CallStateEnum.INCOMING_AUDIO_CALLING);
        } else {
            onCallStateChange(CallStateEnum.INCOMING_VIDEO_CALLING);
        }
    }

    /**
     * 拨打音视频
     */
    public void outGoingCalling(String account, AVChatType callTypeEnum) {
        DialogMaker.showProgressDialog(context, null);
        this.receiverId = account;
        VideoChatParam videoParam = null;
        if (callTypeEnum == AVChatType.AUDIO) {
            onCallStateChange(CallStateEnum.OUTGOING_AUDIO_CALLING);
        } else {
            onCallStateChange(CallStateEnum.OUTGOING_VIDEO_CALLING);
            if (videoParam == null) {
                videoParam = new VideoChatParam(avChatSurface.mCapturePreview,
                        ((Activity) context).getWindowManager().getDefaultDisplay().getRotation());
            }
        }

        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        notifyOption.extendMessage = "extra_data";

        /**
         * 发起通话
         * account 对方帐号
         * callTypeEnum 通话类型：语音、视频
         * videoParam 发起视频通话时传入，发起音频通话传null
         * AVChatCallback 回调函数，返回AVChatInfo
         */
        AVChatManager.getInstance().call(account, callTypeEnum, videoParam, notifyOption, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
                avChatData = data;
                DialogMaker.dismissProgressDialog();
                avChatSurface.setExamineButtonVisibility(View.VISIBLE);
//                if(AVChatManager.getInstance().hasMultipleCameras()) {
                canSwitchCamera = true;
//                }
                Log.d(TAG, "success" + AVChatManager.getInstance().hasMultipleCameras() + "---" + canSwitchCamera + AVChatManager.getInstance().isFrontCamera());
                SPUtil.put(context, "is_can_video", true);
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "failed code->" + code);// 408请求超时
                DialogMaker.dismissProgressDialog();
                if (code == 11001) {
                    Toast.makeText(context, "对方不在线", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "发起通话失败" + code, Toast.LENGTH_SHORT).show();
                }
                closeSessions(code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "start call onException->" + exception);
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 状态改变
     *
     * @param stateEnum
     */
    public void onCallStateChange(CallStateEnum stateEnum) {
        callingState = stateEnum;
        avChatSurface.onCallStateChange(stateEnum);
        avChatAudio.onCallStateChange(stateEnum);
        avChatVideo.onCallStateChange(stateEnum);
    }

    /**
     * 挂断
     *
     * @param type 音视频类型
     */
    private void hangUp(final int type) {
        if (type == AVChatExitCode.HANGUP || type == AVChatExitCode.PEER_NO_RESPONSE || type == AVChatExitCode.CANCEL) {
            AVChatManager.getInstance().hangUp(new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "hangup onSuccess");
                }

                @Override
                public void onFailed(int code) {
                    Log.d(TAG, "hangup onFailed->" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    Log.d(TAG, "hangup onException->" + exception);
                }
            });
        }
        closeSessions(type);
    }

    public void receiver(){
        Log.i("AVChatUI receiver");
        avChatVideo.receiver();
    }

    public void refuce(){
        Log.i("AVChatUI refuce");
        avChatVideo.refuse();
    }

    /**
     * 关闭本地音视频各项功能
     *
     * @param exitCode 音视频类型
     */
    public void closeSessions(int exitCode) {
        //not  user  hang up active  and warning tone is playing,so wait its end
        Log.i(TAG, "close session -> " + AVChatExitCode.getExitString(exitCode));
        if (avChatAudio != null)
            avChatAudio.closeSession(exitCode);
        if (avChatVideo != null)
            avChatVideo.closeSession(exitCode);
        uiHandler.removeCallbacks(runnable);
        showQuitToast(exitCode);
        isCallEstablish.set(false);
        canSwitchCamera = false;
        isClosedCamera = false;
        aVChatListener.uiExit();
    }

    /**
     * 给出结束的提醒
     *
     * @param code
     */
    public void showQuitToast(int code) {
        switch (code) {
            case AVChatExitCode.NET_CHANGE: // 网络切换
            case AVChatExitCode.NET_ERROR: // 网络异常
            case AVChatExitCode.CONFIG_ERROR: // 服务器返回数据错误
                Toast.makeText(context, R.string.avchat_net_error_then_quit, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_HANGUP://挂断
            case AVChatExitCode.HANGUP:
                if (isCallEstablish.get()) {
                    Toast.makeText(context, R.string.avchat_call_finish, Toast.LENGTH_SHORT).show();
                }
                break;
            case AVChatExitCode.PEER_BUSY:// 用户正忙
                Toast.makeText(context, R.string.avchat_peer_busy, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_PEER_LOWER:// 对方版本过低
                Toast.makeText(context, R.string.avchat_peer_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_SELF_LOWER:// 本机版本过低
                Toast.makeText(context, R.string.avchat_local_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.INVALIDE_CHANNELID:// 对方已挂断
                Toast.makeText(context, R.string.avchat_invalid_channel_id, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.LOCAL_CALL_BUSY:// 正在进行本地通话
                Toast.makeText(context, R.string.avchat_local_call_busy, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.REJECT:// 对方拒绝
                Toast.makeText(context, R.string.avchat_peer_reject, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_NO_RESPONSE:// 无回应
                Toast.makeText(context, R.string.avchat_no_response, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * ******************************* 接听与拒绝操作 *********************************
     */

    /**
     * 拒绝来电
     */
    private void rejectInComingCall() {
        /**
         * 接收方拒绝通话
         * AVChatCallback 回调函数
         */
        AVChatManager.getInstance().hangUp(new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "reject success");
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "reject success->" + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "reject success");
            }
        });
        closeSessions(AVChatExitCode.REJECT);
    }

    /**
     * 拒绝音视频切换
     */
    private void rejectAudioToVideo() {
        onCallStateChange(CallStateEnum.AUDIO);
        AVChatManager.getInstance().ackSwitchToVideo(false, videoParam, null); // 音频切换到视频请求的回应. true为同意，false为拒绝
        updateRecordTip();
    }

    //Only for test
    private int getVideoDimens() {

        File file = new File("sdcard/nim.properties");

        if(file.exists()) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(file));
                Properties properties = new Properties();
                properties.load(is);
                int dimens = Integer.parseInt(properties.getProperty("avchat.video.dimens", "0"));
                LogUtil.i(TAG, "dimes:" + dimens);
                return dimens;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        return 0;
    }

    /**
     * 接听来电
     */
    private void receiveInComingCall() {
        //接听，告知服务器，以便通知其他端
        VideoChatParam videoParam = null;

        if (callingState == CallStateEnum.INCOMING_AUDIO_CALLING) {
            onCallStateChange(CallStateEnum.AUDIO_CONNECTING);
        } else {
            onCallStateChange(CallStateEnum.VIDEO_CONNECTING);
            videoParam = new VideoChatParam(avChatSurface.mCapturePreview, 0, getVideoDimens());
        }
        /**
         * 接收方接听电话
         * videoParam 接听视频通话时传入，接听音频通话传null
         * AVChatCallback 回调函数。成功则连接建立，不成功则关闭activity。
         */
        AVChatManager.getInstance().accept(videoParam, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void v) {
                LogUtil.i(TAG, "accept success");

                isCallEstablish.set(true);
                canSwitchCamera = true;
            }

            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    Toast.makeText(context, "本地音视频启动失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "建立连接失败", Toast.LENGTH_SHORT).show();
                }
                LogUtil.e(TAG, "accept onFailed->" + code);
                closeSessions(AVChatExitCode.CANCEL);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "accept exception->" + exception);
            }
        });
    }

    /*************************** AVChatUIListener ******************************/

    /**
     * 点击挂断或取消
     */
    @Override
    public void onHangUp() {
        if (isCallEstablish.get()) {
            hangUp(AVChatExitCode.HANGUP);
        } else {
            hangUp(AVChatExitCode.CANCEL);
        }
//        // 停止录制
//        if(AVChatManager.getInstance().isRecording()) {
//            AVChatManager.getInstance().stopRecord(new AVChatCallback<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    Log.i("stop record success ---> ", "success");
//                    Toast.makeText(context, "录像成功，已保存至SD卡", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onFailed(int i) {
//                    Log.i("stop record failed ---> ", "failed " + i);
//                }
//
//                @Override
//                public void onException(Throwable throwable) {
//                    Log.i("stop record exception ---> ", throwable.getMessage());
//                }
//            });
//        }
    }

    /**
     * 拒绝操作，根据当前状态来选择合适的操作
     */
    @Override
    public void onRefuse() {
        switch (callingState) {
            case INCOMING_AUDIO_CALLING:
            case AUDIO_CONNECTING:
                rejectInComingCall();
                break;
            case INCOMING_AUDIO_TO_VIDEO:
                rejectAudioToVideo();
                break;
            case INCOMING_VIDEO_CALLING:
            case VIDEO_CONNECTING: // 连接中点击拒绝
                rejectInComingCall();
                break;
            default:
                break;
        }
    }

    /**
     * 开启操作，根据当前状态来选择合适的操作
     */
    @Override
    public void onReceive() {
        switch (callingState) {
            case INCOMING_AUDIO_CALLING:
                receiveInComingCall();
                onCallStateChange(CallStateEnum.AUDIO_CONNECTING);
                break;
            case AUDIO_CONNECTING: // 连接中，继续点击开启 无反应
                break;
            case INCOMING_VIDEO_CALLING:
                receiveInComingCall();
                onCallStateChange(CallStateEnum.VIDEO_CONNECTING);
                break;
            case VIDEO_CONNECTING: // 连接中，继续点击开启 无反应
                break;
            case INCOMING_AUDIO_TO_VIDEO:
                receiveAudioToVideo();
            default:
                break;
        }
    }

    @Override
    public void toggleMute() {
        if (!isCallEstablish.get()) { // 连接未建立，在这里记录静音状态
            return;
        } else { // 连接已经建立
            if (!AVChatManager.getInstance().isMute()) { // isMute是否处于静音状态
                // 关闭音频
                AVChatManager.getInstance().setMute(true);
            } else {
                // 打开音频
                AVChatManager.getInstance().setMute(false);
            }
        }
    }

    @Override
    public void toggleSpeaker() {
        AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled()); // 设置扬声器是否开启
    }

    @Override
    public void toggleRecord() {
        if(AVChatManager.getInstance().isRecording()) {
            AVChatManager.getInstance().stopRecord(new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });

            uiHandler.removeCallbacks(runnable);
            recordWarning = false;

        } else {
            recordWarning = false;

            if(AVChatManager.getInstance().startRecord(new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            })) {

                if(CallStateEnum.isAudioMode(callingState)) {
                    Toast.makeText(context, "仅录制你说话的内容", Toast.LENGTH_SHORT).show();
                }

                if(CallStateEnum.isVideoMode(callingState)) {
                    Toast.makeText(context, "仅录制你的声音和图像", Toast.LENGTH_SHORT).show();
                }

                uiHandler.post(runnable);

            } else {

                Toast.makeText(context, "录制失败", Toast.LENGTH_SHORT).show();
            }
        }

        updateRecordTip();
    }

    @Override
    public void videoSwitchAudio() {
        /**
         * 请求视频切换到音频
         */
        AVChatManager.getInstance().requestSwitchToAudio(new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 界面布局切换。
                onCallStateChange(CallStateEnum.AUDIO);
                onVideoToAudio();
                avChatAudio.setSTime(true);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    public void setExamine(String msg) {
        Log.d("event", msg);
        if(!TextUtils.isEmpty(msg) && msg.equals("审核通过")) {
            avChatVideo.setTime(true);// 开始计时
            avChatVideo.setTopRoot(true);// 设置顶部栏可见
            avChatVideo.setVisibilityToggle(true);// 设置底部开关可用
            avChatSurface.setThroughtVisibility(View.GONE);
            SPUtil.put(context, "is_can_video", true);
            SPUtil.put(context, "current_ms", 900 + "");
        } else if (!TextUtils.isEmpty(msg) && msg.equals("审核未通过")) {
            // 审核未通过  自动挂断  家属端提示审核未通过
            hangUp(AVChatExitCode.HANGUP);
        }
    }

    @Override
    public void audioSwitchVideo() {
        onCallStateChange(CallStateEnum.OUTGOING_AUDIO_TO_VIDEO);
        videoParam = new VideoChatParam(avChatSurface.mCapturePreview,
                ((Activity) context).getWindowManager().getDefaultDisplay().getRotation());
        /**
         * 请求音频切换到视频
         */
        AVChatManager.getInstance().requestSwitchToVideo(videoParam, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "requestSwitchToVideo onSuccess");

            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "requestSwitchToVideo onFailed" + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "requestSwitchToVideo onException" + exception);
            }
        });
    }

    @Override
    public void switchCamera() {
        AVChatManager.getInstance().toggleCamera(); // 切换摄像头（主要用于前置和后置摄像头切换）
    }

    @Override
    public void closeCamera() {
        if (!isClosedCamera) {
            // 关闭摄像头
            AVChatManager.getInstance().toggleLocalVideo(false, null);
            isClosedCamera = true;
            avChatSurface.localVideoOff();
        } else {
            // 打开摄像头
            AVChatManager.getInstance().toggleLocalVideo(true, null);
            isClosedCamera = false;
            avChatSurface.localVideoOn();
        }
    }

    /**
     * 音频切换为视频的请求
     */
    public void incomingAudioToVideo() {
        onCallStateChange(CallStateEnum.INCOMING_AUDIO_TO_VIDEO);
        if (videoParam == null) {
            videoParam = new VideoChatParam(avChatSurface.mCapturePreview,
                    ((Activity) context).getWindowManager().getDefaultDisplay().getRotation());
        }
    }

    /**
     * 同意音频切换为视频
     */
    private void receiveAudioToVideo() {
        AVChatManager.getInstance().ackSwitchToVideo(true, videoParam, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onAudioToVideo();
                initSurfaceView(videoAccount);
//                if (sp.getBoolean("is_can_video", true)) {
                // 会有这个操作都是已经审核通过了的   不用再判断
                avChatSurface.setThroughtVisibility(View.GONE);
//                }
                avChatVideo.setTime(true);// 开始计时
                avChatVideo.setTopRoot(true);// 设置顶部栏可见
                avChatVideo.setVisibilityToggle(true);// 设置底部开关可用
                avChatSurface.setThroughtVisibility(View.GONE);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        }); // 音频切换到视频请求的回应. true为同意，false为拒绝
    }

    /**
     * 初始化大小图像
     *
     * @param largeAccount 对方的帐号
     */
    public void initSurfaceView(String largeAccount) {
        Log.i("AVChatUI initSurfaceView ---> ", largeAccount + "----" + DemoCache.getAccount() + "----" + avChatData.toString());
        avChatSurface.initLargeSurfaceView(avChatData.getAccount());
        avChatSurface.initSmallSurfaceView(DemoCache.getAccount());
        Log.i(TAG, avChatData.getAccount() + "----" + DemoCache.getAccount());
        if(avChatData.getAccount().length() == 32) { // 发起者(狱警方)  普通用户都是32位
            Log.i(TAG, "发起者(狱警方)");
            avChatSurface.setIsComing(false);
            avChatSurface.setonThroughExamineClickListener(new AVChatSurface.OnThroughExamineListener() {
                @Override
                public void onClick() {
                    HttpUtils httpUtils = new HttpUtils(5000);
                    RequestParams params = new RequestParams();
                    params.setContentType("application/json");
                    try {
                        StringEntity entity = new StringEntity("{" +
                                "    \"notification\": {" +
                                "        \"code\": 200," +
                                "        \"receiver\": \"" + sp.getString("family_accid", "") + "\"," +
                                "        \"sender\": \"" + sp.getString("token", "") + "\"" +
                                "    }" +
                                "}", HTTP.UTF_8);
                        params.setBodyEntity(entity);
                        Log.i(TAG, "through entity is :" + entity);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    httpUtils.send(HttpRequest.HttpMethod.POST, Constants.URL_HEAD + Constants.VIDEO_EXAMIME, params, new RequestCallBack<Object>() {
                        @Override
                        public void onSuccess(ResponseInfo<Object> responseInfo) {
                            Log.i("审核通过成功", responseInfo.result.toString());
                            avChatSurface.setExamineButtonVisibility(View.GONE);
                            avChatVideo.setTime(true);// 开始计时
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Log.i("审核通过失败", s);
                            EventBus.getDefault().post(new ExamineEvent("发送审核状态异常"));

                            try {
                                //用于格式化日期,作为日志文件名的一部分
                                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
                                String time = formatter.format(new Date());
                                String fileName = "审核异常日志文件" + time + "-" + ".txt";
                                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                    String path = Environment.getExternalStorageDirectory().getPath();
                                    File dir = new File(path);
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }
                                    FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
                                    fos.write(e.toString().getBytes());
                                    fos.close();
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            });
            avChatSurface.setonNotThroughExamineClickListener(new AVChatSurface.OnNotThroughExamineListener() {
                @Override
                public void onClick() {
                    HttpUtils httpUtils = new HttpUtils(5000);
                    RequestParams params = new RequestParams();
                    params.setContentType("application/json");
                    try {
                        StringEntity entity = new StringEntity("{" +
                                "    \"notification\": {" +
                                "        \"code\": 401," +
                                "        \"receiver\": \"" + sp.getString("family_accid", "") + "\"," +
                                "        \"sender\": \"" + sp.getString("token", "") + "\"" +
                                "    }" +
                                "}", HTTP.UTF_8);
                        Log.i(TAG, "not through entity is :" + entity);
                        params.setBodyEntity(entity);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    httpUtils.send(HttpRequest.HttpMethod.POST, Constants.URL_HEAD + Constants.VIDEO_EXAMIME, params, new RequestCallBack<Object>() {
                        @Override
                        public void onSuccess(ResponseInfo<Object> responseInfo) {
                            Log.i("审核不通过成功", responseInfo.result.toString());
                            avChatSurface.setExamineButtonVisibility(View.GONE);
                            //  挂断
                            hangUp(AVChatExitCode.HANGUP);
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Log.i("审核不通过失败", s);
                            EventBus.getDefault().post(new ExamineEvent("发送审核状态异常"));
                        }
                    });
                }
            });
        }else {
            Log.i(TAG, "不是发起者(狱警方)");
            if(sp.getBoolean("is_can_video", false)){
                avChatVideo.setTime(true);// 开始计时
                avChatVideo.setTopRoot(true);// 设置顶部栏可见
            }else {
                avChatSurface.setIsComing(true);
                avChatVideo.setTopRoot(false);
            }
        }
    }

    public void initRemoteSurfaceView(String account) {
        avChatSurface.initLargeSurfaceView(avChatData.getAccount());
    }

    public void initLocalSurfaceView() {
        avChatSurface.initSmallSurfaceView(DemoCache.getAccount());
    }

    private void updateRecordTip() {

        if(CallStateEnum.isAudioMode(callingState)) {
            avChatAudio.showRecordView(AVChatManager.getInstance().isRecording(), recordWarning);
        }
        if(CallStateEnum.isVideoMode(callingState)) {
            avChatVideo.showRecordView(AVChatManager.getInstance().isRecording(), recordWarning);
        }

    }

    public void resetRecordTip() {
        uiHandler.removeCallbacks(runnable);
        recordWarning = false;
        if(CallStateEnum.isAudioMode(callingState)) {
            avChatAudio.showRecordView(AVChatManager.getInstance().isRecording(), recordWarning);
        }
        if(CallStateEnum.isVideoMode(callingState)) {
            avChatVideo.showRecordView(AVChatManager.getInstance().isRecording(), recordWarning);
        }
    }

    /**
     * 音频切换为视频
     */
    public void onAudioToVideo() {
        onCallStateChange(CallStateEnum.VIDEO);
        avChatVideo.onAudioToVideo(AVChatManager.getInstance().isMute()); // isMute是否处于静音状态
        if (!AVChatManager.getInstance().isVideoSend()) { // 是否在发送视频 即摄像头是否开启
            AVChatManager.getInstance().toggleLocalVideo(true, null);
            avChatSurface.localVideoOn();
            isClosedCamera = false;
        }
    }

    /**
     * 视频切换为音频
     */
    public void onVideoToAudio() {
        // 判断是否静音，扬声器是否开启，对界面相应控件进行显隐处理。
        avChatAudio.onVideoToAudio(AVChatManager.getInstance().isMute(), AVChatManager.getInstance().speakerEnabled());
    }

    public void peerVideoOff() {
        avChatSurface.peerVideoOff();
    }

    public void peerVideoOn() {
        avChatSurface.peerVideoOn();
    }

    /**
     * // 恢复视频聊天（用于视频聊天退到后台后，从后台恢复时调用）
     */
    public void resumeVideo() {
        AVChatManager.getInstance().resumeVideo(avChatSurface.isLocalPreviewInSmallSize());
    }

    public boolean canSwitchCamera() {
        return canSwitchCamera;
    }

    public CallStateEnum getCallingState() {
        return callingState;
    }

    public String getVideoAccount() {
        return videoAccount;
    }

    public void setVideoAccount(String videoAccount) {
        this.videoAccount = videoAccount;
    }

    public String getAccount() {
        if (receiverId != null)
            return receiverId;
        return null;
    }

    public long getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(long timeBase) {
        this.timeBase = timeBase;
    }
}
