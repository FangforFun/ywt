package com.gkzxhn.gkprison.avchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.utils.DensityUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nimlib.sdk.avchat.AVChatManager;

import java.io.InputStream;

/**
 * 视频绘制管理
 * Created by hzxuwen on 2015/5/6.
 */
public class AVChatSurface {

    private Context context;
    private AVChatUI manager;
    private View surfaceRoot;
    private Handler uiHandler;

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
            smallSizePreviewFrameLayout.setOnTouchListener(touchListener);

            largeSizePreviewLayout = (LinearLayout) surfaceRoot.findViewById(R.id.large_size_preview);
            largeSizePreviewCoverLayout = surfaceRoot.findViewById(R.id.notificationLayout);

            init = true;
            bitmapUtils = new BitmapUtils(context);
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

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
                    if(diff < TOUCH_SLOP)
                        break;

                    if(paddingRect == null) {
                        paddingRect = new Rect(ScreenUtil.dip2px(10), ScreenUtil.dip2px(20), ScreenUtil.dip2px(10),
                                ScreenUtil.dip2px(70));
                    }

                    int destX, destY;
                    if(x - inX <= paddingRect.left) {
                        destX = paddingRect.left;
                    } else if(x - inX + v.getWidth() >= ScreenUtil.screenWidth - paddingRect.right) {
                        destX = ScreenUtil.screenWidth - v.getWidth() - paddingRect.right;
                    } else {
                        destX = x - inX;
                    }

                    if(y - inY <= paddingRect.top) {
                        destY = paddingRect.top;
                    } else if(y - inY + v.getHeight() >= ScreenUtil.screenHeight - paddingRect.bottom){
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
                    if(Math.max(Math.abs(lastX - x), Math.abs(lastY - y)) <= 5){
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
            return true;
        }
    };

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
                break;
            case OUTGOING_VIDEO_CALLING:// 去电
               /* iv_meeting_ic_card.setVisibility(View.VISIBLE);
                iv_meeting_icon.setVisibility(View.VISIBLE);
                bitmapUtils.display(iv_meeting_ic_card, sp.getString(current_show == 1 ? "img_url_01" : "img_url_02", ""), new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                        Log.i("加载完成", "加载完成了");
                    }

                    @Override
                    public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                        iv_meeting_ic_card.setImageResource(R.drawable.ic_card);
                        Log.i("加载失败", "加载失败了");
                    }
                });
                bitmapUtils.display(iv_meeting_icon, sp.getString("img_url_03", ""), new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                        Log.i("头像加载完成", "加载完成了");
                    }

                    @Override
                    public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                        iv_meeting_ic_card.setImageResource(R.drawable.default_icon);
                        Log.i("头像加载失败", "加载失败了");
                    }
                });
                iv_meeting_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog dialog = new AlertDialog.Builder(context).create();
                        ImageView imgView = getView(sp.getString("img_url_03", ""));
                        dialog.setView(imgView);
                        Window dialogWindow = dialog.getWindow();
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                        /*
                         * lp.x与lp.y表示相对于原始位置的偏移.
                         * 当参数值包含Gravity.LEFT时,对话框出现在左边,所以lp.x就表示相对左边的偏移,负值忽略.
                         * 当参数值包含Gravity.RIGHT时,对话框出现在右边,所以lp.x就表示相对右边的偏移,负值忽略.
                         * 当参数值包含Gravity.TOP时,对话框出现在上边,所以lp.y就表示相对上边的偏移,负值忽略.
                         * 当参数值包含Gravity.BOTTOM时,对话框出现在下边,所以lp.y就表示相对下边的偏移,负值忽略.
                         * 当参数值包含Gravity.CENTER_HORIZONTAL时
                         * ,对话框水平居中,所以lp.x就表示在水平居中的位置移动lp.x像素,正值向右移动,负值向左移动.
                         * 当参数值包含Gravity.CENTER_VERTICAL时
                         * ,对话框垂直居中,所以lp.y就表示在垂直居中的位置移动lp.y像素,正值向右移动,负值向左移动.
                         * gravity的默认值为Gravity.CENTER,即Gravity.CENTER_HORIZONTAL |
                         * Gravity.CENTER_VERTICAL.
                         *
                         * 本来setGravity的参数值为Gravity.LEFT | Gravity.TOP时对话框应出现在程序的左上角,但在
                         * 我手机上测试时发现距左边与上边都有一小段距离,而且垂直坐标把程序标题栏也计算在内了,
                         * Gravity.LEFT, Gravity.TOP, Gravity.BOTTOM与Gravity.RIGHT都是如此,据边界有一小段距离
                         */
//                        lp.x = 100; // 新位置X坐标
//                        lp.y = 100; // 新位置Y坐标
                /*
                        lp.width = WindowManager.LayoutParams.WRAP_CONTENT; // 宽度
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
                        lp.alpha = 0.95f; // 透明度

                        // 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
                        // dialog.onWindowAttributesChanged(lp);
                        dialogWindow.setAttributes(lp);
                        dialog.show();
                        // 点击图片消失
                        imgView.setOnClickListener(new View.OnClickListener() {
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
                        dialog.setView(imgView);
                        Window dialogWindow = dialog.getWindow();
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
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
                                bitmapUtils.display(iv_meeting_ic_card, sp.getString(current_show == 1 ? "img_url_01" : "img_url_02", ""), new BitmapLoadCallBack<ImageView>() {
                                    @Override
                                    public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                                        Log.i("加载完成", "加载完成了2");
                                    }

                                    @Override
                                    public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                                        iv_meeting_ic_card.setImageResource(R.drawable.ic_card);
                                    }
                                });
                            }
                        });
                    }
                });
    */
                break;
            default:
                break;
        }
        setSurfaceRoot(CallStateEnum.isVideoMode(state));
    }

    /**
     * 获取view
     * @return
     */
    private ImageView getView(String img_url) {
        ImageView imgView = new ImageView(context);
        imgView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
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

        iv_meeting_ic_card.setVisibility(View.VISIBLE);
        iv_meeting_icon.setVisibility(View.VISIBLE);
        bitmapUtils.display(iv_meeting_ic_card, sp.getString(current_show == 1 ? "img_url_01" : "img_url_02", ""), new BitmapLoadCallBack<ImageView>() {
            @Override
            public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                Log.i("加载完成", "加载完成了");
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                iv_meeting_ic_card.setImageResource(R.drawable.ic_card);
                Log.i("加载失败", "加载失败了");
            }
        });
        bitmapUtils.display(iv_meeting_icon, sp.getString("img_url_03", ""), new BitmapLoadCallBack<ImageView>() {
            @Override
            public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                Log.i("头像加载完成", "加载完成了");
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                iv_meeting_ic_card.setImageResource(R.drawable.default_icon);
                Log.i("头像加载失败", "加载失败了");
            }
        });
        iv_meeting_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(context).create();
                ImageView imgView = getView(sp.getString("img_url_03", ""));
                dialog.setView(imgView);
                Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.BOTTOM | Gravity.LEFT);
                        /*
                         * lp.x与lp.y表示相对于原始位置的偏移.
                         * 当参数值包含Gravity.LEFT时,对话框出现在左边,所以lp.x就表示相对左边的偏移,负值忽略.
                         * 当参数值包含Gravity.RIGHT时,对话框出现在右边,所以lp.x就表示相对右边的偏移,负值忽略.
                         * 当参数值包含Gravity.TOP时,对话框出现在上边,所以lp.y就表示相对上边的偏移,负值忽略.
                         * 当参数值包含Gravity.BOTTOM时,对话框出现在下边,所以lp.y就表示相对下边的偏移,负值忽略.
                         * 当参数值包含Gravity.CENTER_HORIZONTAL时
                         * ,对话框水平居中,所以lp.x就表示在水平居中的位置移动lp.x像素,正值向右移动,负值向左移动.
                         * 当参数值包含Gravity.CENTER_VERTICAL时
                         * ,对话框垂直居中,所以lp.y就表示在垂直居中的位置移动lp.y像素,正值向右移动,负值向左移动.
                         * gravity的默认值为Gravity.CENTER,即Gravity.CENTER_HORIZONTAL |
                         * Gravity.CENTER_VERTICAL.
                         *
                         * 本来setGravity的参数值为Gravity.LEFT | Gravity.TOP时对话框应出现在程序的左上角,但在
                         * 我手机上测试时发现距左边与上边都有一小段距离,而且垂直坐标把程序标题栏也计算在内了,
                         * Gravity.LEFT, Gravity.TOP, Gravity.BOTTOM与Gravity.RIGHT都是如此,据边界有一小段距离
                         */
//                        lp.x = 100; // 新位置X坐标
//                        lp.y = 100; // 新位置Y坐标
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT; // 宽度
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
//                lp.height = DensityUtil.dip2px(context, 400); // 高度
                lp.alpha = 0.95f; // 透明度

                // 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
                // dialog.onWindowAttributesChanged(lp);
                dialogWindow.setAttributes(lp);
                dialog.show();
                // 点击图片消失
                imgView.setOnClickListener(new View.OnClickListener() {
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
                dialog.setView(imgView);
                Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
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
                        bitmapUtils.display(iv_meeting_ic_card, sp.getString(current_show == 1 ? "img_url_01" : "img_url_02", ""), new BitmapLoadCallBack<ImageView>() {
                            @Override
                            public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                                Log.i("加载完成", "加载完成了2");
                            }

                            @Override
                            public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                                iv_meeting_ic_card.setImageResource(R.drawable.ic_card);
                            }
                        });
                    }
                });
            }
        });
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
