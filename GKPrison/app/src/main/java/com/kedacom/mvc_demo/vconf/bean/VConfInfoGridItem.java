/**
 * @(#)VConfInfoGridItem.java 2013-11-8 Copyright 2013 it.kedacom.com, Inc. All
 *                            rights reserved.
 */

package com.kedacom.mvc_demo.vconf.bean;

/**
 * @author chenjian
 * @date 2013-11-8
 */

public class VConfInfoGridItem {

	public final static int NUM_LOW = 4;

	public static final int NORMAL_VIEW = 0;
	public static final int ADD_VIEW = 1;
	public static final int DEL_VIEW = 2;
	public static final int EXTRA_VIEW = 3;

	public TMtInfoEx mTMtInfoEx;
	public int mViewType;

	public int getViewCount() {
		return 4;
	}
}
