/**
 * @(#)ConfInfo.java 2013-8-9 Copyright 2013 it.kedacom.com, Inc. All rights
 *                   reserved.
 * 
 *                   <pre>
 *  //定义会议信息
 * typedef struct tagTMtConfInfo
 * {
 * 	s8          chConfId[MT_MAXLEN_CONFGUID+1];
 * 	TMtKdvTime    tStartTime;//开始时间，控制台填0为立即开始
 * 	u16           wDuration;   //持续时间(分钟)，0表示不自动停止
 *     u16           wBitRate;        //会议码率(单位:Kbps,1K=1024)
 *     u16           wSecBitRate;     //双速会议的第2码率(单位:Kbps,为0表示是单速会议)
 *     EmVideoResolution emMainVideoResolution;  //主视频格式
 *     EmVideoResolution emSecondVideoResolution; //辅视频格式
 *     EmVideoResolution emDoubleVideoResolution;   //第二路视频格式,
 *     u8            byTalkHoldTime;                 //最小发言持续时间(单位:秒)
 * 
 * 	s8            achConfPwd[MT_MAXLEN_PASSWORD+1];    //会议密码
 *     s8            achConfName[MT_MAX_CONF_NAME_LEN+1]; //会议名
 *     s8            achConfE164[MT_MAX_CONF_E164_LEN+1]; //会议的E164号码
 * 	
 * 	BOOL          bIsAudioPowerSel;   //是否语音激励
 * 	BOOL          bIsDiscussMode;     //是否讨论模式
 *     BOOL          bIsAutoVMP;            //是否自动多画面合成
 * 	BOOL         
 * 
 *  bIsCustomVMP;        //是否自定义多画面合成
 * 	BOOL          bIsForceBroadcast;//强制广播
 * 
 * 	/**************************************************************************** 
 * 	修改时间:		2013/01/11 9:34
 * 	修改人:			chenlijun 
 * 	=============================================================================
 * 	说明：		告知上层当前画面是否在对话模式
 * ***************************************************************************
 * 	BOOL		  bIsPairTalkMode; 			// 是否为对话模式
 *     TMtId 	      tChairman;	   //主席终端，MCU号为0表示无主席
 *     TMtId		  tSpeaker;		  //发言终端，MCU号为0表示无发言人
 *     TMtPollInfo   tPollInfo;        //会议轮询参数,仅轮询时有较
 *     TMtVMPParam   tVMPParam;        //当前视频复合参数，仅视频复合时有效
 *   
 * public:
 * 	tagTMtConfInfo(){ memset ( this ,0 ,sizeof( struct tagTMtConfInfo) );}
 * }TMtConfInfo ,*PTMtConfInfo;
 * </pre>
 */

package com.kedacom.mvc_demo.vconf.bean;

import com.pc.utils.StringUtils;

/**
 * @author chenjian
 * @date 2013-8-9
 */

public class ConfInfo {

	public String mConfId;

	// 开始时间，控制台填0为立即开始
	public String mStartTime;

	// 持续时间(分钟)，0表示不自动停止
	public String mDuration;

	// 会议码率(单位:Kbps,1K=1024)
	public String mBitRate;

	// 双速会议的第2码率(单位:Kbps,为0表示是单速会议)
	public String mSecBitRate;

	// 主视频格式
	public String mEmMainVideoResolution;

	// 辅视频格式
	public String mEmSecondVideoResolution;

	// 第二路视频格式,
	public String mEmDoubleVideoResolution;

	// 最小发言持续时间(单位:秒)
	public String mTalkHoldTime;

	// 会议密码
	public String mConfPwd;

	// 会议名
	public String mConfName;

	// 会议的E164号码
	public String mConfE164;

	// 是否语音激励
	public boolean mIsAudioPowerSel;

	// 是否讨论模式
	public boolean mIsDiscussMode;

	// 是否自动多画面合成
	public boolean mIsAutoVMP;

	// 是否自定义多画面合成
	public boolean mIsCustomVMP;

	// 强制广播
	public boolean mIsForceBroadcast;

	// 主席终端，MCU号为0表示无主席
	public MtId mChairman;

	// 发言终端，MCU号为0表示无发言人
	public MtId mSpeaker;

	// 会议轮询参数,仅轮询时有较
	public PollInfo mPollInfo;

	// 当前视频复合参数，仅视频复合时有效
	public VMPParam mVMPParam;

	/**
	 * 截取E164号后六位
	 * @return
	 */
	public String getSingleConfE164() {
		String split = "#";

		if (StringUtils.isNull(mConfE164) || !mConfE164.contains(split)) {
			return mConfE164;
		}

		int index = mConfE164.indexOf(split);
		String sigleConfE164 = mConfE164;
		try {
			sigleConfE164 = mConfE164.substring(index + 1);
		} catch (Exception e) {
		}

		return sigleConfE164;
	}
}
