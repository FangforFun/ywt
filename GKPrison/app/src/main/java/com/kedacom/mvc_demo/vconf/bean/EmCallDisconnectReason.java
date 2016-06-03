/**
 * @(#)LinkStateEmReason.java 2013-12-20 Copyright 2013 it.kedacom.com, Inc. All
 *                            rights reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;


/**
 * 呼叫挂断原因
 * 
 * <pre>
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
 * </pre>
 * @author chenjian
 * @date 2013-12-20
 */

public enum EmCallDisconnectReason {
	// @formatter:off
	start,
	
	busy, //对端忙
	normal, //正常挂断
	rejected, //对端拒绝
	unreachable , //对端不可达
	local, //本地原因
	nnknown, //未知原因
	custom, //自定义原因
	adaptiveBusy,		 // 接入电话终端失败
	joinconftimeout,		// 参加会议超时
	createconftimeout,		// 召集会议超时
	nomediaresource,		// 没有媒体资源
	exceedmaxinconfmtnum,	// 超过会议最大终端数（参加会议时）
	exceedmaxconfnum,		// 超过会议最大数（创建会议时）
	encrypeErr, // 与会议加密模式不符
	p2pTimeout, // 点对点呼叫超时

	mccDrop, // 会控挂断
	chairDrop, // 主席挂断
	mMcuDrop, // 上级会议挂断
	confRelease, // 会议结束挂断

	peerInConf, //正在会议中
	peerNoDisturb, //免打扰
	notInPeerContact, //非好友
	end;
	
	// @formatter:no
	
	
}
