/**
 * @(#)EmDoConfResult.java 2013-12-23 Copyright 2013 it.kedacom.com, Inc. All
 *                         rights reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;


/**
 * 参会创会结果
 * 
 * <pre>
 * enum EmDoConfResult
 * {
 * 	MT_CONF_SUCCESS = 0,
 * 	MT_CONF_SER_DB_ERR,
 * 	MT_CONF_TIMEOUT_ERR,
 * 	MT_CONF_NOTE164_ERR,
 * 	MT_CONF_DATA_ERR,
 * 	MT_CONF_MCU_NOIDLEVPU,			  // 没有足够能力的空闲媒体处理器
 * 	MT_CONF_MCU_MINIMEDIACONFMTNUM,	  // 超过小型媒体会议入会终端限制
 * 	MT_CONF_MCU_MAXCONFNUM,			  // 已达最大会议数
 * 	MT_CONF_MCU_ENCRYPEERR,			  // 与会议加密模式不符
 * 	MT_CONF_MCU_CONFNOTEXIST,		  // 会议不存在
 * 	MT_CONF_MCU_UNDEFINED,
 * 	MT_CONF_GK_MPCD_DISCONNECTED,     // MPCD热备重启
 * 	MT_CONF_MCU_MAXMTNUM,			// 终端数已达最大上限
 * 	MT_CONF_MCU_PWDERR,				//密码错误
 * 	MT_CONF_MCU_NSATPMS,            // 无卫星权限
 * 	MT_CONF_MCU_NSATDADDR,			// 没有组播地址
 *  MT_CONF_CALLERNUM_EXCEED
 * };
 * </pre>
 * @author chenjian
 * @date 2013-12-23
 */

public enum EmDoConfResult {
	// @formatter:off
	
	SUCCESS,
	SER_DB_ERR,
	TIMEOUT_ERR,
	NOTE164_ERR,
	DATA_ERR,

	// 没有足够能力的空闲媒体处理器
	MCU_NOIDLEVPU,
	
	// 超过小型媒体会议入会终端限制
	MCU_MINIMEDIACONFMTNUM,
	
	// 已达最大会议数
	MCU_MAXCONFNUM,
	
	// 与会议加密模式不符
	MCU_ENCRYPEERR,
	
	// 会议不存在
	MCU_CONFNOTEXIST,
	MCU_UNDEFINED,
	
	// MPCD热备重启
	GK_MPCD_DISCONNECTED,
	
	// 终端数已达最大上限
	MCU_MAXMTNUM,
	
	//密码错误
	MCU_PWDERR,
	
	// 无卫星权限
	MCU_NSATPMS,
	
	// 没有组播地址
	MCU_NSATDADDR,
	
	CALLERNUM_EXCEED;
	
	// @formatter:on

//	public static String getDoConfResult(EmCallDisconnectReason reason) {
//		if (null == reason) {
//			return "";
//		}
//
//		return getDoConfResult(reason.ordinal());
//	}
//
//	public static String getDoConfResult(int reason) {
//		String errorKey = "vconf_EmDoConfResult_" + reason;
//
//		return PcResourceUtils.getStringByKey(R.string.class, errorKey, "");
//	}

}
