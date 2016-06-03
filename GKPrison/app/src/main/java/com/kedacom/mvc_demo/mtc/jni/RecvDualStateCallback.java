/**
 * <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29279</EventID>
 * <Message>
 * <RecvDualState>
 * 	<Result>1</Result>
 * 	<bIsReceiveDual>0</bIsReceiveDual>
 * </RecvDualState>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */
package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.kedacom.mvc_demo.login.LoginFlowService;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;
import com.pc.utils.StringUtils;

/**
 * @author chenjian
 * @date 2013-6-6
 */
public class RecvDualStateCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		Log.e("vconf", "RecvDualState");

		// 刚启时，会收到一条RecvDualState消息，通过判断GK是否注册成功来过滤这条多余的消息
		if (!LoginFlowService.mRegisterGK) {
			return;
		}

		loadParseSingleXML(strXML);

		if (!result4SingleXML()) {
			return;
		}

		String isReceiveDual = getSingleObject("bIsReceiveDual");
		boolean isDualStream = StringUtils.equals(isReceiveDual, "1");

		Bundle b = new Bundle();
		b.putBoolean("bIsReceiveDual", isDualStream);
		Intent intent = new Intent();
		intent.setAction(VideoConferenceService.RECVDUALSTATE_ACTION);
		intent.putExtras(b);
		MyApplication.getApplication().sendBroadcast(intent);
		if (!isDualStream) {// 取消双流
			Activity currentActivity = AppStackManager.Instance().currentActivity();

			// VConfAudioActivity audioActivity = (VConfAudioActivity)
			// TruetouchGlobal.getActivity(VConfAudioActivity.class);
			//
			// // 音频播放界面
			// if (audioActivity != null) {
			// if (audioActivity.getVConfFunctionView() == null) {
			// return;
			// }
			//
			// audioActivity.getVConfFunctionView().cleanSecondEncryption();
			// }

			VConfVideoActivity videoActivity = (VConfVideoActivity) AppStackManager.Instance().getActivity(VConfVideoActivity.class);

			// 视频播放界面
			if (videoActivity != null) {
				if (videoActivity.getVConfFunctionView() == null) {
					return;
				}

				// videoActivity.getVConfFunctionView().cleanSecondEncryption();
			}
		}
	}

}
