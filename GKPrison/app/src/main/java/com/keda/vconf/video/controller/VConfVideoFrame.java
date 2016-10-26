package com.keda.vconf.video.controller;

/**
 * @(#)VConfVideoContentFrame.java   2014-10-11
 * Copyright 2014  it.kedacom.com, Inc. All rights reserved.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

import com.kedacom.kdv.mt.api.Configure;
import com.kedacom.kdv.mt.constant.EmNativeConfType;
import com.gkzxhn.gkprison.R;
import com.keda.vconf.controller.VConfFunctionFragment;
import com.keda.vconf.controller.VConfVideoUI;
import com.keda.vconf.manager.VConferenceManager;
import com.keda.vconf.manager.VideoCapServiceManager;
import com.kedacom.truetouch.video.capture.VideoCapture;
import com.kedacom.truetouch.video.player.EGLConfigChooser;
import com.kedacom.truetouch.video.player.EGLContextFactory;
import com.kedacom.truetouch.video.player.EGLWindowSurfaceFactory;
import com.kedacom.truetouch.video.player.Renderer;
import com.kedacom.truetouch.video.player.Renderer.Channel;
import com.pc.utils.TerminalUtils;

/**
  * 视频会议界面
  * 
  * @author chenj
  * @date 2014-10-11
  */

public class VConfVideoFrame extends Fragment implements View.OnClickListener {

	// 切换间隔时间2s
	private final long TOGGLE_INTERVAL_TIME = 2 * 1000;

	// 摄像头值
	private final int CAMERA_COUNT = 2;

	// 预览窗口标准值计算基数：标准屏幕480x800,标准小窗口203x254
	private final int standard_SW = 480;
	private final int standard_SH = 800;
	private final int standard_W = 203;
	private final int standard_H = 254;

	// 视频会议管理界面
	private VConfVideoUI mVConfVideoUI;
	private VConfFunctionFragment mBottomFunctionFragment;

	private SurfaceView mPreSurfaceView;

	// 画中画Frame
	private FrameLayout mPrePipFrame;

	// 预览窗口，注意：只有关闭画中画时才能主动隐藏预览窗口，其余地方均保持VISIBLE，否则预览窗口可能被遮挡
	private GLSurfaceView mGlPreview;
	private ImageView mStaticPrepicImg;
	private Renderer mPreviewRenderer;

	private VideoCapture mVideoCapture;

	// 需要重新设置画中画的位置
	private boolean mIsRsetPipFramePosition;

	// 前置窗口当前显示信道类型
	private Renderer.Channel mCurrPreChannel;
	// 主窗口当前显示信道类型
	private Renderer.Channel mCurrMainChannel;
	// 非双流时，前置窗口显示信道
	private Renderer.Channel mSinglePreChannel;

	// 上一次切换Camera时间
	private long mPreSwitchCameraTime;

	// 屏幕宽高
	private int[] wh = null;

	private boolean mIsMoveFlag;
	private int mLlyout, mTlayout, mRlayout, mBlayout;

	// 计时器
	private Chronometer mChronometer;

	// 预览窗口的Margin初始属性
	private int mPipframeLmargin;
	private int mPipframeTmargin;
	private int mPipframeRmargin;
	private int mPipframeBmargin;

	// 打开画中画按钮的初始 margin值
	private int mPipopenImgLmargin;
	private int mPipopenImgTmargin;
	private int mPipopenImgRmargin;
	private int mPipopenImgBmargin;

	private ImageView cameraOpenSwitchImg;
	private boolean isOpenCamera = true;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Log.i("VConfVideo", "VconfVideoFrame-->onAttach ");

		mVConfVideoUI = (VConfVideoUI) getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("VConfVideo", "VconfVideoFrame-->onCreate ");
	}

	/**
	 *
	 * @see com.pc.app.v4fragment.PcAbsFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i("VConfVideo", "VconfVideoFrame-->onCreateView ");

		View view = inflater.inflate(R.layout.vconf_video_content, null);

		mBottomFunctionFragment = new VConfFunctionFragment();
		getFragmentManager().beginTransaction().replace(R.id.bottomFunction_Frame, mBottomFunctionFragment).commitAllowingStateLoss();
		return view;
	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.i("VConfVideo", "VconfVideoFrame-->onViewCreated ");
		findViews();
		initComponentValue();
		registerListeners();
		super.onViewCreated(view, savedInstanceState);

		wh = TerminalUtils.terminalWH(getActivity());
		initPreGLSurfaceView();
		computePipViewLayoutParams();
		// 存储本地音视频入会类型为：视频会议
		VConferenceManager.nativeConfType = EmNativeConfType.VIDEO;
	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onStart()
	 */
	@Override
	public void onStart() {
		Log.i("VConfVideo", "VconfVideoFrame-->onStart ");
		super.onStart();

		initFacingPreviewSurfaceView();
		// initialize video capture
		if (VideoCapServiceManager.getVideoCapServiceConnect() != null) {
			VideoCapServiceManager.getVideoCapServiceConnect().initVideoCapture();
		}
		// set automatic rotation correct mode for video capture
		VideoCapture.setAutoRotationCorrect(true);
	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onResume()
	 */
	@Override
	public void onResume() {
		Log.i("VConfVideo", "VconfVideoFrame-->onResume ");
		super.onResume();
		mPrePipFrame.bringToFront();

		try {
			// 上报对端型号
			VideoCapture.setConfIsP2PMeeting(mVConfVideoUI.ismIsP2PConf());
			VideoCapture.setConfRemoteTerminal(VConferenceManager.getEmPeerMtModel4Media());
			// 先检测是否正在后天采集数据，如果是，先停止再开始
			reStartVideoCapture();

			initCamera();
			// initChannel();

			// 摄像头初始状态关闭
			// setCameraState(new MtVConfInfo().isOpenCamera(true));
		} catch (Exception e) {
		}

		if (null != mGlPreview) {
			mGlPreview.onResume();
		}

		if (mPipframeBmargin == 0) {
			FrameLayout.LayoutParams lp = (LayoutParams) mPrePipFrame.getLayoutParams();
			mPipframeLmargin = lp.leftMargin;
			mPipframeTmargin = lp.topMargin;
			mPipframeRmargin = lp.rightMargin;
			mPipframeBmargin = lp.bottomMargin;
		}

		/*		if (mPipopenImgBmargin == 0) {
					RelativeLayout.LayoutParams rlLP = (RelativeLayout.LayoutParams) getView().findViewById(R.id.pip_open_img).getLayoutParams();
					mPipopenImgLmargin = rlLP.leftMargin;
					mPipopenImgTmargin = rlLP.topMargin;
					mPipopenImgRmargin = rlLP.rightMargin;
					mPipopenImgBmargin = rlLP.bottomMargin;
				}
		*/
		registerPreGLSurfaceViewListener();

		mVideoCapture = new VideoCapture();

	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onSaveInstanceState(android.os.Bundle)
	 */

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.i("VConfVideo", "VconfVideoFrame-->onSaveInstanceState ");
		super.onSaveInstanceState(outState);
	}

	/**
	 * 视频内容
	 *
	 * @param tfragment
	 */
	public void replaceContentFrame(Fragment tfragment) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.video_frame, tfragment);
		ft.commitAllowingStateLoss();
	}

	public void findViews() {
		mPrePipFrame = (FrameLayout) getView().findViewById(R.id.pip_frame);
		cameraOpenSwitchImg = (ImageView) getView().findViewById(R.id.camera_open_switchimg);
	}

	public void initComponentValue() {
		// getView().findViewById(R.id.pip_open_img).setVisibility(View.GONE);
		cameraOpenSwitchImg.setImageResource(R.drawable.vconf_camera_open_selector);
	}

	/**
	 * 初始化PreviewSurfaceView
	 *
	 * <pre>
	 * preView是一个隐藏的SurfaceView,只用于采集图形
	 * </pre>
	 */
	private void initFacingPreviewSurfaceView() {
		FrameLayout preFacingView = (FrameLayout) getView().findViewById(R.id.FacingPreview_layout);
		if (null == preFacingView) {
			return;
		}

		preFacingView.removeAllViews();
		mPreSurfaceView = new SurfaceView(getActivity());
		preFacingView.addView(mPreSurfaceView);
		FrameLayout.LayoutParams flLP = (FrameLayout.LayoutParams) mPreSurfaceView.getLayoutParams();
		flLP.width = 1;
		flLP.height = 1;

		mPreSurfaceView.getHolder().setKeepScreenOn(true);
		mPreSurfaceView.setFocusable(false);
		mPreSurfaceView.setFocusableInTouchMode(false);
	}

	/**
	 * FacingView remove all views
	 */
	public void removeFacingView() {
		FrameLayout preFacingView = (FrameLayout) getView().findViewById(R.id.FacingPreview_layout);
		if (null == preFacingView) return;
		preFacingView.removeAllViews();
	}

	/**
	 * 初始化摄像头
	 */
	private void initCamera() {
		int currCameraId = VideoCapture.getCameraId();

		// 只有一个摄像头
		if (VideoCapture.getCameraCount() < CAMERA_COUNT) {
		}
		// 设备摄像头有2个
		else if (null == VConferenceManager.mIsCameraFront) {
			if (currCameraId != Camera.CameraInfo.CAMERA_FACING_FRONT) {
				VideoCapture.switchCamera();
				VConferenceManager.mIsCameraFront = false;
			} else {
				VConferenceManager.mIsCameraFront = true;
			}
		}
	}

	/**
	 * init Channel
	 */
	protected void initChannel() {
		// 双流
		if (VConferenceManager.isDualStream) {
			if (mCurrPreChannel != null && mCurrPreChannel == Renderer.Channel.second) {
				setMainRendererChannel(Renderer.Channel.first);
				setPreviewRendererChannel(Renderer.Channel.second);
			} else {
				setMainRendererChannel(Renderer.Channel.second);
				setPreviewRendererChannel(Renderer.Channel.first);
			}
		} else {
			// 非双流
			if (mCurrPreChannel != null && mCurrPreChannel == Renderer.Channel.first) {
				setMainRendererChannel(Renderer.Channel.preview);
				setPreviewRendererChannel(Renderer.Channel.first);
			} else {
				setMainRendererChannel(Renderer.Channel.first);
				setPreviewRendererChannel(Renderer.Channel.preview);
			}
		}
		keepAspectRatio();

	}

	/**
	 * 设置摄像头状态
	 *
	 * @param colse 关闭摄像头，发送静态图片
	 */
	protected void setCameraState(boolean open) {
		isOpenCamera = open;
		if (open && null != VConferenceManager.currTMtCallLinkSate) {
			Configure.setStaticPicCfgCmd(false);
			cameraOpenSwitchImg.setImageResource(R.drawable.vconf_camera_open_selector);
		} else if (null != VConferenceManager.currTMtCallLinkSate) {
			Configure.setStaticPicCfgCmd(true);
			cameraOpenSwitchImg.setImageResource(R.drawable.vconf_camera_close_selector);
		}
		// 本地图片显示
		autoSwitchStaticPicVisibility();
	}

	/**
	 * 初始化GLSurfaceView
	 *
	 * <pre>
	 * Small GLSurfaceView 前置播放窗口
	 * </pre>
	 */
	private void initPreGLSurfaceView() {
		Log.i("VConfVideo", "VconfVideoFrame-->initPreGLSurfaceView ");
		// 播放小窗口的装载布局
		FrameLayout prePipPicFrame = (FrameLayout) getView().findViewById(R.id.pip_pic_frame);
		if (prePipPicFrame == null) {
			return;
		}

		mGlPreview = (GLSurfaceView) prePipPicFrame.findViewById(R.id.gl_SV);
		mStaticPrepicImg = (ImageView) prePipPicFrame.findViewById(R.id.staticpic_Img);
		mStaticPrepicImg.setImageResource(R.drawable.camera_big);
		mStaticPrepicImg.setVisibility(View.GONE);

		mGlPreview.setZOrderMediaOverlay(true);
		mGlPreview.setKeepScreenOn(true);

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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * @return the mCurrMainChannel
	 */
	public Renderer.Channel getCurrMainChannel() {
		return mCurrMainChannel;
	}

	/**
	 * 设置主窗口显示信道
	 *
	 * @param channel
	 */
	private void setMainRendererChannel(Renderer.Channel channel) {
		VConfVideoPlayFrame playFrame = null;
		Fragment currFragment = mVConfVideoUI.getCurrFragmentView();
		if (null != currFragment && (currFragment instanceof VConfVideoPlayFrame)) {
			playFrame = (VConfVideoPlayFrame) currFragment;
		}

		if (null != playFrame) {
			playFrame.setMainRendererChannel(channel);
		}
		mCurrMainChannel = channel;
	}

	/**
	 * 设置前置窗口显示信道
	 *
	 * @param channel
	 */
	private void setPreviewRendererChannel(Renderer.Channel channel) {
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
		if (!VConferenceManager.isDualStream) {
			mSinglePreChannel = channel;
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

		// 切换间隔时间小于2s时，切换无效
		if ((System.currentTimeMillis() - mPreSwitchCameraTime) <= TOGGLE_INTERVAL_TIME) {
			return;
		}

		VideoCapture.switchCamera();
		mPreSwitchCameraTime = System.currentTimeMillis();
		if (VideoCapture.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			// new MtVConfInfo().putCameraFacingFront(true);
			VConferenceManager.mIsCameraFront = true;
		} else {
			// new MtVConfInfo().putCameraFacingFront(false);
			VConferenceManager.mIsCameraFront = false;
		}
	}

	/**
	 * 摄像头开关
	 */
	private void toggleCamera() {
		isOpenCamera = !isOpenCamera;
		setCameraState(isOpenCamera);
	}

	/**
	 * 操作切换双流
	 * Note:只有在视频播放界面时才有效
	 *
	 * @param isReceiveDual
	 */
	public void switchDualStreamCtrl(boolean isReceiveDual) {
		Fragment currFragment = mVConfVideoUI.getCurrFragmentView();
		if (null == currFragment || !(currFragment instanceof VConfVideoPlayFrame)) {
			return;
		}

		GLSurfaceView glPlayView = getGlPlayView();
		if (null == glPlayView) {
			return;
		}

		// 接收到双流时，检测是否已经有双流显示，如果已经显示，则不做任何处理
		if (isReceiveDual && (mCurrMainChannel == Renderer.Channel.second || mCurrPreChannel == Renderer.Channel.second)) {
			return;
		}

		// 非双流时，检测是否已经有双流显示，如果没有，则不做任何处理
		if (!isReceiveDual && (mCurrMainChannel != Renderer.Channel.second && mCurrPreChannel != Renderer.Channel.second)) {
			return;
		}

		// 接收到双流
		if (isReceiveDual) {
			((VConfVideoPlayFrame) currFragment).setReceivingDual(true);

			// 先显示接收双流缓冲界面
			glPlayView.setVisibility(View.GONE);
			((VConfVideoPlayFrame) currFragment).getStaticPlaypicImg().setVisibility(View.GONE);

			resetPipFramePosition();
		}

		switchDualStream(isReceiveDual);

		if (!isReceiveDual) {
			((VConfVideoPlayFrame) currFragment).setReceivingDual(false);

			autoSwitchStaticPicVisibility();
			((VConfVideoPlayFrame) currFragment).computePlayViewLayoutParams(true, true);
		}
	}

	/**
	 * 切换双流
	 *
	 * @param isReceiveDual
	 */
	private void switchDualStream(boolean isReceiveDual) {
		// 双流
		if (isReceiveDual) {
			setPreviewRendererChannel(Renderer.Channel.first);

			setMainRendererChannel(Renderer.Channel.second);
		} else {
			// 检测从双流切换回来时，上次前置窗口显示信道类型，如果上次是第一路信道类型，则切换回来是也应该是第一路类型
			if (mSinglePreChannel == Renderer.Channel.first) {
				setPreviewRendererChannel(Renderer.Channel.first);

				setMainRendererChannel(Renderer.Channel.preview);
			} else {
				setPreviewRendererChannel(Renderer.Channel.preview);

				setMainRendererChannel(Renderer.Channel.first);
			}
		}
		keepAspectRatio();
	}

	/**
	 * 自动选择是否显示静态图片
	 */
	protected void autoSwitchStaticPicVisibility() {
		if (isOpenCamera) {
			staticPicVisibility(false, false);
			return;
		}

		// 双流时，本端不显示静态图片
		if (VConferenceManager.isDualStream) {
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
	 * 	二者只能一个为true，否则均视为false
	 * </pre>
	 * @param visiblePlayView 显示主窗口静态图片
	 * @param visiblePreView 显示预览窗口静态图片
	 */
	private void staticPicVisibility(boolean visiblePlayView, boolean visiblePreView) {
		View sendingDesktopImg = null;
		GLSurfaceView glPlayView = null;
		ImageView staticPlaypicImg = null;
		Fragment currFragment = mVConfVideoUI.getCurrFragmentView();
		if (null != currFragment && currFragment instanceof VConfVideoPlayFrame) {
			glPlayView = ((VConfVideoPlayFrame) currFragment).getPlayGLSurfaceView();
			// sendingDesktopImg = ((VConfVideoPlayFrame) currFragment).getSendingDesktopImg();
			staticPlaypicImg = ((VConfVideoPlayFrame) currFragment).getStaticPlaypicImg();
		}

		if (null != sendingDesktopImg) {
			sendingDesktopImg.setVisibility(View.GONE);
		}

		// 显示主窗口静态图片
		if (visiblePlayView) {
			if (null != glPlayView) {
				glPlayView.setVisibility(View.GONE);
			}
			if (null != staticPlaypicImg) {
				staticPlaypicImg.setVisibility(View.VISIBLE);
			}

			mStaticPrepicImg.setVisibility(View.GONE);
			// 关闭画中画图标隐藏
			mGlPreview.setVisibility(View.VISIBLE);
			mGlPreview.bringToFront();
		}
		// 显示预览窗口静态图片
		else if (visiblePreView) {
			if (null != glPlayView) {
				glPlayView.setVisibility(View.VISIBLE);
			}
			if (null != staticPlaypicImg) {
				staticPlaypicImg.setVisibility(View.GONE);
			}

			mStaticPrepicImg.setVisibility(View.VISIBLE);
			mStaticPrepicImg.bringToFront();
		} else {
			if (null != glPlayView) {
				glPlayView.setVisibility(View.VISIBLE);
			}
			if (null != staticPlaypicImg) {
				staticPlaypicImg.setVisibility(View.GONE);
			}

			mStaticPrepicImg.setVisibility(View.GONE);
			// 关闭画中画图标隐藏
			mGlPreview.setVisibility(View.VISIBLE);
			mGlPreview.bringToFront();
		}

		resetPipFramePosition();
	}

	/**
	 * 重新设置画中画的位置
	 */
	private void resetPipFramePosition() {
		// 重新设置画中画的位置
		if (!mIsRsetPipFramePosition || null == mPrePipFrame) {
			return;
		}

		// 设置过一次之后不需要再重新设置了
		mIsRsetPipFramePosition = false;
		int[] wh = TerminalUtils.terminalWH(getActivity());
		int statusBarHeight = 0;
		// 全屏时不计算状态栏
		// statusBarHeight = TerminalUtils.getStatusBarHeight(getActivity());

		int right = wh[0] - mRlayout;
		int bottom = wh[1] - statusBarHeight - mBlayout;

		// 判断右边距离是否超出屏幕
		if (right > (wh[0] - mPrePipFrame.getWidth())) {
			right = wh[0] - mPrePipFrame.getWidth();
		}

		// 判断底部距离是否超出屏幕
		if (bottom > (wh[1] - statusBarHeight - mPrePipFrame.getHeight())) {
			bottom = wh[1] - statusBarHeight - mPrePipFrame.getHeight();
		}

		FrameLayout.LayoutParams lp = (LayoutParams) mPrePipFrame.getLayoutParams();
		lp.setMargins(mPipframeLmargin, mPipframeTmargin, right, bottom);
		mPrePipFrame.setLayoutParams(lp);
		mPrePipFrame.postInvalidate();
	}

	/**
	 * 设置画中画显示隐藏
	 *
	 * @param visible
	 */
	protected void setVisibilityPip(boolean visible) {
		// 还原移动画中画坐标值
		mLlyout = mTlayout = mRlayout = mBlayout = 0;
		mIsRsetPipFramePosition = false;

		// 显示画中画
		if (visible) {
			/*	// 打开画中画图标
				if (null != getView()) {
					getView().findViewById(R.id.pip_open_img).setVisibility(View.GONE);
				}*/
			// 画中画Frame
			mPrePipFrame.setVisibility(View.VISIBLE);

			autoSwitchStaticPicVisibility();
			FrameLayout.LayoutParams lp = (LayoutParams) mPrePipFrame.getLayoutParams();
			if (lp.leftMargin == mPipframeLmargin && lp.topMargin == mPipframeTmargin && lp.rightMargin == mPipframeRmargin
					&& (lp.bottomMargin == mPipframeBmargin || lp.bottomMargin == 0)) {
			}
		}
		// 关闭画中画，需要主动隐藏预览窗口
		else {
			// 画中画Frame
			mPrePipFrame.setVisibility(View.GONE);
			// 预览窗口
			mGlPreview.setVisibility(View.GONE);
			// // 打开画中画图标
			// getView().findViewById(R.id.pip_open_img).setVisibility(View.VISIBLE);

			int pipopenImgBmargin = mPipopenImgBmargin;
			if (getBottomFunctionFragmentView().getVisibility() == View.GONE) {
				pipopenImgBmargin = 0;
			}
			/*	RelativeLayout.LayoutParams rlLP = (RelativeLayout.LayoutParams) getView().findViewById(R.id.pip_open_img).getLayoutParams();
				rlLP.setMargins(mPipopenImgLmargin, mPipopenImgTmargin, mPipopenImgRmargin, pipopenImgBmargin);
				getView().findViewById(R.id.pip_open_img).setLayoutParams(rlLP);*/
		}
	}

	/**
	 * 计算画中画 LayoutParams
	 */
	private void computePipViewLayoutParams() {
		if (null == mPrePipFrame) {
			return;
		}

		int[] wh = TerminalUtils.terminalWH(getActivity());

		if (null == wh || wh.length != 2 || wh[0] == 0) {
			return;
		}
		Log.w("VConfVideo", wh[0] + "VconfVideoFrame  " + wh[1]);
		// (i & 1)
		FrameLayout.LayoutParams flLP = (LayoutParams) mPrePipFrame.getLayoutParams();
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
		Log.w("VConfVideo", newW + "VconfVideoFrame  " + newH);
		flLP.width = newW / 2;
		flLP.height = newH / 2;
		mPrePipFrame.setLayoutParams(flLP);
	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onPause()
	 */
	@Override
	public void onPause() {
		Log.w("VConfVideo", "VconfVideoFrame-->onPause ");

		if (null != mGlPreview) {
			mGlPreview.onPause();
		}

		super.onPause();
	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onStop()
	 */
	@Override
	public void onStop() {
		Log.w("VConfVideo", "VconfVideoFrame-->onStop ");
		super.onStop();

		stopVideoCapture();

		removeFacingView();
	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onDetach()
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		Log.w("VConfVideo", "VconfVideoFrame-->onDetach ");
	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();

		Log.w("VConfVideo", "VconfVideoFrame-->onDestroyView ");
	}

	/**
	 * @see com.pc.app.v4fragment.PcAbsFragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.w("VConfVideo", "VconfVideoFrame-->onDestroy ");
		getBottomFunctionFragment().onDestroyView();

		destroyPreviewRenderer();
		// 如果没有在会议中，销毁采集模块
		if (VideoCapServiceManager.getVideoCapServiceConnect() != null) {
			VideoCapServiceManager.getVideoCapServiceConnect().destroyVideoCapture();
		}

		VideoCapServiceManager.unBindService();

		super.onDestroy();
	}

	/**
	 * destroy PreviewRenderer
	 */
	private void destroyPreviewRenderer() {
		if (null == mPreviewRenderer) {
			return;
		}

		mPreviewRenderer.setListener(null);
		if (null != mVConfVideoUI.getVConfContentFrame().getCurrMainChannel()) {
			mPreviewRenderer.destroy();
		}
	}

	public void registerListeners() {
		getView().findViewById(R.id.camera_convert_img).setOnClickListener(this);
		getView().findViewById(R.id.camera_open_switchimg).setOnClickListener(this);

	}

	/**
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
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

			default:
				break;
		}
	}

	/**
	 * 注册预览窗口的Listner
	 */
	private void registerPreGLSurfaceViewListener() {
		if (null != mPreviewRenderer && null == mPreviewRenderer.getListener()) {
			mPreviewRenderer.setListener(new Renderer.FrameListener() {

				public void onNewFrame() {
					mGlPreview.requestRender();
				}
			});
		}
	}

	/**
	 * 预览播放窗口
	 *
	 * @return
	 */
	protected GLSurfaceView getGlPreview() {
		return mGlPreview;
	}

	/**
	 * 视频播放界面
	 *
	 * @return
	 */
	private GLSurfaceView getGlPlayView() {
		GLSurfaceView glPlayView = null;
		Fragment currFragment = mVConfVideoUI.getCurrFragmentView();
		if (null != currFragment && currFragment instanceof VConfVideoPlayFrame) {
			glPlayView = ((VConfVideoPlayFrame) currFragment).getPlayGLSurfaceView();
		}

		return glPlayView;
	}

	/**
	 * 顶部工具栏
	 *
	 * @return
	 */
	public View getTopVConfFunctionView() {
		return getView().findViewById(R.id.topVConfFunctionView);
	}

	/**
	 * 底部工具栏（VConfFunctionFragment）
	 *
	 * @return
	 */
	public VConfFunctionFragment getBottomFunctionFragment() {
		// if (null == mBottomFunctionFragment) {
		// mBottomFunctionFragment = (VConfFunctionFragment)
		// getFragmentManager().findFragmentById(R.id.bottomFunction_Frame);
		// }
		return mBottomFunctionFragment;
	}

	/**
	 * 底部工具栏（View）
	 *
	 * @return
	 */
	public View getBottomFunctionFragmentView() {
		// return getBottomFunctionFragment().getView();
		return getView().findViewById(R.id.bottomFunction_Frame);
	}

	/**
	 * 停止采集图像
	 */
	public void stopVideoCapture() {
		if (VideoCapServiceManager.getVideoCapServiceConnect() == null) {
			return;
		}

		VideoCapServiceManager.getVideoCapServiceConnect().stopVideoCapture();
	}

	/**
	 * 重新开始采集图像
	 *
	 * @param prevHolder
	 * @param resolution
	 */
	public void reStartVideoCapture() {
		if (VideoCapServiceManager.getVideoCapServiceConnect() == null) {
			return;
		}

		VideoCapServiceManager.getVideoCapServiceConnect().reStartVideoCapture(mPreSurfaceView.getHolder(), !isScreenLandscape());
	}

	/**
	 * 是否为横屏
	 * @return
	 */
	private boolean isScreenLandscape() {
		int[] wh = TerminalUtils.terminalWH(getActivity());
		if (null == wh || wh.length != 2 || wh[0] == 0) {
			return false;
		}

		if (wh == null || wh.length != 2) {
			return false;
		}

		return wh[0] > wh[1];
	}

	/**
	 * 解决两画面合成 黑边的问题
	 */
	public void keepAspectRatio() {
		if (null == mPreviewRenderer) {
			return;
		}
		if (VConferenceManager.isVmpStyle1X2()) {
			{
				Log.w("VConfVideoFrame", " 两画面合成：mCurrPreChannel == Renderer.Channel.first？==>"
						+ (mCurrPreChannel == Renderer.Channel.first)
						+ "++++++mCurrMainChannel == Renderer.Channel.first?==>" + (mCurrMainChannel == Renderer.Channel.first));
			}
			if (mCurrPreChannel != null && mCurrPreChannel == Renderer.Channel.first) {
				// setMainAspectRatio(-1);
				mPreviewRenderer.keepAspectRatio(1);
			}
			if (mCurrMainChannel != null && mCurrMainChannel == Renderer.Channel.first) {
				setMainAspectRatio(1);
				// mPreviewRenderer.keepAspectRatio(-1);
			}
		} else {
			{
				Log.w("VConfVideoFrame", " 不是两画面合成：mCurrPreChannel == Renderer.Channel.first？==>"
						+ (mCurrPreChannel == Renderer.Channel.first)
						+ "++++++mCurrMainChannel == Renderer.Channel.first?==>" + (mCurrMainChannel == Renderer.Channel.first));
			}
			if (mCurrPreChannel != null && mCurrPreChannel == Renderer.Channel.first) {
				// setMainAspectRatio(-1);
				mPreviewRenderer.keepAspectRatio(-1);
			}
			if (mCurrMainChannel != null && mCurrMainChannel == Renderer.Channel.first) {
				setMainAspectRatio(-1);
				// mPreviewRenderer.keepAspectRatio(-1);
			}
			/*setMainAspectRatio(-1);
			mPreviewRenderer.keepAspectRatio(-1);*/
		}
	}

	private void setMainAspectRatio(int val) {
		VConfVideoPlayFrame playFrame = null;
		Fragment currFragment = mVConfVideoUI.getCurrFragmentView();
		if (null != currFragment && (currFragment instanceof VConfVideoPlayFrame)) {
			playFrame = (VConfVideoPlayFrame) currFragment;
		}

		if (null != playFrame && null != playFrame.getmMainRenderer()) {
			playFrame.getmMainRenderer().keepAspectRatio(val);
		}
	}
}