/**
 * @(#)EmGKRegFailedReason.java 2013-10-21 Copyright 2013 it.kedacom.com, Inc.
 *                              All rights reserved.
 * 
 *                              <pre>
 * 
 * 
 * 
 * enum EmGKRegFailedReason {
 * 	emNone, emGKUnReachable, emInvalidAliase, emDupAlias, emInvalidCallAddress, emResourceUnavailable, emUnknown, emRegNumberFull, // 注册数量满，PCMT绑定GK失败消息提示
 * 	emGKSecurityDenial, // GK注册权限失败
 * 	emGKDismatch, // GK不是运营版本,服务器不匹配
 * 	emUnRegGKReq, // GK被抢登后，要求注销GK
 * 	emLRRQTimeout // 等待轻量级注册结果超时
 * };
 * </pre>
 */

package com.kedacom.truetouch.mtc;

/**
 * @author chenjian
 * @date 2013-10-21
 */

public enum EmGKRegFailedReason {
	// @formatter:off

		emNone, 
		emGKUnReachable, // 不可达
		emInvalidAliase,  // 别名无效
		emDupAlias, // 别名重复
		emInvalidCallAddress, // 地址无效
		emResourceUnavailable, // 资源无效
		emUnknown, 
		emRegNumberFull, // 注册数量满，PCMT绑定GK失败消息提示(登录失败，GK授权数已达上限)
		emGKSecurityDenial, // GK注册权限失败
		emGKDismatch, // GK不是运营版本,服务器不匹配
		emUnRegGKReq, // GK被抢登后，要求注销GK
		emLRRQTimeout; // 等待轻量级注册结果超时

		// @formatter:on

	public static EmGKRegFailedReason toEmGKRegFailedReason(int ordinal) {
		if (ordinal == emNone.ordinal()) {
			return emNone;
		}

		if (ordinal == emGKUnReachable.ordinal()) {
			return emGKUnReachable;
		}

		if (ordinal == emInvalidAliase.ordinal()) {
			return emInvalidAliase;
		}

		if (ordinal == emDupAlias.ordinal()) {
			return emDupAlias;
		}

		if (ordinal == emInvalidCallAddress.ordinal()) {
			return emInvalidCallAddress;
		}

		if (ordinal == emResourceUnavailable.ordinal()) {
			return emResourceUnavailable;
		}

		if (ordinal == emUnknown.ordinal()) {
			return emUnknown;
		}

		if (ordinal == emRegNumberFull.ordinal()) {
			return emRegNumberFull;
		}

		if (ordinal == emGKSecurityDenial.ordinal()) {
			return emGKSecurityDenial;
		}

		if (ordinal == emGKDismatch.ordinal()) {
			return emGKDismatch;
		}

		if (ordinal == emUnRegGKReq.ordinal()) {
			return emUnRegGKReq;
		}

		if (ordinal == emLRRQTimeout.ordinal()) {
			return emLRRQTimeout;
		}

		return emNone;

	}
}
