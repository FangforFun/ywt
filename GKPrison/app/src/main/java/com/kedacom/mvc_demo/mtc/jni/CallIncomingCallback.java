/**
 * @(#)CallIncomingCallback.java 2013-7-30 Copyright 2013 it.kedacom.com, Inc.
 *                               All rights reserved.
 * 
 *                               <pre>
 *  ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29266</EventID>
 * <Message>
 * <CallIncoming>
 * 			<Result>1</Result>
 * 			<emCallType>0</emCallType>
 * 			<emProtocol>0</emProtocol>
 * 			<CallRate>64</CallRate>
 * 			<CallingAddr>
 * 				<emType>0</emType>
 * 				<IP>172.16.10.3</IP>
 * 				<Alias>1.5M+720P</Alias>
 * 			</CallingAddr>
 * 			<CalledAddr>
 * 				<emType>0</emType>
 * 				<IP>0.0.0.0</IP>
 * 				<Alias></Alias>
 * 			</CalledAddr>
 * 		</CallIncoming>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.pc.utils.NetWorkUtils;
import com.pc.utils.StringUtils;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.login.LoginFlowService;
import com.kedacom.mvc_demo.vconf.bean.CallingAddr;
import com.kedacom.mvc_demo.vconf.controller.AVResponseActivity;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;
import com.kedacom.truetouch.mtc.EmMtAddrType;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * @author chenjian
 * @date 2013-7-30
 */

public class CallIncomingCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		if (!LoginFlowService.mRegisterGK) {
			return;
		}

		// 当前音视频状态为正在主动呼叫、正处于音视频状态，直接拒绝
		if (VideoConferenceService.mLinkState != null
				&& ((VideoConferenceService.mLinkState.isCSCalling() && VideoConferenceService.mLinkState.isbCalling()) || VideoConferenceService.mLinkState.isCSP2P() || VideoConferenceService.mLinkState
						.isCSMCC())) {

			KdvMtBaseAPI.acceptP2PCall(false);

			return;
		}

		// 2G拒绝呼叫
		if (NetWorkUtils.is2G(MyApplication.mOurApplication)) {
			KdvMtBaseAPI.acceptP2PCall(false);

			return;
		}

		parseXML(strXML);
	}

	private void parseXML(String strXML) {
		Intent intent = new Intent(VideoConferenceService.ONCALLINCOMING_ACTION);
		Bundle extras = new Bundle();

		try {
			Document doc = DocumentHelper.parseText(strXML);
			Element root = doc.getRootElement();
			if (root == null) {
				return;
			}

			Element message = root.element("Message");
			Element callIncomingElt = message.element("CallIncoming");
			if (callIncomingElt == null) {
				return;
			}

			boolean r = StringUtils.equals(callIncomingElt.elementTextTrim("Result"), "1");
			String emCallType = callIncomingElt.elementTextTrim("emCallType");
			String emProtocol = callIncomingElt.elementTextTrim("emProtocol");
			int callRate = StringUtils.str2Int(callIncomingElt.elementTextTrim("CallRate"), 0);

			Element callingAddrElt = callIncomingElt.element("CallingAddr");
			Element calledAddrElt = callIncomingElt.element("CalledAddr");
			String peerEmType = "", peerIp = "", peerAlias = "";
			if (callingAddrElt != null) {
				CallingAddr callingAddr = new CallingAddr();
				peerEmType = callingAddrElt.elementTextTrim("emType");
				peerIp = callingAddrElt.elementTextTrim("IP");
				String ip2 = callingAddrElt.elementTextTrim("IP2");
				peerAlias = callingAddrElt.elementTextTrim("Alias"); // 别名

				int inIP = StringUtils.str2Int(ip2, 0);
				String ippp = (inIP & 0xFF) + "." + ((inIP >> 8) & 0xFF) + "." + ((inIP >> 16) & 0xFF) + "." + (inIP >> 24 & 0xFF);

				Log.e("Test", ip2 + "   " + ippp);

				callingAddr.mEmType = StringUtils.str2Int(peerEmType, EmMtAddrType.emIPAddr.ordinal());
				callingAddr.mIP = peerIp;
				callingAddr.mAlias = peerAlias;

				VideoConferenceService.mCallingAddr = callingAddr;
			}

			// 音频
			boolean isAudio = callRate == VideoConferenceService.CALLRATE_SPLITLINE;

			extras.putBoolean(MyApplication.RESULT, r);

			intent.putExtras(extras);
			MyApplication.getApplication().sendBroadcast(intent);
			// if (VideoConferenceService.mCallPeerE164Num != null) {
			gotoResponse(isAudio, callRate, peerAlias);
			// }
		} catch (DocumentException e) {
		}

	}

	/**
	 * 跳转至应答界面
	 * @param isAudio
	 * @param peerAlias
	 * @param existContact
	 */
	private void gotoResponse(boolean isAudio, int callRate, String peerAlias) {
		// NotificationsManager.notificationVConference();
		VideoConferenceService.isJoinConf = false;
		// 跳转到应答界面
		Activity preActivity = AppStackManager.Instance().currentActivity();
		// 检测上一个界面是否为一个空闲的音视频应答界面
		if (preActivity != null && preActivity instanceof AVResponseActivity) {
			AppStackManager.Instance().popActivity(preActivity);
		}

		// 跳转到应答界面
		Intent responseIntent = new Intent();
		responseIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle b = new Bundle();
		b.putBoolean("isAudio", isAudio);
		b.putInt("callRate", callRate);
		b.putString("peerAlias", peerAlias);
		b.putBoolean("appIsForeground", true);
		responseIntent.putExtras(b);

		Context context = MyApplication.mOurApplication;
		if (context != null) {
			responseIntent.setClass(context, AVResponseActivity.class);
			context.startActivity(responseIntent);
		}
	}
}
