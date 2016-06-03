/**
 * @(#)LabelAssignCallback.java 2013-8-9 Copyright 2013 it.kedacom.com, Inc. All
 *                              rights reserved.
 * 
 *                              <pre>
 *   <?xml version="1.0" encoding="utf-8"?>
 *  <TrueTouchAndroid>
 *  <EventID>29308</EventID>
 *  <Message>
 *  <LabelAssign>
 *  	<Result>1</Result>
 *  	<McuNo>192</McuNo>
 *  	<TerNo>1</TerNo>
 *  </LabelAssign>
 *  </Message>
 *  </TrueTouchAndroid>
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import com.kedacom.mvc_demo.vconf.bean.LabelAssign;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;

/**
 * 多点会议时，本终端编号通知
 * 
 * @author chenjian
 * @date 2013-8-9
 */

public class LabelAssignCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		android.util.Log.i("vconf", "LabelAssign");

		loadParseSingleXML(strXML);
		if (!result4SingleXML()) {
			VideoConferenceService.mLabelAssign = null;
			return;
		}

		LabelAssign labelAssign = new LabelAssign();
		labelAssign.mCuNo = getSingleObject("McuNo");
		labelAssign.mTerNo = getSingleObject("TerNo");

		VideoConferenceService.mLabelAssign = labelAssign;
	}

}
