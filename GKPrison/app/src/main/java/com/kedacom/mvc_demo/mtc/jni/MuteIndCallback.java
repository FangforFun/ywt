/**
 * <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29103</EventID>
 * <Message>
 * <MuteInd>
 * 	<Result>1</Result>
 * 	<bIsMute>0</bIsMute>
 * </MuteInd>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */
package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.pc.utils.StringUtils;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.mvc_demo.vconf.ui.VConfFunctionView;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;

/**
 * 是否哑音
 * @author chenjian
 * @date 2013-6-6
 */
public class MuteIndCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		loadParseSingleXML(strXML);

		if (!result4SingleXML()) {
			return;
		}

		String isMute = getSingleObject("bIsMute");
		boolean bIsMute = StringUtils.equals("1", isMute);

		Activity currActivity = AppStackManager.Instance().currentActivity();
		if (null == currActivity) {
			return;
		}

		VConfFunctionView vconfFunctionView = null;
		if (currActivity instanceof VConfVideoActivity) {
			vconfFunctionView = ((VConfVideoActivity) currActivity).getVConfFunctionView();
		}
		// else if (currActivity instanceof VConfAudioActivity) {
		// vconfFunctionView = ((VConfAudioActivity) currActivity).getVConfFunctionView();
		// }
		if (null != vconfFunctionView) {
			vconfFunctionView.setMuteImageView(bIsMute);
		}
	}
}
