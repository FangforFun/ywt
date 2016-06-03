/**
 * @(#)EmConfListType.java 2013-9-23 Copyright 2013 it.kedacom.com, Inc. All
 *                         rights reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

/**
 * @author chenjian
 * @date 2013-9-23
 */

public enum EmConfListType
{
	// enum EmConfListType
	// {
	// emConfListType_Hold = 0, //正在召开
	// emConfListType_Subscribe, //预约
	// emConfListType_Idle, //空闲
	// emConfListType_All, //全部
	// emConfListTypeEnd
	// };

	/**
	 * 正在召开
	 */
	emConfListType_Hold,

	/**
	 * 预约
	 */
	emConfListType_Subscribe,

	/**
	 * 空闲
	 */
	emConfListType_Idle,

	/**
	 * 全部
	 */
	emConfListType_All
}
