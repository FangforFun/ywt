/**
 * @(#)EmMtAddrType.java   2013-10-21
 * Copyright 2013  it.kedacom.com, Inc. All rights reserved.
 
 <pre>
 //地址类型
enum EmMtAddrType
{
emIPAddr =0,
emE164   ,
emH323id ,
emDialNum,
emSipAddr,
emInvalidAddr,
};
 </pre>
 *
 *
 */

package com.pc.utils;

/**
 * @author chenjian
 * @date 2013-10-21
 */

public enum EmMtAddrType {
	emIPAddr, emE164, emH323id, emDialNum, emSipAddr, emInvalidAddr;

	/**
	 * to EmMtAddrType
	 * 
	 * @param addrType
	 * @return
	 */
	public static EmMtAddrType toEmMtAddrType(int addrType) {
		if (addrType == EmMtAddrType.emIPAddr.ordinal()) {
			return EmMtAddrType.emIPAddr;
		} else if (addrType == EmMtAddrType.emE164.ordinal()) {
			return EmMtAddrType.emE164;
		} else if (addrType == EmMtAddrType.emH323id.ordinal()) {
			return EmMtAddrType.emH323id;
		} else if (addrType == EmMtAddrType.emDialNum.ordinal()) {
			return EmMtAddrType.emDialNum;
		} else if (addrType == EmMtAddrType.emSipAddr.ordinal()) {
			return EmMtAddrType.emSipAddr;
		} else if (addrType == EmMtAddrType.emInvalidAddr.ordinal()) {
			return EmMtAddrType.emInvalidAddr;
		}

		return EmMtAddrType.emInvalidAddr;
	}
}
