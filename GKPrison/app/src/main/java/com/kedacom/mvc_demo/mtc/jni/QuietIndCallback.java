/**
 * <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29101</EventID>
 * <Message>
 * <QuietInd>
 * 	<Result>1</Result>
 * 	<bIsQuiet>0</bIsQuiet>
 * </QuietInd>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */
package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.mvc_demo.vconf.controller.VConfVideoActivity;
import com.kedacom.mvc_demo.vconf.ui.VConfFunctionView;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;
import com.pc.utils.StringUtils;

/**
 * 静音
 * @author chenjian
 * @date 2013-6-6
 */
public class QuietIndCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {

		loadParseSingleXML(strXML);

		if (!result4SingleXML()) {
			return;
		}

		boolean bIsQuiet = StringUtils.equals("1", getSingleObject("bIsQuiet"));

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
			vconfFunctionView.setQuietImageView(bIsQuiet);
		}
	}

}
