/**
 * @(#)EmCallState.java 2013-12-20 Copyright 2013 it.kedacom.com, Inc. All
 *                      rights reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

/**
 * 呼叫状态
 * 
 * <pre>
 *  EmCallState {
 * 	emCS_Idle, 
 * 	emCS_Calling, // 正在发起呼叫
 * 	emCS_P2P, // 点对点会议
 * 	emCS_MCC, // 多点会议
 * 	emCS_Hanup // 挂断
 * };
 * </pre>
 * @author chenjian
 * @date 2013-12-20
 */

public enum EmCallState {
	// @formatter:off
	
	IDLE,
	
	// 正在发起呼叫
	CALLING,
	
	// 点对点会议
	P2P,
	// 多点会议
	MCC, 
	
	// 挂断
	HANUP 
	
	// @formatter:on

}
