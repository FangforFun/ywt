package com.kedacom.mvc_demo.vconf.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.kedacom.mvc_demo.ContactListActivity;
import com.kedacom.mvc_demo.vconf.bean.LinkState;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.pc.utils.StringUtils;

/**
 * 音视频广播接收器
 * 
 * @author fuzheng
 * 
 */
public class VConfBroadcastReceiver extends BroadcastReceiver {

	private Activity currActivity;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			return;
		}

		String action = intent.getAction();
		if (!StringUtils.equals(action, VideoConferenceService.JOINCONF_ACTION)
				&& !StringUtils.equals(action,
						VideoConferenceService.LINKSTATE_ACTION)) {
			return;
		}

		Bundle b = intent.getExtras();
		if (b == null) {
			return;
		}

		currActivity = AppStackManager.Instance().currentActivity();

		// 未知数据，暂时不做处理
		if (StringUtils.equals(action, VideoConferenceService.JOINCONF_ACTION)) {
			if (!VideoConferenceService.isCSVConf()) {
				boolean result = b.getBoolean(MyApplication.RESULT,
						false);
				if (!result) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							Looper.prepare();
//							Toast.makeText(MBaseApplicationImpl.getContext(),
//									R.string.vconf_failed, 0).show();
							Looper.loop();
						}
					}).start();
				}
			}

			closeCallingDialog();

			return;
		}

		boolean result = b.getBoolean(MyApplication.RESULT, false);
		final int reason = StringUtils.str2Int(b.getString("emReason"), -1);
		final LinkState linkState = VideoConferenceService.mLinkState;

		// linkState
		if (StringUtils.equals(action, VideoConferenceService.LINKSTATE_ACTION)) {
			if (linkState == null) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
//						Toast.makeText(MBaseApplicationImpl.getContext(),
//								R.string.vconf_failed, 0).show();
						closeCallingDialog();
					}
				}, 1000);
				return;
			}

			// if (!linkState.isVideo()) {
			// return;
			// }

			if (!result || linkState.isCSHanup()) {
//				Toast.makeText(MBaseApplicationImpl.getContext(),
//						R.string.vconf_failed, 0).show();
				closeCallingDialog();
				return;
			}

			if (linkState.isCSCalling()) {
				return;
			}

			// 收到视频入会通知
			if ((linkState.isCSP2P() || linkState.isCSMCC())) {
				Bundle pBundle = new Bundle();
				pBundle.putBoolean("MackCall", true);
				pBundle.putBoolean("JoinVConf", false);
				pBundle.putString(MyApplication.E164NUM,
						VideoConferenceService.mCallPeerE164Num);

				Intent intent2 = new Intent(currActivity,
						VConfVideoActivity.class);
				intent2.putExtras(pBundle);
				currActivity.startActivity(intent2);
			}
			// 入会失败
			else {
//				Toast.makeText(MBaseApplicationImpl.getContext(),
//						R.string.vconf_failed, 0).show();
			}

			closeCallingDialog();
		}
	}

	private void closeCallingDialog() {
		if (null == currActivity)
			return;

//		if (currActivity instanceof MenuActivity) {
//			((MenuActivity) currActivity).closeCallingDialog();
//		} else
		if (currActivity instanceof ContactListActivity) {
			((ContactListActivity) currActivity).closeCallingDialog();
		}
	}

}
