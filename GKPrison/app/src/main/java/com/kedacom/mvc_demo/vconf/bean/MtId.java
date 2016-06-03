/**
 * @(#)MtInfo.java 2013-8-9 Copyright 2013 it.kedacom.com, Inc. All rights
 *                 reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

import com.pc.utils.StringUtils;

/**
 * @author chenjian
 * @date 2013-8-9
 */

public class MtId {

	public MtId() {
		// TODO 尚未实现
	}

	public MtId(String mcuNo, String terNo) {
		this.mMcuNo = mcuNo;
		this.mTerNo = terNo;
	}

	// mcu编号
	public String mMcuNo;

	// 终端编号
	public String mTerNo;

	@Override
	public boolean equals(Object o) {
		if (null == o) {
			return false;
		}

		MtId mtId = (MtId) o;
		return StringUtils.equals(mTerNo, mtId.mTerNo);
	}

	public short getMcuNo() {
		return StringUtils.str2Short(mMcuNo, (short) 0);
	}

	public short getTerNo() {
		return StringUtils.str2Short(mTerNo, (short) 0);
	}
}
