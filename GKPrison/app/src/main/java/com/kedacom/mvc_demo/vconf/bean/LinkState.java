/**
 * @(#)LinkState.java 2013-7-30 Copyright 2013 it.kedacom.com, Inc. All rights
 *                    reserved.
 * 
 *                    <pre>
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
 * 	emCS_Idle, emCS_Calling, // 正在发起呼叫
 * 	emCS_P2P, // 点对点会议
 * 	emCS_MCC, // 多点会议
 * 	emCS_Hanup // 挂断
 * 	};
 * </pre>
 */

package com.kedacom.mvc_demo.vconf.bean;

import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.truetouch.mtc.EmMtModel;
import com.pc.utils.StringUtils;

/**
 * 视频会议状态
 * 
 * @author chenjian
 * @date 2013-7-30
 */
public class LinkState {

	// // 对端忙
	// public static final int DISCONNECT_BUSY = 1;
	// // 正常挂断
	// public static final int DISCONNECT_NORMAL = 2;
	// // 对端拒绝
	// public static final int DISCONNECT_REJECTED = 3;
	// // 对端不可达
	// public static final int DISCONNECT_UNREACHABLE = 4;
	// // 本地原因
	// public static final int DISCONNECT_LOCAL = 5;
	// // 未知原因
	// public static final int DISCONNECT_NNKNOWN = 6;
	// // 自定义原因 emDisconnect_custom
	// public static final int DISCONNECT_CUSTOM = 7;
	// // 接入电话终端失败 emDisconnect_AdaptiveBusy
	// public static final int DISCONNECT_ADAPTIVEBUSY = 8;
	// // 参加会议超时 emDisconnect_Joinconftimeout
	// public static final int DISCONNECT_JOINCONFTIMEOUT = 9;
	// // 召集会议超时 emDisconnect_Createconftimeout
	// public static final int DISCONNECT_CREATECONFTIMEOUT = 10;
	// // 没有媒体资源 emDisconnect_Nomediaresource
	// public static final int DISCONNECT_NOMEDIARESOURCE = 11;
	// // 超过会议最大终端数（参加会议时） emDisconnect_Exceedmaxinconfmtnum
	// public static final int DISCONNECT_EXCEEDMAXINCONFMTNUM = 12;
	// // 超过会议最大数（创建会议时）emDisconnect_Exceedmaxconfnum
	// public static final int DISCONNECT_EXCEEDMAXCONFNUM = 13;
	// // 与会议加密模式不符 emDisconnect_EncrypeErr
	// public static final int DISCONNECT_ENCRYPEERR = 14;
	// // 点对点呼叫超时 emDisconnect_P2Ptimeout
	// public static final int DISCONNECT_P2PTIMEOUT = 15;
	// // 会控挂断 emDisconnect_MccDrop
	// public static final int DISCONNECT_MCCDROP = 16;
	// // 主席挂断 emDisconnect_ChairDrop
	// public static final int DISCONNECT_CHAIRDROP = 17;
	// // 上级会议挂断 emDisconnect_MMcuDrop
	// public static final int DISCONNECT_MMCUDROP = 18;
	// // 会议结束挂断 emDisconnect_ConfReleaseR
	// public static final int DISCONNECT_ConfRelease = 19;
	// // 正在会议中 emDisconnect_PeerInConf
	// public static final int DISCONNECT_PeerInConf = 20;
	// // 免打扰 emDisconnect_PeerNoDisturb
	// public static final int DISCONNECT_PeerNoDisturb = 21;
	// // 非好友 emDisconnect_NotInPeerContact
	// public static final int DISCONNECT_NotInPeerContact = 22;
	//
	// // emCS_Idle
	// public static final int CS_IDLE = 0;
	// // 正在发起呼叫 emCS_Calling
	// public static final int CS_Calling = 1;
	// // 点对点会议 emCS_P2P
	// public static final int CS_P2P = 2;
	// // 多点会议 emCS_MCC
	// public static final int CS_MCC = 3;
	// // 挂断 emCS_Hanup
	// public static final int CS_Hanup = 4;

	private boolean result;
	private int emCallState; // 呼叫状态
	private String ipAddr; // 对端IP地址(网络序)
	private String alias; // 对端别名
	private boolean bCalling; // TRUE = 主叫 FALSE=被叫
	private String emReason; // 呼叫挂断原因
	private boolean bGetChairToken; // 获得主席令牌
	private boolean bSeenByAll; // 被广播
	private int wCallRate; // 呼叫码率
	private EmMtModel emPeerMtModel; // 对端型号

	/** @return the result */
	public boolean isResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

	/** @return the emCallState */
	public int getEmCallState() {
		return emCallState;
	}

	/**
	 * @param emCallState
	 *            the emCallState to set
	 */
	public void setEmCallState(int emCallState) {
		this.emCallState = emCallState;
	}

	/** @return the ipAddr */
	public String getIpAddr() {
		return ipAddr;
	}

	/**
	 * @param ipAddr
	 *            the ipAddr to set
	 */
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	/** @return the alias */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias
	 *            the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/** @return the bCalling */
	public boolean isbCalling() {
		return bCalling;
	}

	/**
	 * @param bCalling
	 *            the bCalling to set
	 */
	public void setbCalling(boolean bCalling) {
		this.bCalling = bCalling;
	}

	/** @return the emReason */
	public String getEmReason() {
		return emReason;
	}

	public int getReason() {
		return StringUtils.str2Int(emReason,
				EmCallDisconnectReason.nnknown.ordinal());
	}

	/**
	 * @param emReason
	 *            the emReason to set
	 */
	public void setEmReason(String emReason) {
		this.emReason = emReason;
	}

	/** @return the bGetChairToken */
	public boolean isbGetChairToken() {
		return bGetChairToken;
	}

	/**
	 * @param bGetChairToken
	 *            the bGetChairToken to set
	 */
	public void setbGetChairToken(boolean bGetChairToken) {
		this.bGetChairToken = bGetChairToken;
	}

	/** @return the bSeenByAll */
	public boolean isbSeenByAll() {
		return bSeenByAll;
	}

	/**
	 * @param bSeenByAll
	 *            the bSeenByAll to set
	 */
	public void setbSeenByAll(boolean bSeenByAll) {
		this.bSeenByAll = bSeenByAll;
	}

	/** @return the wCallRate */
	public int getwCallRate() {
		return wCallRate;
	}

	/**
	 * @param wCallRate
	 *            the wCallRate to set
	 */
	public void setwCallRate(int wCallRate) {
		this.wCallRate = wCallRate;
	}

	/** @return the emPeerMtModel */
	public EmMtModel getEmPeerMtModel() {
		return emPeerMtModel;
	}

	/**
	 * @param emPeerMtModel
	 *            the emPeerMtModel to set
	 */
	public void setEmPeerMtModel(EmMtModel emPeerMtModel) {
		this.emPeerMtModel = emPeerMtModel;
	}

	/**
	 * 对端型号是否为Phone
	 * 
	 * @return
	 */
	public boolean isPeerMtModelPhone() {
		if (isPeerMtModelAndroidPhone()) {
			return true;
		}

		if (isPeerMtModelIPHONE()) {
			return true;
		}

		return false;
	}

	/**
	 * 对端型号是否为Android Phone
	 * 
	 * @return
	 */
	public boolean isPeerMtModelAndroidPhone() {

		return emPeerMtModel == EmMtModel.emAndroid_Phone;
	}

	/**
	 * 对端型号是否为Android Phone
	 * 
	 * @return
	 */
	public boolean isPeerMtModelIPHONE() {

		return emPeerMtModel == EmMtModel.emIPHONE
				|| emPeerMtModel == EmMtModel.emIPHONE4S
				|| emPeerMtModel == EmMtModel.emIPHONE5;
	}

	/**
	 * 当前呼叫状态是否为空闲状态
	 * 
	 * @return
	 */
	public boolean isCSIDLE() {
		return emCallState == EmCallState.IDLE.ordinal();
	}

	/**
	 * 是否为正在发起呼叫状态
	 * 
	 * @return
	 */
	public boolean isCSCalling() {
		return emCallState == EmCallState.CALLING.ordinal();
	}

	/**
	 * 点对点会议 emCS_P2P
	 * 
	 * @return
	 */
	public boolean isCSP2P() {
		return emCallState == EmCallState.P2P.ordinal();
	}

	/**
	 * 多点会议
	 * 
	 * @return
	 */
	public boolean isCSMCC() {
		return emCallState == EmCallState.MCC.ordinal();
	}

	/**
	 * 挂断
	 * 
	 * @return
	 */
	public boolean isCSHanup() {
		return emCallState == EmCallState.HANUP.ordinal();
	}

	/**
	 * 是否正处于音视频状态
	 * 
	 * @return
	 */
	public boolean isVConf() {
		return isCSMCC() || isCSP2P();
	}

	/**
	 * 是否是音频
	 * 
	 * @return
	 */
	public boolean isAudio() {

		return wCallRate == VideoConferenceService.CALLRATE_SPLITLINE;
	}

	/**
	 * 视频
	 * 
	 * @return
	 */
	public boolean isVideo() {

		return wCallRate > VideoConferenceService.CALLRATE_SPLITLINE;
	}

	/**
	 * 音视频断链原因
	 * 
	 * @return
	 */
	public String getCallDisconnectReason() {
		// return EmCallDisconnectReason.getCallDisconnectReason(getReason());
		return "失败";
	}

}
