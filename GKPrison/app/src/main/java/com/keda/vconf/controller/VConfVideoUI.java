package com.keda.vconf.controller;

/**
 * @(#)VConfVideoUI.java   2014-8-28
 * Copyright 2014  it.kedacom.com, Inc. All rights reserved.
 */

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.gkzxhn.gkprison.application.MyApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kedacom.kdv.mt.bean.TMtAddr;
import com.kedacom.kdv.mt.constant.EmNativeConfType;
import com.keda.sky.app.PcAppStackManager;
import com.keda.vconf.bean.VConf;
import com.keda.vconf.manager.VConferenceManager;
import com.keda.vconf.video.controller.VConfJoinVideoFrame;
import com.keda.vconf.video.controller.VConfVideoFrame;
import com.keda.vconf.video.controller.VConfVideoPlayFrame;
import com.pc.utils.StringUtils;

import java.util.List;

/**
  * 视频会议
  *  
  * @author chenj
  * @date 2014-8-28
  */

public class VConfVideoUI extends ActionBarActivity {

	private final int id = 0x1e010011;
	protected VConf mVConf;
	protected String mE164;
	protected String mConfTitle;
	protected boolean mIsP2PConf;
	protected boolean mIsJoinConf;

	// 当前的音视频面
	protected Fragment mCurrFragmentView;

	// 当前的ContentFrame
	protected Fragment mVConfContentFrame;
	private List<TMtAddr> mTMtList;// 邀请的视频会议终端

	private int mVConfQuality;// 会议质量 2M.1M.256,192
	private int mDuration;// 会议时长

	/**
	 * @see com.kedacom.truetouch.vconf.controller.AbsVConfActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("VConfVideo", "VConfVideoUI-->onCreate");
		super.onCreate(savedInstanceState);
		PcAppStackManager.Instance().pushActivity(this);
		// 让音量键固定为媒体音量控制,其他的页面不要这样设置--只在音视频的界面加入这段代码
		this.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

		FrameLayout c = new FrameLayout(this);
		c.setId(id);
		setContentView(c, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		initExtras();
		onViewCreated();
	}

	/**
	 * @see com.kedacom.truetouch.sky.app.TTActivity#onRestart()
	 */
	@Override
	protected void onRestart() {
		Log.i("VConfVideo", "VConfVideoUI-->onRestart");
		super.onRestart();
	}

	/**
	 * @see com.kedacom.truetouch.sky.app.TTActivity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.i("VConfVideo", "VConfVideoUI-->onStart");
		super.onStart();
	}

	/**
	 * @see com.kedacom.truetouch.vconf.controller.AbsVConfActivity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.i("VConfVideo", "VConfVideoUI-->onResume");
		super.onResume();
	}

	/**
	 * @see android.support.v4.app.FragmentActivity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Log.i("VConfVideo", "VConfVideoUI-->onNewIntent");
	}

	/**
	 * @see com.pc.app.base.PcActivity#onPostCreate(android.os.Bundle)
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	protected void onViewCreated() {
		mVConf = new VConf();
		mVConf.setAchConfE164(mE164);
		mVConf.setAchConfName(mConfTitle);

		// if (VConferenceManager.currTMtCallLinkSate != null) {
		// mVConf.setConfName(VConferenceManager.currTMtCallLinkSate.getAlias());
		// }

		if (!StringUtils.isNull(VConferenceManager.mCallPeerE164Num)) {
			mVConf.setAchConfE164(VConferenceManager.mCallPeerE164Num);
		}
		switchVConfFragment();
	}

	/**
	 * @see com.kedacom.truetouch.vconf.controller.AbsVConfActivity#initExtras()
	 */
	public void initExtras() {

		Bundle extra = getIntent().getExtras();
		if (null == extra) {
			return;
		}
		if (null != extra) {
			mConfTitle = extra.getString("VconfName");
			mE164 = extra.getString(MyApplication.E164NUM);
			mIsP2PConf = extra.getBoolean("MackCall", false);
			mIsJoinConf = extra.getBoolean("JoinConf", false);
		}

		if (null != VConferenceManager.mConfInfo) {
			mConfTitle = VConferenceManager.mConfInfo.achConfName;
		}
		if (null != VConferenceManager.currTMtCallLinkSate) {
			mIsP2PConf = VConferenceManager.currTMtCallLinkSate.isP2PVConf();
		}
		if (null != VConferenceManager.mCallPeerE164Num) {
			mE164 = VConferenceManager.mCallPeerE164Num;
		}
		mVConfQuality = extra.getInt("VconfQuality"); //会议质量
		mDuration = extra.getInt("VconfDuration");//会议时长
		try {
			mTMtList = new Gson().fromJson(extra.getString("tMtList"), new TypeToken<List<TMtAddr>>() {
			}.getType());
		} catch (Exception e) {
		}

	}

	public Fragment getCurrFragmentView() {
		return mCurrFragmentView;
	}

	public VConfVideoFrame getVConfContentFrame() {
		return (VConfVideoFrame) mVConfContentFrame;
	}

	/**
	 * 切换视音频界面
	 * @see com.kedacom.truetouch.vconf.controller.AbsVConfActivity#switchVConfFragment()
	 */
	public void switchVConfFragment() {
		// 视频会议
		if (VConferenceManager.isCSVConf() || VConferenceManager.nativeConfType == EmNativeConfType.VIDEO
				|| (null != VConferenceManager.currTMtCallLinkSate && !VConferenceManager.currTMtCallLinkSate.isCaller())) {// 被叫时直接进入视频界面
			if (null == mVConfContentFrame) {
				mVConfContentFrame = new VConfVideoFrame();
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(id, mVConfContentFrame);
				ft.commitAllowingStateLoss();
			}

			if (!(mCurrFragmentView instanceof VConfVideoPlayFrame)) {
				mCurrFragmentView = new VConfVideoPlayFrame();
				((VConfVideoFrame) mVConfContentFrame).replaceContentFrame(mCurrFragmentView);
			}
		}
		// 视频入会
		else {
			if (mCurrFragmentView instanceof VConfJoinVideoFrame) {
				return;
			}

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			mVConfContentFrame = null;
			mCurrFragmentView = new VConfJoinVideoFrame();
			ft.replace(id, mCurrFragmentView);
			ft.commit();
		}
	}

	public void setScreenOrientationLandscape() {
		if (VConferenceManager.isCSMCC() && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) { // 横屏
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	/**
	 * 旋转屏幕时
	 * @see android.support.v4.app.FragmentActivity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.i("VConfVideo", "VconfVideoFrame-->onConfigurationChanged 是否为横屏：false");
		}
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.i("VConfVideo", "VconfVideoFrame-->onConfigurationChanged 是否为横屏：true");
		}
	}

	/**
	 * @see com.kedacom.truetouch.app.TTBaseActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// super.onBackPressed();
	}

	/** @return the tMtList */
	public List<TMtAddr> gettMtList() {
		return mTMtList;
	}

	/**
	 * @see com.kedacom.truetouch.vconf.controller.AbsVConfActivity#onPause()
	 */
	@Override
	protected void onPause() {
		Log.w("VConfVideo", "VConfVideoUI-->onPause");
		super.onPause();
	}

	/**
	 * @see com.kedacom.truetouch.vconf.controller.AbsVConfActivity#onStop()
	 */
	@Override
	protected void onStop() {
		Log.w("VConfVideo", "VConfVideoUI-->onStop");
		super.onStop();
	}

	/**
	 * @see com.kedacom.truetouch.sky.app.TTActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.w("VConfVideo", "VConfVideoUI-->onDestroy");
		PcAppStackManager.Instance().popActivity(this, false);
		super.onDestroy();
	}

	/** @return the mE164 */
	public String getmE164() {
		return mE164;
	}

	/** @return the mVConf */
	public VConf getmVConf() {
		return mVConf;
	}

	/** @return the mConfTitle */
	public String getmConfTitle() {
		return mConfTitle;
	}

	/** @return the mIsP2PConf */
	public boolean ismIsP2PConf() {
		return mIsP2PConf;
	}

	/** @return the mIsJoinConf */
	public boolean ismIsJoinConf() {
		return mIsJoinConf;
	}

	/** @param mConfTitle the mConfTitle to set */
	public void setmConfTitle(String mConfTitle) {
		this.mConfTitle = mConfTitle;
	}

	/** @return the mVConfQuality */
	public int getVConfQuality() {
		return mVConfQuality;
	}

	/** @return the mDuration */
	public int getDuration() {
		return mDuration;
	}

}