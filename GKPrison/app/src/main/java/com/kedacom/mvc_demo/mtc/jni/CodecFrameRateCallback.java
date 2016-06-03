/**
 * @(#)CodecFrameRateCallback.java 2013-8-15 Copyright 2013 it.kedacom.com, Inc.
 *                                 All rights reserved.
 * 
 *                                 <pre>
 *  ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29858</EventID>
 * <Message>
 * <CodecFrameRate>
 * 	<Result>1</Result>
 * 	<CallInfoFrameRate>
 * 		<PriomEncFrame>0</PriomEncFrame>
 * 		<PriomDecFrame>0</PriomDecFrame>
 * 		<SecondEncFrame>0</SecondEncFrame>
 * 		<SecondDecFrame>0</SecondDecFrame>
 * 	</CallInfoFrameRate>
 * </CodecFrameRate>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;
import android.util.Log;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.mvc_demo.vconf.bean.CallInfoFrameRate;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;

/**
 * @author chenjian
 * @date 2013-8-15
 */

public class CodecFrameRateCallback extends BaseCallbackHandler {

	/**
	 * @see com.kedacom.truetouch.mtc.jni.BaseCallbackHandler#addCallback(String)
	 */
	@Override
	public void addCallback(String strXML) {
		Log.i("vconf", "CodecFrameRate");

		if (null == strXML) {
			return;
		}

		// 保证只在音视频播放界面时才接收数据
		Activity currActivity = AppStackManager.Instance().currentActivity();
		if (null == currActivity) {
			return;
		}

		// if (!(currActivity instanceof VConfAudioActivity) && !(currActivity instanceof VConfVideoActivity)) {
		// return;
		// }
		if (!(currActivity instanceof VConfVideoActivity)) {
			return;
		}

		loadParseSingleXML(strXML);
		CallInfoFrameRate callInfoFrameRate = new CallInfoFrameRate();
		callInfoFrameRate.mPriomEncFrame = getSingleObject("PriomEncFrame");
		callInfoFrameRate.mPriomDecFrame = getSingleObject("PriomDecFrame");
		callInfoFrameRate.mSecondEncFrame = getSingleObject("SecondEncFrame");
		callInfoFrameRate.mSecondDecFrame = getSingleObject("SecondDecFrame");

		// // 当前正处于音频播放界面
		// if (currActivity instanceof VConfAudioActivity) {
		// VConfAudioActivity vconfAudioActivity = (VConfAudioActivity) currActivity;
		// if (vconfAudioActivity.getVConfFunctionView() == null) {
		// return;
		// }
		//
		// vconfAudioActivity.getVConfFunctionView().showCodeStatusDetails(null, callInfoFrameRate);
		// }
		// 当前正处于视频播放界面
		// else

		if (currActivity instanceof VConfVideoActivity) {
			VConfVideoActivity vconfVideoActivity = (VConfVideoActivity) currActivity;
			if (vconfVideoActivity.getVConfFunctionView() == null) {
				return;
			}

			// vconfVideoActivity.getVConfFunctionView().showCodeStatusDetails(null, callInfoFrameRate);
		}
	}
}
