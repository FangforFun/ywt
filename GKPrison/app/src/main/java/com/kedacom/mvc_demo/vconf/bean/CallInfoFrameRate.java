/**
 * @(#)CallInfoFrameRate.java 2013-8-16 Copyright 2013 it.kedacom.com, Inc. All
 *                            rights reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

/**
 * 呼叫统计信息增加呼叫帧率
 * 
 * <pre>
 * typedef struct tagTCallInfoFrameRate
 * {
 *     u8 m_byPriomEncFrame;
 *     u8 m_byPriomDecFrame;
 *     u8 m_bySecondEncFrame;
 *     u8 m_bySecondDecFrame;
 * public:
 *     tagTCallInfoFrameRate() { memset( this, 0, sizeof(struct tagTCallInfoFrameRate) ); }
 * }
 * </pre>
 * @author chenjian
 * @date 2013-8-16
 */

public class CallInfoFrameRate {

	public String mPriomEncFrame;
	public String mPriomDecFrame;
	public String mSecondEncFrame;
	public String mSecondDecFrame;
}
