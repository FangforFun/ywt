package com.gkzxhn.gkprison.avchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nimlib.sdk.avchat.AVChatManager;

/**
 * 视频绘制管理
 * Created by hzxuwen on 2015/5/6.
 */
public class AVChatSurface {

    private Context context;
    private AVChatUI manager;
    private View surfaceRoot;
    private Handler uiHandler;
    private FrameLayout fl_examine;
    private Button bt_through_examine;
    private Button bt_not_through_examine;

    public boolean getIsComing() {
        return isComing;
    }

    public void setIsComing(boolean isComing) {
        this.isComing = isComing;
    }

    private boolean isComing = false;

    // constant
    private static final int PEER_CLOSE_CAMERA = 0;
    private static final int LOCAL_CLOSE_CAMERA = 1;
    private static final int AUDIO_TO_VIDEO_WAIT = 2;
    private static final int TOUCH_SLOP = 10;

    // view
    private LinearLayout largeSizePreviewLayout;
    public SurfaceView mCapturePreview ;
    private SurfaceView smallSizeSurfaceView;// always added into small size layout
    private FrameLayout smallSizePreviewFrameLayout;
    private LinearLayout smallSizePreviewLayout;
    private ImageView smallSizePreviewCoverImg;//stands for peer or local close camera
    private ImageView iv_meeting_ic_card; // 对方身份证正面照
    private ImageView iv_meeting_icon;// 对方头像
    private View largeSizePreviewCoverLayout;//stands for peer or local close camera

    // state
    private boolean init =false;
    private boolean localPreviewInSmallSize = true;
    private boolean isPeerVideoOff = false;
    private boolean isLocalVideoOff = false;

    // 审核通话或未通过点击监听
    private OnThroughExamineListener onThroughExamineListener;
    private OnNotThroughExamineListener onNotThroughExamineListener;

    // move
    private int lastX, lastY;
    private int inX, inY;
    private Rect paddingRect;

    // data
    private String largeAccount; // 显示在大图像的用户id
    private String smallAccount; // 显示在小图像的用户id

    private BitmapUtils bitmapUtils;
    private SharedPreferences sp;
    private int current_show = 1; // 目前显示的身份证前后照  1 表示img_url_01    2 表示img_url_02

    public AVChatSurface(Context context, AVChatUI manager, View surfaceRoot) {
        this.context = context;
        this.manager = manager;
        this.surfaceRoot = surfaceRoot;
        this.uiHandler = new Handler(context.getMainLooper());
    }

    private void findViews() {
        if(init)
            return;
        if(surfaceRoot != null){
            mCapturePreview = (SurfaceView) surfaceRoot.findViewById(R.id.capture_preview);
            mCapturePreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            smallSizePreviewFrameLayout = (FrameLayout) surfaceRoot.findViewById(R.id.small_size_preview_layout);
            smallSizePreviewLayout = (LinearLayout) surfaceRoot.findViewById(R.id.small_size_preview);
            smallSizePreviewCoverImg = (ImageView) surfaceRoot.findViewById(R.id.smallSizePreviewCoverImg);
            iv_meeting_ic_card = (ImageView) surfaceRoot.findViewById(R.id.iv_meeting_ic_card);
            iv_meeting_icon = (ImageView) surfaceRoot.findViewById(R.id.iv_meeting_icon);
            fl_examine = (FrameLayout) surfaceRoot.findViewById(R.id.fl_examine);
            bt_through_examine = (Button) surfaceRoot.findViewById(R.id.bt_through_examine);
            bt_not_through_examine = (Button) surfaceRoot.findViewById(R.id.bt_not_through_examine);
            smallSizePreviewFrameLayout.setOnTouchListener(touchListener);

            largeSizePreviewLayout = (LinearLayout) surfaceRoot.findViewById(R.id.large_size_preview);
            largeSizePreviewCoverLayout = surfaceRoot.findViewById(R.id.notificationLayout);

            init = true;
            bitmapUtils = new BitmapUtils(context);
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
    }

    /**
     * 设置审核点击事件
     * @param onThroughExamineListener
     */
    public void setonThroughExamineClickListener(OnThroughExamineListener onThroughExamineListener){
        this.onThroughExamineListener = onThroughExamineListener;
    }

    public interface OnThroughExamineListener {
        // 点击事件
        public void onClick();
    }

    /**
     * 设置审核未通过点击事件
     * @param onNotThroughExamineListener
     */
    public void setonNotThroughExamineClickListener(OnNotThroughExamineListener onNotThroughExamineListener){
        this.onNotThroughExamineListener = onNotThroughExamineListener;
    }

    public interface OnNotThroughExamineListener {
        // 点击事件
        public void onClick();
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            if(sp.getBoolean("is_can_video", false)){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        int[] p = new int[2];
                        smallSizePreviewFrameLayout.getLocationOnScreen(p);
                        inX = x - p[0];
                        inY = y - p[1];

                        break;
                    case MotionEvent.ACTION_MOVE:
                        final int diff = Math.max(Math.abs(lastX - x), Math.abs(lastY - y));
                        if (diff < TOUCH_SLOP)
                            break;

                        if (paddingRect == null) {
                            paddingRect = new Rect(ScreenUtil.dip2px(10), ScreenUtil.dip2px(20), ScreenUtil.dip2px(10),
                                    ScreenUtil.dip2px(70));
                        }

                        int destX, destY;
                        if (x - inX <= paddingRect.left) {
                            destX = paddingRect.left;
                        } else if (x - inX + v.getWidth() >= ScreenUtil.screenWidth - paddingRect.right) {
                            destX = ScreenUtil.screenWidth - v.getWidth() - paddingRect.right;
                        } else {
                            destX = x - inX;
                        }

                        if (y - inY <= paddingRect.top) {
                            destY = paddingRect.top;
                        } else if (y - inY + v.getHeight() >= ScreenUtil.screenHeight - paddingRect.bottom) {
                            destY = ScreenUtil.screenHeight - v.getHeight() - paddingRect.bottom;
                        } else {
                            destY = y - inY;
                        }

                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                        params.gravity = Gravity.NO_GRAVITY;
                        params.leftMargin = destX;
                        params.topMargin = destY;
                        v.setLayoutParams(params);

                        break;
                    case MotionEvent.ACTION_UP:
                        if (Math.max(Math.abs(lastX - x), Math.abs(lastY - y)) <= 5) {
                            if (largeAccount == null || smallAccount == null) {
                                return true;
                            }
                            String temp;
                            switchRender(smallAccount, largeAccount);
                            temp = largeAccount;
                            largeAccount = smallAccount;
                            smallAccount = temp;
                            switchAndSetLayout();
                        } else {

                        }

                        break;
                }
            }else {

            }
            return true;
        }
    };

    /**
     * 设置审核通过
     */
    public void setThroughtVisibility(int visibility){
        fl_examine.setVisibility(visibility);
    }

    public void onCallStateChange(CallStateEnum state) {
        if(CallStateEnum.isVideoMode(state))
            findViews();
        switch (state){
            case VIDEO:
                largeSizePreviewCoverLayout.setVisibility(View.GONE);
                break;
            case OUTGOING_AUDIO_TO_VIDEO:
                showNotificationLayout(AUDIO_TO_VIDEO_WAIT);
                break;
            case INCOMING_AUDIO_TO_VIDEO:
                break;
            case INCOMING_VIDEO_CALLING:// 来电
                iv_meeting_ic_card.setVisibility(View.GONE);
                iv_meeting_icon.setVisibility(View.GONE);
                bt_through_examine.setVisibility(View.GONE);
                bt_not_through_examine.setVisibility(View.GONE);
                fl_examine.setVisibility(View.VISIBLE);
                AVChatManager.getInstance().setMute(true);// 静音
                String network_type = SystemUtil.GetNetworkType(context);
                Log.i("当前网络状态", "------------" + network_type);
                switch (network_type){
                    case "2G":
                        Toast.makeText(context, "目前网络处于2G状态，请尽快切换到wifi，否则可能会影响通话质量！", Toast.LENGTH_SHORT);
                        break;
                    case "3G":
                        Toast.makeText(context, "目前网络处于3G状态，请尽快切换到wifi，否则可能会影响通话质量！", Toast.LENGTH_SHORT);
                        break;
                    case "4G":
                        Toast.makeText(context, "目前网络处于4G状态，请尽快切换到wifi，否则可能会产生高额流量费！", Toast.LENGTH_SHORT);
                        break;
                    case "WIFI":
                        Toast.makeText(context, "目前网络处于wifi状态，请保持网络畅通，否则可能会产生高额流量费！", Toast.LENGTH_SHORT);
                        break;
                    default:
                        Toast.makeText(context, "目前网络处于默认状态，请尽快切换到wifi，否则可能会影响通话质量！", Toast.LENGTH_SHORT);
                        break;
                }
                break;
            case OUTGOING_VIDEO_CALLING:// 去电
                iv_meeting_ic_card.setVisibility(View.VISIBLE);
                iv_meeting_icon.setVisibility(View.VISIBLE);
                bt_through_examine.setVisibility(View.VISIBLE);
                bt_not_through_examine.setVisibility(View.VISIBLE);
                bt_through_examine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(onThroughExamineListener != null) {
                            onThroughExamineListener.onClick();
                        }
                    }
                });
                bt_not_through_examine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(onNotThroughExamineListener != null) {
                            onNotThroughExamineListener.onClick();
                        }
                    }
                });
                bitmapUtils.display(iv_meeting_ic_card, sp.getString(current_show == 1 ? "img_url_01" : "img_url_02", ""), new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                        imageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                        iv_meeting_ic_card.setImageResource(R.drawable.ic_card);
                    }
                });
                bitmapUtils.display(iv_meeting_icon, sp.getString("img_url_03", ""), new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                        imageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                        iv_meeting_ic_card.setImageResource(R.drawable.default_icon);
                    }
                });
                iv_meeting_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog dialog = new AlertDialog.Builder(context).create();
                        View view = View.inflate(context, R.layout.icon_dialog, null);
                        ImageView imageView = (ImageView) view.findViewById(R.id.iv_meeting_icon);
                        bitmapUtils.display(imageView, sp.getString("img_url_03", ""));
                        dialog.setView(view);
                        Window dialogWindow = dialog.getWindow();
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.LEFT);
                        dialogWindow.setAttributes(lp);
                        dialog.show();
                        // 点击图片消失
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                iv_meeting_ic_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ToDo
                        final AlertDialog dialog = new AlertDialog.Builder(context).create();
                        ImageView imgView = getView(sp.getString(current_show == 1 ? "img_url_01" : "img_url_02", ""));
                        imgView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                        dialog.setView(imgView);
                        Window dialogWindow = dialog.getWindow();
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.LEFT);
                        lp.width = WindowManager.LayoutParams.WRAP_CONTENT; // 宽度
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
                        lp.alpha = 0.95f; // 透明度
                        dialogWindow.setAttributes(lp);
                        dialog.show();
                        // 点击图片消失
                        imgView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                current_show = current_show == 1 ? 2 : 1;
                            }
                        });
                    }
                });
                break;
            default:
                break;
        }
        setSurfaceRoot(CallStateEnum.isVideoMode(state));
    }

    /**
     * 设置审核按钮显示/隐藏
     * @param visibility
     */
    public void setExamineButtonVisibility(int visibility){
        bt_through_examine.setVisibility(visibility);
        bt_not_through_examine.setVisibility(visibility);
    }

    /**
     * 获取view
     * @return
     */
    private ImageView getView(String img_url) {
        ImageView imgView = new ImageView(context);
        bitmapUtils.display(imgView, img_url);
        return imgView;
    }

    /**
     * 大图像surfaceview 初始化
     * @param account 显示视频的用户id
     */
    public void initLargeSurfaceView(String account){
        largeAccount = account;
        findViews();
        /**
         * 获取视频SurfaceView，加入到自己的布局中，用于呈现视频图像
         * account 要显示视频的用户帐号
         */
        SurfaceView surfaceView = AVChatManager.getInstance().getSurfaceRender(account);
        if (surfaceView != null) {
            addIntoLargeSizePreviewLayout(surfaceView);
        }
    }

    /**
     * 小图像surfaceview 初始化
     * @param account
     * @return
     */
    public void initSmallSurfaceView(String account){
        smallAccount = account;
        findViews();
        /**
         * 获取视频SurfaceView，加入到自己的布局中，用于呈现视频图像
         * account 要显示视频的用户帐号
         */
        SurfaceView surfaceView = AVChatManager.getInstance().getSurfaceRender(account);
        if (surfaceView != null) {
            smallSizeSurfaceView = surfaceView;
            addIntoSmallSizePreviewLayout();
        }
    }

    /**
     * 添加surfaceview到largeSizePreviewLayout
     * @param surfaceView
     */
    private void addIntoLargeSizePreviewLayout(SurfaceView surfaceView) {
        if (surfaceView.getParent() != null)
            ((ViewGroup)surfaceView.getParent()).removeView(surfaceView);
        largeSizePreviewLayout.addView(surfaceView);
        if(manager.getCallingState() == CallStateEnum.VIDEO)
            largeSizePreviewCoverLayout.setVisibility(View.GONE);
    }

    /**
     * 添加surfaceview到smallSizePreviewLayout
     */
    private void addIntoSmallSizePreviewLayout() {
        smallSizePreviewCoverImg.setVisibility(View.GONE);
        if (smallSizeSurfaceView.getParent() != null) {
            ((ViewGroup)smallSizeSurfaceView.getParent()).removeView(smallSizeSurfaceView);
        }
        smallSizePreviewLayout.addView(smallSizeSurfaceView);
        smallSizeSurfaceView.setZOrderMediaOverlay(true);
        smallSizePreviewLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 关闭小窗口
     */
    private void closeSmallSizePreview() {
        smallSizePreviewCoverImg.setVisibility(View.VISIBLE);
    }

    /**
     * 对方打开了摄像头
     */
    public void peerVideoOn() {
        isPeerVideoOff = false;
        if (localPreviewInSmallSize) {
            largeSizePreviewCoverLayout.setVisibility(View.GONE);
        } else {
            smallSizePreviewCoverImg.setVisibility(View.GONE);
        }
    }

    /**
     * 对方关闭了摄像头
     */
    public void peerVideoOff(){
        isPeerVideoOff = true;
        if(localPreviewInSmallSize){ //local preview in small size layout, then peer preview should in large size layout
            showNotificationLayout(PEER_CLOSE_CAMERA);
        }else{  // peer preview in small size layout
            closeSmallSizePreview();
        }
    }

    /**
     * 本地打开了摄像头
     */
    public void localVideoOn() {
        isLocalVideoOff = false;
        if (localPreviewInSmallSize) {
            smallSizePreviewCoverImg.setVisibility(View.GONE);
        } else {
            largeSizePreviewCoverLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 本地关闭了摄像头
     */
    public void localVideoOff(){
        isLocalVideoOff = true;
        if(localPreviewInSmallSize)
            closeSmallSizePreview();
        else
            showNotificationLayout(LOCAL_CLOSE_CAMERA);
    }

    /**
     * 摄像头切换时，布局显隐
     */
    private void switchAndSetLayout() {
        localPreviewInSmallSize = !localPreviewInSmallSize;
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
        smallSizePreviewCoverImg.setVisibility(View.GONE);
        if(isPeerVideoOff) {
            peerVideoOff();
        }
        if(isLocalVideoOff) {
            localVideoOff();
        }
    }

    /**
     * 界面提示
     * @param closeType
     */
    private void showNotificationLayout(int closeType){
        TextView textView = (TextView) largeSizePreviewCoverLayout;
        switch (closeType){
            case PEER_CLOSE_CAMERA:
                textView.setText(R.string.avchat_peer_close_camera);
                break;
            case LOCAL_CLOSE_CAMERA:
                textView.setText(R.string.avchat_local_close_camera);
                break;
            case AUDIO_TO_VIDEO_WAIT:
                textView.setText(R.string.avchat_audio_to_video_wait);
                break;
            default:
                return;
        }
        largeSizePreviewCoverLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 布局是否可见
     * @param visible
     */
    private void setSurfaceRoot(boolean visible) {
        surfaceRoot.setVisibility(visible ? View.VISIBLE: View.GONE);
    }

    /**
     * 大小图像显示切换
     * @param user1 用户1的account
     * @param user2 用户2的account
     */
    private void switchRender(String user1, String user2){
        AVChatManager.getInstance().switchRender(user1, user2);
    }

    /**
     * 是否本地预览图像在小图像（UI上层）
     * @return
     */
    public boolean isLocalPreviewInSmallSize() {
        return localPreviewInSmallSize;
    }
}
