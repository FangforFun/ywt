/**
 * @(#)ReceiveSimpleConfInfo.java 2013-8-9 Copyright 2013 it.kedacom.com, Inc.
 *                                All rights reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

/**
 * @author chenjian
 * @date 2013-8-9
 */

public class SimpleConfInfo
{

	// 发言终端,MCU号为0表示无发言人
	public MtId mSpeaker;

	// 主席终端,MCU号为0表示无主席
	public MtId mChairMan;

	// 是否是VAC模式,是否正在语音激励
	public boolean bIsVAC;

	// 是否是讨论模式
	public boolean bIsDisc;

	// 是否自动多画面合成
	public boolean bIsAutoVMP;

	// 是否自定义画面合成
	public boolean bIsCustomVMP;
}
