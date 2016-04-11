package com.gkzxhn.gkprison.avchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.avchat.toggleview.ToggleListener;
import com.gkzxhn.gkprison.avchat.toggleview.ToggleState;
import com.gkzxhn.gkprison.avchat.toggleview.ToggleView;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nimlib.sdk.avchat.AVChatManager;

/**
 * 视频管理器， 视频界面初始化和相关管理
 * Created by hzxuwen on 2015/5/5.
 */
public class AVChatVideo implements View.OnClickListener, ToggleListener, Anticlockwise.OnTimeCompleteListener {

    // data
    private Context context;
    private View root;
    private AVChatUI manager;
    //顶部控制按钮
    private View topRoot;
    private View switchAudio;
    private Anticlockwise time;
    private TextView tv_shengyu_time;// 剩余时间
    private TextView netUnstableTV;
    //中间控制按钮
    private View middleRoot;
    private HeadImageView headImg;
    private TextView nickNameTV;
    private TextView notifyTV;
    private View refuse_receive;
    private TextView refuseTV;
    private TextView receiveTV;
    //底部控制按钮
    private View bottomRoot;
    ToggleView switchCameraToggle;
    ToggleView closeCameraToggle;
    ToggleView muteToggle;
    ToggleView recordToggle;
    ImageView hangUpImg;

    //record
    private View recordView;
    private View recordTip;
    private View recordWarning;

    private int topRootHeight = 0;
    private int bottomRootHeight = 0;

    private AVChatUIListener listener;

    // state
    private boolean init = false;
    private boolean shouldEnableToggle = false;
    private boolean isInSwitch = false;
    private SharedPreferences sp;

    public AVChatVideo(Context context, View root, AVChatUIListener listener, AVChatUI manager) {
        this.context = context;
        this.root = root;
        this.listener = listener;
        this.manager = manager;
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    private void findViews() {
        if(init || root == null )
            return;
        topRoot = root.findViewById(R.id.avchat_video_top_control);
        switchAudio = topRoot.findViewById(R.id.avchat_video_switch_audio);
        switchAudio.setOnClickListener(this);
        time = (Anticlockwise) topRoot.findViewById(R.id.avchat_video_time);
        tv_shengyu_time = (TextView) topRoot.findViewById(R.id.tv_shengyu_time);
        netUnstableTV = (TextView) topRoot.findViewById(R.id.avchat_video_netunstable);

        middleRoot = root.findViewById(R.id.avchat_video_middle_control);
        headImg = (HeadImageView) middleRoot.findViewById(R.id.avchat_video_head);
        nickNameTV = (TextView) middleRoot.findViewById(R.id.avchat_video_nickname);
        notifyTV = (TextView) middleRoot.findViewById(R.id.avchat_video_notify);

        refuse_receive = middleRoot.findViewById(R.id.avchat_video_refuse_receive);
        refuseTV = (TextView) refuse_receive.findViewById(R.id.refuse);
        receiveTV = (TextView) refuse_receive.findViewById(R.id.receive);
        refuseTV.setOnClickListener(this);
        receiveTV.setOnClickListener(this);

        recordView = root.findViewById(R.id.avchat_record_layout);
        recordTip = recordView.findViewById(R.id.avchat_record_tip);
        recordWarning = recordView.findViewById(R.id.avchat_record_warning);

        bottomRoot = root.findViewById(R.id.avchat_video_bottom_control);
        switchCameraToggle = new ToggleView(bottomRoot.findViewById(R.id.avchat_switch_camera), ToggleState.DISABLE, this);
        closeCameraToggle = new ToggleView(bottomRoot.findViewById(R.id.avchat_close_camera), ToggleState.DISABLE, this);
        muteToggle = new ToggleView(bottomRoot.findViewById(R.id.avchat_video_mute), ToggleState.DISABLE, this);
        recordToggle = new ToggleView(bottomRoot.findViewById(R.id.avchat_video_record), ToggleState.DISABLE, this);
        hangUpImg = (ImageView) bottomRoot.findViewById(R.id.avchat_video_logout);
        hangUpImg.setOnClickListener(this);
        init = true;
    }

    /**
     * 音视频状态变化及界面刷新
     * @param state
     */
    public void onCallStateChange(CallStateEnum state) {
        if(CallStateEnum.isVideoMode(state))
            findViews();
        switch (state){
            case OUTGOING_VIDEO_CALLING:
                showProfile();//对方的详细信息
                showNotify(R.string.avchat_wait_recieve);
                setRefuseReceive(false);
                shouldEnableToggle = true;
                enableToggle();
                setTopRoot(false);
                setMiddleRoot(true);
                setBottomRoot(true);
                break;
            case INCOMING_VIDEO_CALLING:
                showProfile();//对方的详细信息
                showNotify(R.string.avchat_video_call_request);
                setRefuseReceive(true);
                receiveTV.setText(R.string.avchat_pickup);
                setTopRoot(false);
                setMiddleRoot(true);
                setBottomRoot(false);
                break;
            case VIDEO:
                isInSwitch = false;
//                enableToggle();
//                setTime(true);
                if(sp.getBoolean("is_can_video", false)) {
                    setTopRoot(true);
                }else {
                    setTopRoot(false);
                }
                setMiddleRoot(false);
                setBottomRoot(true);
                break;
            case VIDEO_CONNECTING:
                showNotify(R.string.avchat_connecting);
                shouldEnableToggle = true;
                break;
            case OUTGOING_AUDIO_TO_VIDEO:
                isInSwitch = true;
//                if(sp.getBoolean("is_can_video", false)) {
//                    setTopRoot(true);
//                }else {
//                    setTopRoot(false);
//                }
                setTime(true);
                setTopRoot(true);
                setMiddleRoot(false);
                setBottomRoot(true);
                break;
            default:
                break;
        }
        setRoot(CallStateEnum.isVideoMode(state));
    }

    /********************** 界面显示 **********************************/

    /**
     * 显示个人信息
     */
    private void showProfile(){
        String account = manager.getAccount();
        headImg.loadBuddyAvatar(account);
        nickNameTV.setText(NimUserInfoCache.getInstance().getUserDisplayName(account));
    }

    /**
     * 显示通知
     * @param resId
     */
    private void showNotify(int resId){
        notifyTV.setText(resId);
        notifyTV.setVisibility(View.VISIBLE);
    }

    /************************ 布局显隐设置 ****************************/

    public void setRoot(boolean visible) {
        root.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setRefuseReceive(boolean visible){
        refuse_receive.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setTopRoot(boolean visible){
        topRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        if(topRootHeight == 0){
            Rect rect = new Rect();
            topRoot.getGlobalVisibleRect(rect);
            topRootHeight = rect.bottom;
        }
    }

    public void setMiddleRoot(boolean visible){
        middleRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setBottomRoot(boolean visible){
        bottomRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        if(bottomRootHeight == 0){
            bottomRootHeight = bottomRoot.getHeight();
        }
    }

    /**
     * 设置通话时间  屏幕上方正中间
     * @param visible
     */
    public void setTime(boolean visible){
        time.setVisibility(visible ? View.VISIBLE : View.GONE);
        tv_shengyu_time.setVisibility(visible ? View.VISIBLE : View.GONE);
        if(visible){
            time.setOnTimeCompleteListener(this);
            time.initTime(Long.parseLong(sp.getString("current_ms", 900 + "")));
            time.start();
            int surplus_time = (int) (Long.parseLong(sp.getString("current_ms", 900 + "").equals("上次通话已完成") ? 900 + "" : sp.getString("current_ms", 900 + "")) / 60);
            Toast.makeText(context, "开始进行视频通话，您还剩余" +
                    surplus_time + "分钟，请抓紧时间", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 暴露给其它页面操作设置底部控制开关是否可用
     */
    public void setVisibilityToggle(boolean visibility){
        if(visibility) {
            shouldEnableToggle = true;
            enableToggle();
        }
    }

    /**
     * 底部控制开关可用
     */
    private void enableToggle() {
        if (shouldEnableToggle) {
            if (manager.canSwitchCamera())
                switchCameraToggle.enable();
            closeCameraToggle.enable();
            muteToggle.enable();
            recordToggle.enable();
            shouldEnableToggle = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avchat_video_logout:
                listener.onHangUp();
                break;
            case R.id.refuse:
                listener.onRefuse();
                break;
            case R.id.receive:
                listener.onReceive();
                break;
            case R.id.avchat_video_mute:
                listener.toggleMute();
                break;
            case R.id.avchat_video_switch_audio:
                if(isInSwitch) {
                    Toast.makeText(context, R.string.avchat_in_switch, Toast.LENGTH_SHORT).show();
                } else {
                    listener.videoSwitchAudio();
                }
                break;
            case R.id.avchat_switch_camera:
                listener.switchCamera();
                break;
            case R.id.avchat_close_camera:
                listener.closeCamera();
                break;
            case R.id.avchat_video_record:
                listener.toggleRecord();
                break;
            default:
                break;
        }
    }

    public void showRecordView(boolean show, boolean warning) {
        if(show) {
            recordView.setVisibility(View.VISIBLE);
            recordTip.setVisibility(View.VISIBLE);
            if(warning) {
                recordWarning.setVisibility(View.VISIBLE);
            } else {
                recordWarning.setVisibility(View.GONE);
            }
        } else {
            recordView.setVisibility(View.INVISIBLE);
            recordTip.setVisibility(View.INVISIBLE);
            recordWarning.setVisibility(View.GONE);
        }
    }

    /**
     * 音频切换为视频, 界面控件是否开启显示
     * @param muteOn
     */
    public void onAudioToVideo(boolean muteOn){
        muteToggle.toggle(muteOn ? ToggleState.ON : ToggleState.OFF);
        closeCameraToggle.toggle(ToggleState.OFF);
        if(manager.canSwitchCamera()){
            if(AVChatManager.getInstance().isFrontCamera())
                switchCameraToggle.off(false);
            else
                switchCameraToggle.on(false);
        }
    }

    /******************************* toggle listener *************************/
    @Override
    public void toggleOn(View v) {
        onClick(v);
    }

    @Override
    public void toggleOff(View v) {
        onClick(v);
    }

    @Override
    public void toggleDisable(View v) {

    }

    public void closeSession(int exitCode){
        if(init){
            time.stop();
            switchCameraToggle.disable(false);
            muteToggle.disable(false);
            closeCameraToggle.disable(false);
            receiveTV.setEnabled(false);
            refuseTV.setEnabled(false);
            hangUpImg.setEnabled(false);
        }
    }

    @Override
    public void onTimeComplete() {
        Toast.makeText(context, "会话结束", Toast.LENGTH_SHORT).show();
        listener.onHangUp();// 时间到自动挂断
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("current_ms", "上次通话已完成");
        editor.commit();
    }

    @Override
    public void onTimeChanged(long s) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("current_ms", s + "");
        editor.commit();// 保存当前剩余时间
        if(s <= 180) { //剩余会话时间小于3分钟时间颜色报红
            time.setTextColor(context.getResources().getColor(R.color.tv_red));
            tv_shengyu_time.setTextColor(context.getResources().getColor(R.color.tv_red));
        }
    }
}
