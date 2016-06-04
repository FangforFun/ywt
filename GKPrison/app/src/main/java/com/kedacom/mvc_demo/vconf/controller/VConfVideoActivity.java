/**
 * @(#)VConfAideoActivity.java 2013-8-1 Copyright 2013 it.kedacom.com, Inc. All
 *                             rights reserved.
 */

package com.kedacom.mvc_demo.vconf.controller;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.hardware.Camera;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.gkzxhn.gkprison.avchat.AVChatExitCode;
import com.gkzxhn.gkprison.avchat.event.ExamineEvent;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.utils.tool.SPUtil;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.conf.bean.VConf;
import com.kedacom.mvc_demo.vconf.bean.LinkState;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.mvc_demo.vconf.modle.service.VideoCapServiceManager;
import com.kedacom.mvc_demo.vconf.ui.VConfFunctionView;
import com.kedacom.truetouch.mtc.EmMtModel;
import com.kedacom.truetouch.video.capture.VideoCapture;
import com.kedacom.truetouch.video.player.EGLConfigChooser;
import com.kedacom.truetouch.video.player.EGLContextFactory;
import com.kedacom.truetouch.video.player.EGLWindowSurfaceFactory;
import com.kedacom.truetouch.video.player.Renderer;
import com.kedacom.truetouch.video.player.Renderer.Channel;
import com.kedacom.truetouch.video.player.VidGestureDetector;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.pc.ui.layout.ISimpleTouchListener;
import com.pc.ui.layout.SimpleGestureDetectorFrame;
import com.pc.ui.layout.SimpleGestureDetectorRelative;
import com.pc.utils.StringUtils;
import com.pc.utils.TerminalUtils;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;

import de.greenrobot.event.EventBus;

/**
 * 视频会议
 * 
 * @author chenjian
 * @date 2013-8-1
 */

public class VConfVideoActivity extends ActionBarActivity implements View.OnClickListener {

	private final String TAG = "VConfVideoActivity";

	// 预览窗口标准值计算基数：标准屏幕480x800,标准小窗口203x254
	private final int standard_SW = 480;
	private final int standard_SH = 800;
	private final int standard_W = 203;
	private final int standard_H = 254;

	// 切换间隔时间2s
	private final long TOGGLE_INTERVAL_TIME = 2 * 1000;

	// 摄像头值
	private final int CAMERA_COUNT = 2;

	// e164
	private String mConfE164;
	// P2P呼叫时，没有VConf
	private VConf mVConf;

	// 点对点视频会议
	private boolean mP2PConfVideo;

	// 音频入会,true:音频入会, false:音频会议
	private boolean mJoinConfVideo;

	private MyBroadcastReceiver mReceiver;

	// 分辨率
	private short mResolution;

	private SurfaceView mPreSurfaceView;

	// 播放小窗口的装载布局
	private FrameLayout mPrePipPicFrame;
	// 画中画Frame
	private SimpleGestureDetectorFrame mPrePipFrame;
	// 播放大窗口的装载布局
	private SimpleGestureDetectorRelative mPlayPicFrame;

	// PreView GLSurfaceView
	private Renderer mMainRenderer;
	// 预览窗口，注意：只有关闭画中画时才能主动隐藏预览窗口，其余地方均保持VISIBLE，否则预览窗口可能被遮挡
	private GLSurfaceView mGlPreview;
	private ImageView mStaticPrepicImg;

	// PlayView GLSurfaceView
	private GLSurfaceView mGlPlayView;
	private Renderer mPreviewRenderer;
	private ImageView mStaticPlaypicImg;
	private View mSendingDesktopImg;

	// 前置窗口当前显示信道类型
	private Renderer.Channel mCurrPreChannel;
	// 主窗口当前显示信道类型
	private Renderer.Channel mCurrMainChannel;
	// 非双流时，前置窗口显示信道
	private Renderer.Channel mSinglePreChannel;

	// 双流
	private boolean mIsReceiveDual;
	// 正在接收双流
	private boolean mIsReceivingDual;

	private ImageView mCameraConvertImg;
	private ImageView mCameraOpenSwitchImg;

	// 屏幕宽高
	private int[] wh = null;

	// 当前屏幕方向是否为横屏
	private boolean mIsCurrScreenLandscape;

	// 上一次切换Channel时间
	private long mPreToogleChannelTime;
	// 上一次切换Camera时间
	private long mPreSwitchCameraTime;

	private boolean isOpenCamera = true;

	private VideoCapture mVideoCapture;

	private FrameLayout fl_examine;// 审核
	private LinearLayout ll_examine_button;
	private Button bt_through_examine;
	private Button bt_not_through_examine;

	private boolean isCommon = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}

		AppStackManager.Instance().pushActivity(this);

		initExtras();

		setContentView(R.layout.vconf_video_layout);
		findViews();
		initComponentValue();
		createListeners();
		registerReceivers();

		wh = TerminalUtils.terminalWH(getApplicationContext());
		isScreenLandscape();

		initPlayGLSurfaceView();
		initPreGLSurfaceView();
	}

	/**
	 * @see com.kedacom.mobilemvc.base.MBaseFragmentActivity#onViewCreated()
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		VideoConferenceService.setStreamVolumeModel(true, false);

		mVConf = new VConf();
		mVConf.setConfE164(mConfE164);
	}

	@Override
	protected void onStart() {
		super.onStart();

		initFacingPreviewSurfaceView();

		// 初始化采集图像
		// initialize video capture
		if (VideoCapServiceManager.getVideoCapServiceConnect() != null) {
			VideoCapServiceManager.getVideoCapServiceConnect().initVideoCapture(this);
		}

		// set automatic rotation correct mode for video capture
		VideoCapture.setAutoRotationCorrect(true);
	}

	@Override
	protected void onResume() {
		super.onResume();

		VideoConferenceService.mIsQuitAction = false;

		mPrePipFrame.bringToFront();
		mPrePipFrame.setVisibility(View.GONE);
		mIsReceiveDual = VideoConferenceService.isDualStrea();

		try {
			mResolution = 9;

			// 上报对端型号
			VideoCapture.setConfIsP2PMeeting(mP2PConfVideo);
			String emPeerMtModel = EmMtModel.emUnknownMtModel.toString();
			if (VideoConferenceService.mLinkState != null && VideoConferenceService.mLinkState.getEmPeerMtModel() != null) {
				emPeerMtModel = VideoConferenceService.mLinkState.getEmPeerMtModel().toString();
			}
			VideoCapture.setConfRemoteTerminal(emPeerMtModel);
			startVideoCapture(mResolution);

			initChannel();

			// 摄像头初始状态关闭
			setCameraState(true);
		} catch (Exception e) {
			Log.e("VConfVideoActivity", "start Video Capture", e);
		}

		if (null != mGlPlayView) {
			mGlPlayView.onResume();
		}
		if (null != mGlPreview) {
			mGlPreview.onResume();
		}

		computePreViewLayoutParams();
		computePipViewLayoutParams();
		computePlayViewLayoutParams();

		VideoConferenceService.mIsQuitAction = false;

		Log.i(TAG, "onResume");
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		registerPlayGLSurfaceListener();
		registerPreGLSurfaceViewListener();
		mVideoCapture = new VideoCapture();
	}

	/**
	 * 初始化传入参数数据
	 */
	private void initExtras() {
		Bundle extra = getIntent().getExtras();
		if (extra == null) {
			return;
		}
		if(extra.containsKey("isCommon")) {
			isCommon = extra.getBoolean("isCommon");
		}
		mP2PConfVideo = VideoConferenceService.isP2PVConf();
		mConfE164 = extra.getString(MyApplication.E164NUM);
	}

	protected void findViews() {
		mPrePipPicFrame = (FrameLayout) findViewById(R.id.pip_pic_frame);
		mPrePipFrame = (SimpleGestureDetectorFrame) findViewById(R.id.pip_frame);
		mPlayPicFrame = (SimpleGestureDetectorRelative) findViewById(R.id.pic_frame);
		mCameraConvertImg = (ImageView) findViewById(R.id.camera_convert_img);
		mCameraOpenSwitchImg = (ImageView) findViewById(R.id.camera_open_switchimg);
		fl_examine = (FrameLayout) findViewById(R.id.fl_examine);
		ll_examine_button = (LinearLayout) findViewById(R.id.ll_examine_button);
		bt_through_examine = (Button) findViewById(R.id.bt_through_examine);
		bt_not_through_examine = (Button) findViewById(R.id.bt_not_through_examine);

		if(TextUtils.isEmpty((String) SPUtil.get(this, "name", ""))){
			ll_examine_button.setVisibility(View.VISIBLE);
			bt_through_examine.setOnClickListener(this);
			bt_not_through_examine.setOnClickListener(this);
		}else {
			fl_examine.setVisibility(View.VISIBLE);
		}
	}

	public void initComponentValue() {
		mCameraOpenSwitchImg.setImageResource(R.drawable.camera_open);
	}

	protected void createListeners() {
		mCameraConvertImg.setOnClickListener(this);
		mCameraOpenSwitchImg.setOnClickListener(this);
	}

	/**
	 * 注册广播接收器
	 */
	private void registerReceivers() {
		mReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(VideoConferenceService.JOINCONF_ACTION);
		filter.addAction(VideoConferenceService.LINKSTATE_ACTION);
		filter.addAction(VideoConferenceService.RECVDUALSTATE_ACTION);
		filter.addAction(VideoConferenceService.REVEIVECONFINFO_ACTION);
		registerReceiver(mReceiver, filter);
	}

	/**
	 * 反注册广播接收器
	 */
	private void unregisterReceivers() {
		if (null == mReceiver) {
			return;
		}

		unregisterReceiver(mReceiver);
		mReceiver = null;
	}

	private void initChannel() {
		// 双流
		if (mIsReceiveDual) {
			if (mCurrPreChannel != null && mCurrPreChannel == Renderer.Channel.second) {
				setPrerendererChannel(Renderer.Channel.second);

				setMainrendererChannel(Renderer.Channel.first);
			} else {
				setPrerendererChannel(Renderer.Channel.first);

				setMainrendererChannel(Renderer.Channel.second);
			}

			return;
		}

		// 非双流
		if (mCurrPreChannel != null && mCurrPreChannel == Renderer.Channel.first) {
			setPrerendererChannel(Renderer.Channel.first);

			setMainrendererChannel(Renderer.Channel.preview);
		} else {
			setPrerendererChannel(Renderer.Channel.preview);

			setMainrendererChannel(Renderer.Channel.first);
		}
	}

	/**
	 * @see View.OnClickListener#onClick(View)
	 */
	@Override
	public void onClick(View v) {
		if (null == v) {
			return;
		}
		switch (v.getId()) {
		// 前后摄像头旋转
			case R.id.camera_convert_img:
				switchCamera();
				break;
			// 摄像头打开选择
			case R.id.camera_open_switchimg:
				toggleCamera();
				break;
			case R.id.bt_through_examine:
				HttpUtils httpUtils = new HttpUtils(5000);
				RequestParams params = new RequestParams();
				params.setContentType("application/json");
				try {
					StringEntity entity = new StringEntity("{" +
							"    \"notification\": {" +
							"        \"code\": 200," +
							"        \"receiver\": \"" + SPUtil.get(VConfVideoActivity.this, "family_accid", "") + "\"," +
							"        \"sender\": \"" + SPUtil.get(VConfVideoActivity.this, "token", "") + "\"" +
							"    }" +
							"}", HTTP.UTF_8);
					params.setBodyEntity(entity);
					com.gkzxhn.gkprison.utils.tool.Log.i(TAG, "through entity is :" + entity);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				httpUtils.send(HttpRequest.HttpMethod.POST, Constants.URL_HEAD + Constants.VIDEO_EXAMIME, params, new RequestCallBack<Object>() {
					@Override
					public void onSuccess(ResponseInfo<Object> responseInfo) {
						com.gkzxhn.gkprison.utils.tool.Log.i("审核通过成功", responseInfo.result.toString());
					}

					@Override
					public void onFailure(HttpException e, String s) {
						com.gkzxhn.gkprison.utils.tool.Log.i("审核通过失败", s);
						EventBus.getDefault().post(new ExamineEvent("发送审核状态异常"));
					}
				});
				break;
			case R.id.bt_not_through_examine:
				HttpUtils httpUtils1 = new HttpUtils(5000);
				RequestParams params1 = new RequestParams();
				params1.setContentType("application/json");
				try {
					StringEntity entity = new StringEntity("{" +
							"    \"notification\": {" +
							"        \"code\": 401," +
							"        \"receiver\": \"" + SPUtil.get(VConfVideoActivity.this, "family_accid", "") + "\"," +
							"        \"sender\": \"" + SPUtil.get(VConfVideoActivity.this, "token", "") + "\"" +
							"    }" +
							"}", HTTP.UTF_8);
					com.gkzxhn.gkprison.utils.tool.Log.i(TAG, "not through entity is :" + entity);
					params1.setBodyEntity(entity);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				httpUtils1.send(HttpRequest.HttpMethod.POST, Constants.URL_HEAD + Constants.VIDEO_EXAMIME, params1, new RequestCallBack<Object>() {
					@Override
					public void onSuccess(ResponseInfo<Object> responseInfo) {
						com.gkzxhn.gkprison.utils.tool.Log.i("审核不通过成功", responseInfo.result.toString());
						// 挂断
					}

					@Override
					public void onFailure(HttpException e, String s) {
						com.gkzxhn.gkprison.utils.tool.Log.i("审核不通过失败", s);
						EventBus.getDefault().post(new ExamineEvent("发送审核状态异常"));
					}
				});
				break;
			default:
				break;
		}
	}

	public void onEvent(final ExamineEvent examineEvent){
		if(examineEvent.getMsg().contains("发送审核状态异常")){
			Toast.makeText(VConfVideoActivity.this, "服务器异常", Toast.LENGTH_LONG).show();
		}else {
			Toast.makeText(VConfVideoActivity.this, examineEvent.getMsg(), Toast.LENGTH_LONG).show();
			fl_examine.setVisibility(View.GONE);
			ll_examine_button.setVisibility(View.GONE);
		}
	}

	/**
	 * 切换摄像头
	 *
	 * <pre>
	 * The facing of the camera is opposite to that of the screen.
	 * public static final int CAMERA_FACING_BACK = 0;
	 *
	 * The facing of the camera is the same as that of the screen.
	 * public static final int CAMERA_FACING_FRONT = 1;
	 * </pre>
	 */
	private void switchCamera() {
		if (VideoCapture.getCameraCount() < CAMERA_COUNT) {
			return;
		}

		long currTime = System.currentTimeMillis();

		// 切换间隔时间小于2s时，切换无效
		if ((currTime - mPreSwitchCameraTime) <= TOGGLE_INTERVAL_TIME) {
			return;
		}

		mPreSwitchCameraTime = currTime;

		VideoCapture.switchCamera();
	}

	/**
	 * 切换双流
	 *
	 * @param isReceiveDual
	 */
	private void switchDualStream(boolean isReceiveDual) {
		if (mIsReceiveDual == isReceiveDual) {
			return;
		}

		mIsReceiveDual = isReceiveDual;

		// 双流
		if (isReceiveDual) {
			setPrerendererChannel(Renderer.Channel.first);

			setMainrendererChannel(Renderer.Channel.second);
		} else {
			// 检测从双流切换回来时，上次前置窗口显示信道类型，如果上次是第一路信道类型，则切换回来是也应该是第一路类型
			if (mSinglePreChannel == Renderer.Channel.first) {
				setPrerendererChannel(Renderer.Channel.first);

				setMainrendererChannel(Renderer.Channel.preview);
			} else {
				setPrerendererChannel(Renderer.Channel.preview);

				setMainrendererChannel(Renderer.Channel.first);
			}
		}
	}

	/**
	 * 设置前置窗口显示信道
	 *
	 * @param channel
	 */
	private void setPrerendererChannel(Renderer.Channel channel) {
		if (null == mPreviewRenderer || null == channel) {
			return;
		}

		// in order to make this glpreview as a preview surface for
		// camera,
		// the best way to do is to set this renderer as the preview
		// renderer.
		mPreviewRenderer.setChannel(channel);
		mCurrPreChannel = channel;

		// 非双流时，记录前置窗口显示信道
		if (!mIsReceiveDual) {
			mSinglePreChannel = channel;
		}
	}

	/**
	 * 设置主窗口显示信道
	 *
	 * @param channel
	 */
	private void setMainrendererChannel(Renderer.Channel channel) {
		if (null == mMainRenderer || null == channel) {
			return;
		}

		// start drawings only after this activity lives on screen.
		mMainRenderer.setChannel(channel);
		mMainRenderer.refresh();
		mCurrMainChannel = channel;
	}

	/**
	 * 切换前后渲染图
	 *
	 * @param hideGLSurfaceView 是否在切换前检测隐藏GLSurfaceView和显示静态图片
	 */
	private void toggleRendererChannel(boolean hideGLSurfaceView) {
		// 双流
		if (mIsReceiveDual) {
			if (mCurrPreChannel == Renderer.Channel.first) {
				setPrerendererChannel(Renderer.Channel.second);

				setMainrendererChannel(Renderer.Channel.first);
			} else {
				setPrerendererChannel(Renderer.Channel.first);

				setMainrendererChannel(Renderer.Channel.second);
			}
		}

		// 非双流
		else {
			// 非双流且摄像头关闭时切换Channel之前先隐藏GLSurfaceView,
			// 否则在显示静态图片之前会残留上一帧的图片
			if (hideGLSurfaceView && !isOpenCamera) {
				mGlPlayView.setVisibility(View.GONE);
				mStaticPlaypicImg.setVisibility(View.VISIBLE);

				mGlPreview.setVisibility(View.GONE);
				mStaticPrepicImg.setVisibility(View.VISIBLE);
			}

			if (mCurrPreChannel == Renderer.Channel.preview) {
				setPrerendererChannel(Renderer.Channel.first);

				setMainrendererChannel(Renderer.Channel.preview);

			} else {
				setPrerendererChannel(Renderer.Channel.preview);

				setMainrendererChannel(Renderer.Channel.first);
			}
		}
	}

	/**
	 * 摄像头开关
	 */
	private void toggleCamera() {
		if (null == mCameraConvertImg) {
			return;
		}

		boolean isOpenCamera = !this.isOpenCamera;
		setCameraState(isOpenCamera);
	}

	/**
	 * 设置摄像头状态
	 *
	 * @param colse
	 *            关闭摄像头，发送静态图片
	 */
	private void setCameraState(boolean open) {
		isOpenCamera = open;

		if (open) {
			try {
				KdvMtBaseAPI.sendStaticPic(false);
			} catch (Exception e) {
			}

			mCameraOpenSwitchImg.setImageResource(R.drawable.camera_open);
		} else {
			try {
				KdvMtBaseAPI.sendStaticPic(true);
			} catch (Exception e) {
			}

			mCameraOpenSwitchImg.setImageResource(R.drawable.camera_close);
		}

		// 本地图片显示
		autoSwitchStaticPicVisibility();
	}

	/**
	 * 自动选择是否显示静态图片
	 */
	private void autoSwitchStaticPicVisibility() {
		if (isOpenCamera) {
			staticPicVisibility(false, false);
			return;
		}

		// 双流时，本端不显示静态图片
		if (mIsReceiveDual) {
			staticPicVisibility(false, false);

			return;
		}

		if (mCurrPreChannel == Channel.preview) {
			staticPicVisibility(false, true);
		} else if (mCurrMainChannel == Channel.preview) {
			staticPicVisibility(true, false);
		} else {
			staticPicVisibility(false, false);
		}
	}

	/**
	 * 显示静态图片
	 *
	 * <pre>
	 * 二者只能一个为true，否则均视为false
	 * </pre>
	 *
	 * @param visiblePlayView
	 *            显示主窗口静态图片
	 * @param visiblePreView
	 *            显示预览窗口静态图片
	 */
	private void staticPicVisibility(boolean visiblePlayView, boolean visiblePreView) {
		mSendingDesktopImg.setVisibility(View.GONE);

		// 显示主窗口静态图片
		if (visiblePlayView) {
			mGlPlayView.setVisibility(View.GONE);
			mStaticPlaypicImg.setVisibility(View.VISIBLE);

			mStaticPrepicImg.setVisibility(View.GONE);
			mGlPreview.setVisibility(View.VISIBLE);
			mGlPreview.bringToFront();
		}

		// 显示预览窗口静态图片
		else if (visiblePreView) {
			mGlPlayView.setVisibility(View.VISIBLE);
			mStaticPlaypicImg.setVisibility(View.GONE);

			mStaticPrepicImg.setVisibility(View.VISIBLE);
			mStaticPrepicImg.bringToFront();
		} else {
			mGlPlayView.setVisibility(View.VISIBLE);
			mStaticPlaypicImg.setVisibility(View.GONE);

			mStaticPrepicImg.setVisibility(View.GONE);
			mGlPreview.setVisibility(View.VISIBLE);
			mGlPreview.bringToFront();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		boolean isRestoreGlPreview = false;

		// 如果前置窗口显示的是预览图，且摄像头处于关闭状态，旋转之前先隐藏预览窗口
		if (mCurrPreChannel != null && mCurrPreChannel == Renderer.Channel.preview) {
			if (mGlPreview != null && mGlPreview.getVisibility() != View.GONE) {
				isRestoreGlPreview = true;
				mGlPreview.setVisibility(View.GONE);
			}
		}

		super.onConfigurationChanged(newConfig);

		wh = TerminalUtils.terminalWH(getApplicationContext());
		isScreenLandscape();

		try {
			reStartVideoCapture(mResolution);
		} catch (Exception e) {
		}

		computePreViewLayoutParams();
		computePipViewLayoutParams();
		computePlayViewLayoutParams();

		if (isRestoreGlPreview) {
			mGlPreview.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 是否为横屏
	 *
	 * @return
	 */
	private boolean isScreenLandscape() {
		if (wh == null || wh.length != 2) {
			return false;
		}

		if (wh[0] > wh[1]) {
			mIsCurrScreenLandscape = true;
		} else {
			mIsCurrScreenLandscape = false;
		}

		return mIsCurrScreenLandscape;
	}

	/**
	 * 初始化GLSurfaceView
	 *
	 * <pre>
	 * Small GLSurfaceView 前置播放窗口
	 * </pre>
	 */
	private void initPreGLSurfaceView() {
		if (mPrePipPicFrame == null) {
			return;
		}

		mGlPreview = (GLSurfaceView) mPrePipPicFrame.findViewById(R.id.gl_SV);
		mStaticPrepicImg = (ImageView) mPrePipPicFrame.findViewById(R.id.staticpic_Img);
		mStaticPrepicImg.setImageResource(R.drawable.staticpic_mini);
		mStaticPrepicImg.setVisibility(View.GONE);

		mGlPreview.setEGLConfigChooser(new EGLConfigChooser(8, 8, 8, 8, 0, 0));
		mGlPreview.setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory());
		mGlPreview.setEGLContextFactory(new EGLContextFactory());
		mGlPreview.getHolder().setFormat(PixelFormat.OPAQUE);

		mGlPreview.setEGLContextClientVersion(2);

		mPreviewRenderer = new Renderer();
		mGlPreview.setRenderer(mPreviewRenderer);

		mGlPreview.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
		mGlPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	/**
	 * 注册预览窗口的Listner
	 */
	private void registerPreGLSurfaceViewListener() {
		mPreviewRenderer.setListener(new Renderer.FrameListener() {

			public void onNewFrame() {
				mGlPreview.requestRender();
			}
		});

		mPrePipFrame.setOnSimpleTouchListener(new ISimpleTouchListener() {

			@Override
			public void onDown(View v, MotionEvent e) {
			}

			// 单击和双击时，首先触发onSingleTapUp
			@Override
			public void onSingleTapUp(View v, MotionEvent e) {
				// 切换间隔时间小于2s时，切换无效
				long currTime = System.currentTimeMillis();
				if ((currTime - mPreToogleChannelTime) <= TOGGLE_INTERVAL_TIME) {
					return;
				}

				// 已经不在会议中时，切换无效
				if (!VideoConferenceService.isCSVConf()) {
					return;
				}

				mPreToogleChannelTime = currTime;

				toggleRendererChannel(true);
				computePlayViewLayoutParams();
				autoSwitchStaticPicVisibility();
			}

			// 单击
			@Override
			public void onClick(View v) {
			}

			// 双击
			@Override
			public void onDoubleClick(View v) {
			}

			// 长按
			@Override
			public void onLongPress(View v) {
			}

			@Override
			public void onMove(View v, int dx, int dy) {
			}

			@Override
			public void onMoveScroll(View v, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			}

			@Override
			public void onUp(View v, MotionEvent e) {
			}
		});
	}

	/**
	 * 初始化GLSurfaceView *
	 *
	 * <pre>
	 * Big GLSurfaceView 播放大窗口
	 * </pre>
	 */
	private void initPlayGLSurfaceView() {
		if (mPlayPicFrame == null) {
			return;
		}

		mGlPlayView = (GLSurfaceView) mPlayPicFrame.findViewById(R.id.gl_SV);
		mStaticPlaypicImg = (ImageView) mPlayPicFrame.findViewById(R.id.staticpic_Img);
		mSendingDesktopImg = mPlayPicFrame.findViewById(R.id.sending_desktop_layout);
		mStaticPlaypicImg.setVisibility(View.GONE);
		mSendingDesktopImg.setVisibility(View.GONE);

		mGlPlayView.setEGLConfigChooser(new EGLConfigChooser(8, 8, 8, 8, 0, 0));
		mGlPlayView.setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory());
		mGlPlayView.setEGLContextFactory(new EGLContextFactory());
		mGlPlayView.getHolder().setFormat(PixelFormat.OPAQUE);
		// mGlPlayView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		mGlPlayView.setEGLContextClientVersion(2);

		mMainRenderer = new Renderer();
		mGlPlayView.setRenderer(mMainRenderer);

		// xiezhigang]]
		mGlPlayView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
		mGlPlayView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	/**
	 * 注册主窗口的Listner
	 */
	private void registerPlayGLSurfaceListener() {
		// configure gesture detectors
		mGestureGlPlayView = new VidGestureDetector();
		// for preview is on surface, only need listen touches of surface.
		mGlPlayView.setOnTouchListener(mGestureGlPlayView);
		// register onTap to surface view.
		mGestureGlPlayView.setOnTapListener(mGlPlayView, OnTapGlPlayViewListener);
		// register onDrag to surface view.
		mGestureGlPlayView.setOnDragListener(mGlPlayView, OnDragGlPlayViewListener);
		// // register renderer as scaling listener.
		// mGestureGlPlayView.setOnScaleListener(mGlPlayView,
		// mMainRenderer);
		mGestureGlPlayView.setOnScaleListener(mGlPlayView, OnScaleGlPlayViewListener);

		// [[xiezhigang
		mMainRenderer.setListener(new Renderer.FrameListener() {

			public void onNewFrame() {
				mGlPlayView.requestRender();

				// 正在接收双流
				if (mIsReceivingDual && !mMainRenderer.isEmptyFrame()) {
					mIsReceivingDual = false;

					mGlPlayView.postDelayed(new Runnable() {

						@Override
						public void run() {
							autoSwitchStaticPicVisibility();
							computePlayViewLayoutParams();
						}
					}, 2000);
				}
			}
		});
		// xiezhigang]]
	}

	/**
	 * 初始化PreviewSurfaceView
	 *
	 * <pre>
	 * preView是一个隐藏的SurfaceView,只用于采集图形
	 * </pre>
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initFacingPreviewSurfaceView() {
		FrameLayout preFacingView = (FrameLayout) findViewById(R.id.FacingPreview_layout);
		if (null == preFacingView) {
			return;
		}

		preFacingView.removeAllViews();
		mPreSurfaceView = new SurfaceView(this);
		// mPreSurfaceView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		preFacingView.addView(mPreSurfaceView);
		FrameLayout.LayoutParams flLP = (FrameLayout.LayoutParams) mPreSurfaceView.getLayoutParams();
		flLP.width = 10;
		flLP.height = 10;

		mPreSurfaceView.getHolder().setKeepScreenOn(true);
		// mPreView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mPreSurfaceView.setFocusable(false);
		mPreSurfaceView.setFocusableInTouchMode(false);
	}

	/**
	 * 计算用于采集的PreView的大小
	 */
	private void computePreViewLayoutParams() {
		if (null == mPreSurfaceView) {
			return;
		}

		FrameLayout.LayoutParams flLP = (FrameLayout.LayoutParams) mPreSurfaceView.getLayoutParams();
		flLP.width = 1;
		flLP.height = 1;
	}

	/**
	 * 计算画中画 LayoutParams
	 */
	private void computePipViewLayoutParams() {
		if (null == mPrePipFrame) {
			return;
		}

		if (null == wh || wh.length != 2 || wh[0] == 0) {
			return;
		}

		FrameLayout.LayoutParams flLP = (FrameLayout.LayoutParams) mPrePipFrame.getLayoutParams();
		int newW;
		int newH;
		if (wh[0] > wh[1]) {
			newW = wh[0] * standard_H / standard_SH;
			newH = wh[1] * standard_W / standard_SW;
		} else {
			newW = wh[0] * standard_W / standard_SW;
			newH = wh[1] * standard_H / standard_SH;
		}

		if ((newW & 1) != 0) {
			newW += 1;
		}

		if ((newH & 1) != 0) {
			newH += 1;
		}

		flLP.width = newW;
		flLP.height = newH;
	}

	/**
	 * 计算PalyView LayoutParams
	 */
	private void computePlayViewLayoutParams() {
		if (null == mGlPlayView) {
			return;
		}

		if (null == wh || wh.length != 2 || wh[0] == 0) {
			return;
		}

		RelativeLayout.LayoutParams flLP = (RelativeLayout.LayoutParams) mGlPlayView.getLayoutParams();
		if (null == flLP) {
			return;
		}

		// 点对点视频会议对端型号是否为Phone
		if (mP2PConfVideo && VideoConferenceService.mLinkState != null && VideoConferenceService.mLinkState.isPeerMtModelPhone()) {
			flLP.width = FrameLayout.LayoutParams.MATCH_PARENT;
			flLP.height = FrameLayout.LayoutParams.MATCH_PARENT;
			mGlPlayView.setLayoutParams(flLP);
			mGlPlayView.invalidate();

			return;
		}

		// 主窗口当前显示信道类型是预览图，则全屏显示
		if (mCurrMainChannel == Renderer.Channel.preview || mCurrMainChannel == Renderer.Channel.second) {
			flLP.width = FrameLayout.LayoutParams.MATCH_PARENT;
			flLP.height = FrameLayout.LayoutParams.MATCH_PARENT;
			mGlPlayView.setLayoutParams(flLP);

			// 摄像头关闭
			if (!isOpenCamera) {
				mGlPlayView.invalidate();
			} else {
				mGlPlayView.setVisibility(View.VISIBLE);
			}

			return;
		}

		// 横屏
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			flLP.width = FrameLayout.LayoutParams.MATCH_PARENT;
			flLP.height = FrameLayout.LayoutParams.MATCH_PARENT;
			mGlPlayView.setLayoutParams(flLP);
			mGlPlayView.invalidate();
		}

		// 竖屏
		else {
			int newH = wh[0] * 9 / 11;
			flLP.width = FrameLayout.LayoutParams.MATCH_PARENT;
			flLP.height = newH;
			mGlPlayView.setLayoutParams(flLP);
			mGlPlayView.invalidate();
		}
	}

	/**
	 * 开始采集图像
	 *
	 * @param prevHolder
	 * @param resolution
	 */
	private void startVideoCapture(short resolution) throws Exception {
		if (null == mPreSurfaceView) {
			return;
		}

		if (VideoCapServiceManager.getVideoCapServiceConnect() != null) {
			if (mIsCurrScreenLandscape) {
				VideoCapServiceManager.getVideoCapServiceConnect().startVideoCapture(mPreSurfaceView.getHolder(), mResolution, false);
			} else {
				VideoCapServiceManager.getVideoCapServiceConnect().startVideoCapture(mPreSurfaceView.getHolder(), mResolution, true);
			}
		}
	}

	/**
	 * 停止采集图像
	 */
	private void stopVideoCapture() {
		if (VideoCapServiceManager.getVideoCapServiceConnect() != null) {
			VideoCapServiceManager.getVideoCapServiceConnect().stopVideoCapture();
		}
	}

	/**
	 * 重新开始采集图像
	 *
	 * @param prevHolder
	 * @param resolution
	 */
	private void reStartVideoCapture(short resolution) {
		if (VideoCapServiceManager.getVideoCapServiceConnect() == null) {
			return;
		}

		if (mIsCurrScreenLandscape) {
			VideoCapServiceManager.getVideoCapServiceConnect().reStartVideoCapture(mPreSurfaceView.getHolder(), resolution, false);
		} else {
			VideoCapServiceManager.getVideoCapServiceConnect().reStartVideoCapture(mPreSurfaceView.getHolder(), resolution, true);
		}
	}

	@Override
	protected void onPause() {
		Log.e(TAG, "onPause");

		if (null != mGlPlayView) {
			mGlPlayView.onPause();
		}
		if (null != mGlPreview) {
			mGlPreview.onPause();
		}

		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		// 如果没有在会议中，停止采集数据
		if (VideoCapServiceManager.getVideoCapServiceConnect() != null) {
			VideoCapServiceManager.getVideoCapServiceConnect().stopVideoCapture();
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceivers();

		getVConfFunctionView().onDestroyView();

		// destroy all renderers.
		if (null != mMainRenderer) {
			mMainRenderer.setListener(null);
			mMainRenderer.destroy();
		}
		if (null != mPreviewRenderer) {
			mPreviewRenderer.setListener(null);
			mPreviewRenderer.destroy();
		}
		// xiezhigang]]

		// 如果没有在会议中，销毁采集模块
		if (VideoCapServiceManager.getVideoCapServiceConnect() != null) {
			VideoCapServiceManager.getVideoCapServiceConnect().destroyVideoCapture();
		}

		// 解绑服务
		VideoCapServiceManager.unBindService();

		AppStackManager.Instance().popActivity(this);

		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
	}

	/** @return the mP2PConfVideo */
	public boolean isP2PConfVideo() {
		return mP2PConfVideo;
	}

	/** @return the mJoinConfVideo */
	public boolean isJoinConfVideo() {
		return mJoinConfVideo;
	}

	/**
	 * @param mJoinConfVideo
	 *            the mJoinConfVideo to set
	 */
	public void setJoinConfVideo(boolean joinConfVideo) {
		this.mJoinConfVideo = joinConfVideo;
	}

	/** @return the mE164 */
	public String getE164() {
		return mConfE164;
	}

	/**
	 * 视频会议底部功能框
	 *
	 * @return
	 */
	public VConfFunctionView getVConfFunctionView() {
		return (VConfFunctionView) findViewById(R.id.vconfFunction_Frame);
	}

	class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}

			String action = intent.getAction();
			if (!StringUtils.equals(action, VideoConferenceService.JOINCONF_ACTION) && !StringUtils.equals(action, VideoConferenceService.LINKSTATE_ACTION)
					&& !StringUtils.equals(action, VideoConferenceService.REVEIVECONFINFO_ACTION) && !StringUtils.equals(action, VideoConferenceService.RECVDUALSTATE_ACTION)) {
				return;
			}

			Bundle b = intent.getExtras();
			if (b == null) {
				return;
			}

			boolean result = b.getBoolean(MyApplication.RESULT, false);
			final LinkState linkState = VideoConferenceService.mLinkState;

			// 未知数据，暂时不做处理
			if (StringUtils.equals(action, VideoConferenceService.JOINCONF_ACTION)) {
				return;
			}

			// linkState
			if (StringUtils.equals(action, VideoConferenceService.LINKSTATE_ACTION)) {
				if (linkState == null || !linkState.isVideo()) {
					return;
				}

				// 正在发起呼叫
				if (linkState.isCSCalling()) {
					return;
				}

				if (!result || linkState.isCSHanup()) {
					Toast.makeText(getApplicationContext(), R.string.vconf_joinConfVideo_failed, 0).show();
					finish();
					return;
				}

				// 空闲状态
				if (linkState.isCSIDLE() || linkState.isCSHanup()) {
					finish();
					return;
				}
				return;
			}

			if (StringUtils.equals(action, VideoConferenceService.REVEIVECONFINFO_ACTION)) {// 会议信息
				if (VideoConferenceService.isChairMan()) {// 主席

				}

				if (VideoConferenceService.isSpeaker()) {// 发言人

				}
				return;
			}

			// 双流
			if (StringUtils.equals(action, VideoConferenceService.RECVDUALSTATE_ACTION)) {
				boolean bIsReceiveDual = b.getBoolean("bIsReceiveDual");
				if (mIsReceiveDual == bIsReceiveDual) {
					return;
				}

				// final Handler handler = new Handler() {
				//
				// @Override
				// public void handleMessage(Message msg) {
				// super.handleMessage(msg);
				//
				// autoSwitchStaticPicVisibility();
				// computePlayViewLayoutParams();
				// }
				// };

				// 接收到双流
				if (bIsReceiveDual) {
					mIsReceivingDual = true;
					// 先显示接收双流缓冲界面
					mGlPlayView.setVisibility(View.GONE);
					mStaticPlaypicImg.setVisibility(View.GONE);
					mSendingDesktopImg.setVisibility(View.VISIBLE);

					// handler.sendEmptyMessageDelayed(2000, 4000);
				}

				switchDualStream(bIsReceiveDual);
				if (!bIsReceiveDual) {
					mIsReceivingDual = false;
					autoSwitchStaticPicVisibility();
					computePlayViewLayoutParams();
				}
				return;
			}
		}
	}

	/**
	 * 缩放预览
	 *
	 * @param progress
	 */
	private void zoomCamera(int progress) {
		if (progress < 0 || progress > 100) {
			return;
		}
		Camera.Parameters params = VideoCapture.getParameters();
		if (params == null) {
			return;
		}
		boolean zoomSurpport = params.isZoomSupported();
		if (!zoomSurpport) return;
		int maxZoom = params.getMaxZoom();
		int zoom = (int) (progress / 100.0 * maxZoom);
		int currZoom = params.getZoom();
		if (currZoom == zoom) return;

		// 以下注掉的代码需要谢志刚提供接口
		try {
			if (params.isSmoothZoomSupported()) {
				mVideoCapture.stopSmoothZoom();
				mVideoCapture.startSmoothZoom(zoom);
			} else {
				params.setZoom(zoom);
				VideoCapture.setParameters(params);
			}
		} catch (Exception e) {
		}
	}

	private VidGestureDetector mGestureGlPlayView;
	/**
	 * GlPlayView OnTapListener
	 */
	private final VidGestureDetector.OnTapListener OnTapGlPlayViewListener = new VidGestureDetector.OnTapListener() {

		@Override
		public void onTap(int tapCount, PointF point, long time) {
			if (tapCount == 1) {
				// single-tap to show/hide preview window.
				runOnUiThread(new Runnable() {

					public void run() {
					}
				});
			} else if (tapCount > 1 && mGestureGlPlayView.isHit(mGlPreview, point)) {
				// double-tap to switch camera.
				// VideoCapture.switchCamera();
			} else {
				if (null != mMainRenderer && mCurrMainChannel != Renderer.Channel.preview) {
					mMainRenderer.onTap(tapCount, point, time);
				}
			}
		}
	};

	/**
	 * GlPlayView OnScaleListener
	 */
	private final VidGestureDetector.OnScaleListener OnScaleGlPlayViewListener = new VidGestureDetector.OnScaleListener() {

		private boolean isDrag = true;

		@Override
		public void onScaleBegin(PointF arg0, PointF arg1, long arg2) {
			if (null == mMainRenderer || mCurrMainChannel == Renderer.Channel.preview) {
				isDrag = false;
			} else {
				isDrag = true;
			}

			if (isDrag) {
				mMainRenderer.onScaleBegin(arg0, arg1, arg2);
			}
		}

		@Override
		public void onScale(PointF arg0, PointF arg1, long arg2) {
			if (isDrag) {
				mMainRenderer.onScale(arg0, arg1, arg2);
			}
		}

		@Override
		public void onScaleEnd(PointF arg0, PointF arg1, long arg2) {
			if (isDrag) {
				mMainRenderer.onScaleEnd(arg0, arg1, arg2);
			}
		}
	};

	/**
	 * GlPlayView OnDragListener
	 */
	private final VidGestureDetector.OnDragListener OnDragGlPlayViewListener = new VidGestureDetector.OnDragListener() {

		private boolean m_bDragPreview = false; // whether dragging preview.
		private PointF m_pDragPreviewBegin = new PointF(); // start position of
															// dragging.
		private int m_nDragPreviewBeginLeft = 0; // begin left of preview.
		private int m_nDragPreviewBeginTop = 0; // begin top of preview.
		private int m_nDragPreviewBeginWidth = 0; // begin width of preview.
		private int m_nDragPreviewBeginHeight = 0; // begin height of preview.

		private boolean isDrag = true;

		@Override
		public void onDragBegin(PointF point, long time) {
			if (null == mMainRenderer || mCurrMainChannel == Renderer.Channel.preview) {
				isDrag = false;
			} else {
				isDrag = true;
			}

			if (mGestureGlPlayView.isHit(mGlPreview, point)) {
				/*
				 * // if start begin on preview, then drag preview.
				 * m_bDragPreview = true; m_pDragPreviewBegin.set(point);
				 * m_nDragPreviewBeginLeft = mGlPreview.getLeft();
				 * m_nDragPreviewBeginTop = mGlPreview.getTop();
				 * m_nDragPreviewBeginWidth = mGlPreview.getWidth();
				 * m_nDragPreviewBeginHeight = mGlPreview.getHeight();
				 */
			} else {
				// else drag renderer's video while playing.
				m_bDragPreview = false;
				if (isDrag) {
					mMainRenderer.onDragBegin(point, time);
				}
			}
		}

		@Override
		public void onDrag(PointF point, long time) {
			if (m_bDragPreview) {
				/*
				 * // finish the preview dragging. int dx = (int) (point.x -
				 * m_pDragPreviewBegin.x); int dy = (int) (point.y -
				 * m_pDragPreviewBegin.y); int new_x = m_nDragPreviewBeginLeft +
				 * dx; int new_y = m_nDragPreviewBeginTop + dy; int new_right =
				 * new_x + m_nDragPreviewBeginWidth; int new_bottom = new_y +
				 * m_nDragPreviewBeginHeight; mGlPreview.layout(new_x, new_y,
				 * new_right, new_bottom);
				 */
			} else {
				// complete the renderer's video dragging.
				if (isDrag) {
					mMainRenderer.onDrag(point, time);
				}
			}
		}

		@Override
		public void onDragEnd(PointF point, long time) {
			if (m_bDragPreview) {
				/*
				 * // yes, drags the preview. int dx = (int) (point.x -
				 * m_pDragPreviewBegin.x); int dy = (int) (point.y -
				 * m_pDragPreviewBegin.y); int new_x = m_nDragPreviewBeginLeft +
				 * dx; int new_y = m_nDragPreviewBeginTop + dy; int new_right =
				 * new_x + m_nDragPreviewBeginWidth; int new_bottom = new_y +
				 * m_nDragPreviewBeginHeight; mGlPreview.layout(new_x, new_y,
				 * new_right, new_bottom);
				 */
			} else {
				// Oh, drags the renderer's video while playing.
				if (isDrag) {
					mMainRenderer.onDragEnd(point, time);
				}
			}
		}

	};

}
