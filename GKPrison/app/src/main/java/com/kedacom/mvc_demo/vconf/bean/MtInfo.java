/**
 * @(#)MtInfo.java 2013-8-9 Copyright 2013 it.kedacom.com, Inc. All rights
 *                 reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

/**
 * @author chenjian
 * @date 2013-8-9
 */

public class MtInfo {

	public MtInfo() {
		// TODO 尚未实现
	}

	public MtInfo(String alias, MtId mtId) {
		this.mAlias = alias;
		this.mMtId = mtId;
	}

	public MtId mMtId;

	// 别名
	public String mAlias;

	@Override
	public boolean equals(Object o) {
		MtInfo info = (MtInfo) o;
		if (this.mAlias == info.mAlias && this.mMtId.equals(info.mMtId)) {
			return true;
		}
		return false;
	}
}
