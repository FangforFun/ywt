package com.keda.vconf.controller;

/**
 * @(#)AVResponseActivity.java   2014-8-28
 * Copyright 2014  it.kedacom.com, Inc. All rights reserved.
 */

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kedacom.kdv.mt.api.Conference;
import com.kedacom.kdv.mt.constant.EmConfProtocol;
import com.kedacom.kdv.mt.constant.EmNativeConfType;
import com.gkzxhn.gkprison.R;
import com.keda.sky.app.PcAppStackManager;
import com.keda.vconf.manager.VConferenceManager;
import com.pc.utils.NetWorkUtils;

/**
  * 音视频应答
  * 
  * @author chenj
  * @date 2014-8-28
  */

public class VConfAVResponseUI extends ActionBarActivity implements View.OnClickListener {

	private String mE164Num;
	// private String mPeerAlias;// 呼叫方名称

	private TextView mFlowTextView;

	private TextView mConnTextView;

	private TextView mPeerAliasTextView;

	private LinearLayout mAudioRspBtn;

	private LinearLayout mVideoRspBtn;

	// private LinearLayout mTelRspBtn;

	private boolean mIsAudioConf;// true 音频应答，false 视频应答

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PcAppStackManager.Instance().pushActivity(this);

		if (VConferenceManager.answerMode == 0) {//自动应答  --规避，手机不使用自动接听
			acceptVconfCall(true, false);
			setTheme(android.R.style.Theme_Translucent);
		} else {
			setContentView(R.layout.avresponse_layout);
			initExtras();
			onViewCreated();
			showP2PDetails();
		}
	}

	public void initExtras() {

	}

	public void onViewCreated() {
		findViews();
		registerListeners();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

	}

	public void findViews() {
		mFlowTextView = (TextView) findViewById(R.id.flow_remind);
		mConnTextView = (TextView) findViewById(R.id.joinVConf_waitingText);
		mPeerAliasTextView = (TextView) findViewById(R.id.peer_alias);
		mAudioRspBtn = (LinearLayout) findViewById(R.id.audio_response_btn);
		mVideoRspBtn = (LinearLayout) findViewById(R.id.video_response_btn);
		// mTelRspBtn = (LinearLayout) findViewById(R.id.tel_response_btn);

	}

	public void initComponentValue() {
		if (null == VConferenceManager.currTMtCallLinkSate) {
			finish();
		}
		mE164Num = VConferenceManager.mCallPeerE164Num;

		if (NetWorkUtils.isWiFi(this)) {
			mFlowTextView.setVisibility(View.INVISIBLE);
		}
		// 2G网络给出友好提示
		else if (NetWorkUtils.is2G(this)) {
			mVideoRspBtn.setVisibility(View.GONE);
			mAudioRspBtn.setVisibility(View.GONE);
			mFlowTextView.setVisibility(View.VISIBLE);
			mFlowTextView.setText(R.string.vconf_2g_unable_tel_join);
		} else {
			mFlowTextView.setVisibility(View.VISIBLE);
			if (VConferenceManager.isP2PVConf()) {// p2p
				mFlowTextView.setText(R.string.vconf_chooseJoinWay_3GInfo_normal);
			} else {// mcc
				mFlowTextView.setText(R.string.vconf_chooseJoinWay_3GInfo_mobile);
			}
		}

		// 判断码率是否音频码率（64），64的码率只能音频接听
		if ((null != VConferenceManager.currTMtCallLinkSate && VConferenceManager.currTMtCallLinkSate.isAudio())
				|| VConferenceManager.isAudioCallRate(VConferenceManager.confCallRete(getApplicationContext()))) {
			mVideoRspBtn.setVisibility(View.GONE);
			VConferenceManager.nativeConfType = EmNativeConfType.AUDIO;
			((TextView) findViewById(R.id.audio_response_txt)).setText(R.string.vconf_answer);
		} else {
			VConferenceManager.nativeConfType = EmNativeConfType.VIDEO;
		}

		// p2p
		if (VConferenceManager.isP2PVConf()) {
			// mTelRspBtn.setVisibility(View.GONE);
		} else {
			String alias = "";
			if (null != VConferenceManager.currTMtCallLinkSate && null != VConferenceManager.currTMtCallLinkSate.tPeerAlias) {
				alias = VConferenceManager.currTMtCallLinkSate.tPeerAlias.getAlias();
			}
			mPeerAliasTextView.setText(alias);
			mConnTextView.setText(R.string.vconf_join_waitingTxt_mul);
		}

	}

	public void registerListeners() {
		mVideoRspBtn.setOnClickListener(this);
		mAudioRspBtn.setOnClickListener(this);
		// mTelRspBtn.setOnClickListener(this);

		findViewById(R.id.refuse_response_btn).setOnClickListener(this);
	}

	/**
	 * P2P呼叫，对端头像、Name
	 */
	private void showP2PDetails() {
		if (null != VConferenceManager.currTMtCallLinkSate && null != VConferenceManager.currTMtCallLinkSate.tPeerAlias) {
			String alias = VConferenceManager.currTMtCallLinkSate.tPeerAlias.getAlias();
			mPeerAliasTextView.setText(alias);
		}

	}

	/**
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.video_response_btn:// 视频应答

			// 正在发起呼叫状态
				if (VConferenceManager.currTMtCallLinkSate != null && VConferenceManager.currTMtCallLinkSate.isCallIncoming() && !VConferenceManager.currTMtCallLinkSate.isAudio()) {
					acceptVconfCall(true, false);
					mConnTextView.setVisibility(View.VISIBLE);
					mFlowTextView.setVisibility(View.INVISIBLE);
					hintResponseBtn();
					mIsAudioConf = false;

				}
				finish();
				break;

		// 音频应答,Note:视频会议可以音频应答
			case R.id.audio_response_btn:

				if (VConferenceManager.currTMtCallLinkSate != null && VConferenceManager.currTMtCallLinkSate.isCallIncoming()) {
					acceptVconfCall(true, true);
					mConnTextView.setVisibility(View.VISIBLE);
					mFlowTextView.setVisibility(View.INVISIBLE);
					mIsAudioConf = true;
					hintResponseBtn();
				}
				finish();
				break;

		case R.id.refuse_response_btn:// 拒绝呼叫
				acceptVconfCall(false, false);
				finish();
				break;

			default:
				break;
		}

	}

	/**
	 * @see com.kedacom.truetouch.sky.app.TTActivity#onStop()
	 */

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PcAppStackManager.Instance().popActivity(this, false);
	}

	/**
	 * accept vconf
	  *
	  * @param bisAccept
	  * @param audio
	 */
	private void acceptVconfCall(final boolean bIsAccept, final boolean audio) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (!bIsAccept) {
					Conference.rejectConf();
					return;
				}

				if (audio) {
					// 关闭第一路视频流
					Conference.mainVideoOff();

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				} else {
					int minCallRate = VConferenceManager.getCallRate();
					Conference.setCallCapPlusCmd(VConferenceManager.getSendResolutionByCallRate(minCallRate),
							VConferenceManager.getRecResolutionByCallRate(minCallRate),
							EmConfProtocol.em323.ordinal());
				}
				Conference.acceptConf();
			}
		}).start();
	}

	/**
	 * 禁用后退按键
	 */
	@Override
	public void onBackPressed() {
	}

	private void hintResponseBtn() {
		mVideoRspBtn.setVisibility(View.GONE);
		mAudioRspBtn.setVisibility(View.GONE);
		// mTelRspBtn.setVisibility(View.GONE);
	}

	/** 
	 * @return 
	 * 是否是音频接听
	*/
	public boolean ismIsAudioConf() {
		return mIsAudioConf;
	}
}
