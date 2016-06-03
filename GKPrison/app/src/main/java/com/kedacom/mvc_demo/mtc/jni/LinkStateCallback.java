/**
 * @(#)LinkStateCallback.java 2013-7-30 Copyright 2013 it.kedacom.com, Inc. All
 *                            rights reserved.
 * 
 *                            <pre>
 *   ------------------------------
 *  <?xml version="1.0" encoding="utf-8"?>
 *  		<TrueTouchAndroid>
 *  		<EventID>
 *  29338
 *  </EventID>
 *  		<Message>
 *  <LinkState>
 *  			<Result>1</Result>
 *  			<emCallState>0</emCallState>
 *  			<IpAddr>0.0.0.0</IpAddr>
 *  			<Alias></Alias>
 *  			<bCalling>0</bCalling>
 *  			<emReason>5</emReason>
 *  			<bGetChairToken>0</bGetChairToken>
 *  			<bSeenByAll>0</bSeenByAll>
 *  			<wCallRate>0</wCallRate>
 *  			<emPeerMtModel>0</emPeerMtModel>
 *  		</LinkState>
 *  </Message>
 *  		</TrueTouchAndroid>
 *  ------------------------------
 * </pre>
 * 
 *                            <pre>
 * //呼叫挂断原因
 * enum EmCallDisconnectReason
 * {
 * 	emDisconnect_Busy = 1   ,//对端忙
 * 	emDisconnect_Normal     ,//正常挂断
 * 	emDisconnect_Rejected   ,//对端拒绝
 * 	emDisconnect_Unreachable ,//对端不可达
 * 	emDisconnect_Local       ,//本地原因
 * 	emDisconnect_Nnknown,      //未知原因
 * 	emDisconnect_custom,       //自定义原因
 * 	emDisconnect_AdaptiveBusy,		    // 接入电话终端失败
 * 	emDisconnect_Joinconftimeout,		// 参加会议超时
 *  emDisconnect_Createconftimeout,		// 召集会议超时
 * 	emDisconnect_Nomediaresource,		// 没有媒体资源
 *  emDisconnect_Exceedmaxinconfmtnum,	// 超过会议最大终端数（参加会议时）
 *  emDisconnect_Exceedmaxconfnum,		// 超过会议最大数（创建会议时）
 * 	emDisconnect_EncrypeErr,            // 与会议加密模式不符
 * 	emDisconnect_P2Ptimeout,            // 点对点呼叫超时
 * 
 *  emDisconnect_MccDrop,              // 会控挂断
 *  emDisconnect_ChairDrop,            // 主席挂断
 *  emDisconnect_MMcuDrop,             // 上级会议挂断
 *  emDisconnect_ConfRelease,          // 会议结束挂断
 * 
 * 	emDisconnect_PeerInConf,           //正在会议中
 * 	emDisconnect_PeerNoDisturb,        //免打扰
 * 	emDisconnect_NotInPeerContact,      //非好友
 * 	emDisconnect_End
 * };
 * 
 * 	// 呼叫状态
 * 	enum EmCallState {
 * 	emCS_Idle, 0
 * 	emCS_Calling, // 正在发起呼叫
 * 	emCS_P2P, // 点对点会议
 * 	emCS_MCC, // 多点会议
 * 	emCS_Hanup // 挂断
 * 	};
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.kedacom.mvc_demo.login.LoginFlowService;
import com.kedacom.mvc_demo.vconf.bean.EmCallState;
import com.kedacom.mvc_demo.vconf.bean.LinkState;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;
import com.kedacom.truetouch.mtc.EmMtModel;
import com.pc.utils.StringUtils;

/**
 * 视频会议状态
 * 
 * @author chenjian
 * @date 2013-7-30
 */

public class LinkStateCallback extends BaseCallbackHandler {

	// 是否需要存储,true 需要存储
	private static boolean saveFlag = false;

	// 是否已接通 true已接通
	public static boolean hasConnected = true;

	/**
	 * @see com.kedacom.mobilemvc.mtc.jni.BaseCallbackHandler#addCallback(String)
	 */
	@Override
	public void addCallback(String strXML) {
		android.util.Log.i("vconf", "LinkState");

		// 刚启动程序时，会收到LinkState，通过判断GK是否注册成功来过滤这条多余的消息
		if (!LoginFlowService.mRegisterGK && !VideoConferenceService.isCSVConf()) {
			return;
		}

		int preEmCallState = 0;
		String preIpAddr = "";
		String preAlias = "";
		boolean preIsCSVConf = VideoConferenceService.isCSVConf();
		if (VideoConferenceService.mLinkState != null) {
			preEmCallState = VideoConferenceService.mLinkState.getEmCallState();
			preIpAddr = VideoConferenceService.mLinkState.getIpAddr();
			preAlias = VideoConferenceService.mLinkState.getAlias();
		}

		loadParseSingleXML(strXML);

		Intent intent = new Intent(VideoConferenceService.LINKSTATE_ACTION);
		Bundle extras = new Bundle();

		boolean result = result4SingleXML();

		// 会话状态
		int emCallState = StringUtils.str2Int(getSingleObject("emCallState"), 0);

		// 对端IP地址(网络序)
		String ipAddr = getSingleObject("IpAddr");

		// 对端别名
		String alias = getSingleObject("Alias");

		// TRUE = 主叫 FALSE=被叫
		boolean bCalling = StringUtils.equals(getSingleObject("bCalling"), "1");

		// 呼叫挂断原因
		String emReason = getSingleObject("emReason");

		// 获得主席令牌
		boolean bGetChairToken = StringUtils.equals(getSingleObject("bGetChairToken"), "1");

		// 被广播
		boolean bSeenByAll = StringUtils.equals(getSingleObject("bSeenByAll"), "1");

		// 呼叫码率
		int callRate = StringUtils.str2Int(getSingleObject("wCallRate"), 0);

		// 对端型号
		int emPeerMtModle = StringUtils.str2Int(getSingleObject("emPeerMtModel"), 0);

		LinkState linkState;
		if (VideoConferenceService.mLinkState == null) {
			linkState = new LinkState();
		} else {
			linkState = VideoConferenceService.mLinkState;
		}

		linkState.setResult(result);
		linkState.setEmCallState(emCallState);
		linkState.setIpAddr(ipAddr);
		linkState.setAlias(alias);
		linkState.setbCalling(VideoConferenceService.isJoinConf);
		linkState.setEmReason(emReason);
		linkState.setbGetChairToken(bGetChairToken);
		linkState.setbSeenByAll(bSeenByAll);
		linkState.setwCallRate(callRate);
		linkState.setEmPeerMtModel(EmMtModel.toEmMtModel(emPeerMtModle));
		VideoConferenceService.mLinkState = linkState;

		extras.putString("emReason", emReason);
		extras.putBoolean(MyApplication.RESULT, result);

		// 本次会话状态与之前相同
		if (preEmCallState != 0 && preEmCallState == emCallState && StringUtils.equals(preIpAddr, ipAddr) && StringUtils.equals(preAlias, alias)) {
		} else {
			intent.putExtras(extras);
			MyApplication.getApplication().sendBroadcast(intent);
		}

		if (emCallState == EmCallState.IDLE.ordinal() || emCallState == EmCallState.HANUP.ordinal()) {
			if (saveFlag) {
				saveFlag = false;

				VideoConferenceService.cleanConf();
			}

			VideoConferenceService.quitConfAction(false);
		} else if (emCallState == EmCallState.CALLING.ordinal()) {

			hasConnected = false;
			saveFlag = true;
		} else if (emCallState == EmCallState.P2P.ordinal() || emCallState == EmCallState.MCC.ordinal()) {
			hasConnected = true;
			saveFlag = true;
		}

		if (!result || linkState.isCSHanup() || linkState.isCSIDLE()) {
			Activity currentActivity = AppStackManager.Instance().currentActivity();
			if (currentActivity != null && ((currentActivity instanceof VConfVideoActivity))) {
				currentActivity.finish();
			}
		}
	}

}
