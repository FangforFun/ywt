/**
 * @ VideoResponseActivity.java 2013-8-15
 */

package com.kedacom.mvc_demo.vconf.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.vconf.bean.VConfType;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.pc.utils.StringUtils;
import com.pc.utils.TerminalUtils;

/**
 * 音视频应答 Dialog
 * @author ryf
 * @date 2013-8-15
 */

public class AVResponseActivity extends ActionBarActivity implements View.OnClickListener {

	private boolean mIsAudio = true;// ture音频呼叫，false视频呼叫
	private String mPeerAlias = "";// 呼叫方名称
	private String mE164Num;

	private int mCallRate;

	private TextView mFlowTextView;
	private TextView mConnTextView;
	private TextView mPeerAliasTextView;

	private boolean mClickResponse;
	private boolean isForegroundStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AppStackManager.Instance().pushActivity(this);

		android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
		params.x = 0;
		params.y = TerminalUtils.terminalH(getApplicationContext());
		getWindow().setAttributes(params);

		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			mPeerAlias = intent.getExtras().getString("peerAlias");
			mIsAudio = intent.getExtras().getBoolean("isAudio", mIsAudio);
			isForegroundStart = intent.getExtras().getBoolean("appIsForeground", true);
			mCallRate = intent.getExtras().getInt("callRate", VideoConferenceService.CALLRATE_SPLITLINE);
		}

		setContentView(R.layout.avresponse_layout);
		findViews();
		initComponentValue();
		createListeners();
		registerReceivers();

		View rootview = findViewById(R.id.avr_rootview);
		FrameLayout.LayoutParams flLP = (FrameLayout.LayoutParams) rootview.getLayoutParams();
		flLP.width = TerminalUtils.terminalW(getApplicationContext());
		// flLP.width = FrameLayout.LayoutParams.MATCH_PARENT;
		flLP.height = LayoutParams.WRAP_CONTENT;
		rootview.setLayoutParams(flLP);
	}

	protected void findViews() {
		mConnTextView = (TextView) findViewById(R.id.joinVConf_waitingText);

		mFlowTextView = (TextView) findViewById(R.id.flow_remind);
		mPeerAliasTextView = (TextView) findViewById(R.id.peer_alias);
	}

	public void initComponentValue() {
		mPeerAliasTextView.setText(mPeerAlias);
		mE164Num = VideoConferenceService.mCallPeerE164Num;

		mConnTextView.setVisibility(View.GONE);
	}

	protected void createListeners() {
		findViewById(R.id.response_btn).setOnClickListener(this);
		findViewById(R.id.refuse_response_btn).setOnClickListener(this);
	}

	/**
	 * 注册接收器
	 */
	private void registerReceivers() {
		if (null != mReceiver) return;

		mReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(VideoConferenceService.LINKSTATE_ACTION);
		filter.addAction(VideoConferenceService.JOINCONF_ACTION);
		registerReceiver(mReceiver, filter);
	}

	private void unregisterReceivers() {
		if (null == mReceiver) return;

		unregisterReceiver(mReceiver);
		mReceiver = null;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		AppStackManager.Instance().popActivity(this);
		unregisterReceivers();

		if (!isForegroundStart) {
			// moveTaskToBack(true);
		}

		super.onDestroy();
	}

	/**
	 * 禁用后退按键
	 */
	@Override
	public void onBackPressed() {
	}

	/**
	 * @see Activity#onTouchEvent(MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			if (event.getAction() == MotionEvent.ACTION_DOWN && isOutOfBounds(this, event)) {
				return true;
			}
		}

		return super.onTouchEvent(event);
	}

	/**
	 * is out of bounds
	  *
	  * @param context
	  * @param event
	  * @return
	 */
	private boolean isOutOfBounds(Activity context, MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
		final View decorView = context.getWindow().getDecorView();
		return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop)) || (y > (decorView.getHeight() + slop));
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		// 应答
			case R.id.response_btn:
				isForegroundStart = true;

				// 正在发起呼叫状态
				if (VideoConferenceService.mLinkState != null && VideoConferenceService.mLinkState.isCSCalling()) {
					mClickResponse = true;
					KdvMtBaseAPI.acceptP2PCall(true);

					mFlowTextView.setVisibility(View.GONE);
					mConnTextView.setVisibility(View.VISIBLE);
					findViewById(R.id.bottom_layout).setVisibility(View.GONE);
				} else {
					if (mIsAudio) {
						Toast.makeText(getApplicationContext(), R.string.vconf_avresponse_audio, 0).show();
					} else {
						Toast.makeText(getApplicationContext(), R.string.vconf_avresponse_audio, 0).show();
					}

					finish();
				}

				findViewById(R.id.response_btn).setVisibility(View.GONE);

				break;

			case R.id.refuse_response_btn:// 拒绝呼叫
				isForegroundStart = true;
				if (mClickResponse) {
					break;
				}
				KdvMtBaseAPI.acceptP2PCall(false);
				finish();
				break;

			default:
				break;
		}
	}

	private MyBroadcastReceiver mReceiver;

	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (VideoConferenceService.LINKSTATE_ACTION == null) {
				finish();
				return;
			}

			if (StringUtils.equals(VideoConferenceService.LINKSTATE_ACTION, intent.getAction())) {
				String ipAddr = "";
				String alias = "";
				if (VideoConferenceService.mCallingAddr != null) {
					ipAddr = VideoConferenceService.mCallingAddr.mIP;
					alias = VideoConferenceService.mCallingAddr.mAlias;
				}

				if (StringUtils.isNull(mPeerAlias)) {
					mPeerAlias = alias;
				}

				if (VideoConferenceService.mLinkState != null && (VideoConferenceService.mLinkState.isCSP2P() || VideoConferenceService.mLinkState.isCSMCC())) {
					unregisterReceivers();

					VideoConferenceService.openVConfActivity(AVResponseActivity.this, mPeerAlias, ipAddr, mE164Num, (short) mCallRate, false, VConfType.exist.ordinal());
					finish();
				} else {
					finish();
				}
			} else if (StringUtils.equals(VideoConferenceService.JOINCONF_ACTION, intent.getAction())) {
			}
		}
	}

}
