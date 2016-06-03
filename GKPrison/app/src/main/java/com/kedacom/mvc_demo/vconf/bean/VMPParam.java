/**
 * @(#)VMPParam.java 2013-8-9 Copyright 2013 it.kedacom.com, Inc. All rights
 *                   reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

import java.util.List;

/**
 * 当前视频复合参数，仅视频复合时有效
 * 
 * @author chenjian
 * @date 2013-8-9
 */

public class VMPParam
{

	public boolean mIsCustomVMP;
	public boolean mIsAutoVMP;
	public boolean mIsBroadcast;
	public String mEmStyle;

	public List<MtId> mMtList;
	public List<String> mMmbTypeList;

}
