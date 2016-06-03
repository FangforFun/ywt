/**
 * @(#)SysAppInitCompleteCallback.java 2013-6-6 Copyright 2013 it.kedacom.com,
 *                                     Inc. All rights reserved.
 */

package com.kedacom.mvc_demo.mtc.jni;

import com.gkzxhn.gkprison.application.MyApplication;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;
import com.pc.utils.StringUtils;

/**
 * start xmpp
 * @author chenjian
 * @date 2013-6-6
 */
public class SysAppInitCompleteCallback extends BaseCallbackHandler {

	/**
	 * 已经和黄学峰确认过，SysAppInitComplete只要有数据返回则Result一定是1，不可能为0
	 */
	@Override
	public void addCallback(String strXML) {
		if (StringUtils.isNull(strXML)) {
			return;
		}

		loadParseSingleXML(strXML);

		if (result4SingleXML()) {// 成功
		} else {// 失败
			KdvMtBaseAPI.start(35, MyApplication.getMediaLibDir());
		}
	}
}
