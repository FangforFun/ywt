/**
 * @(#)CfgH323SettingConferenceCallback.java 2013-11-4 Copyright 2013
 *                                           it.kedacom.com, Inc. All rights
 *                                           reserved.
 * 
 *                                           <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * <EventID>29581</EventID>
 * <Message>
 * 	<CfgH323SettingConference>
 * 		<Result>1</Result>
 * 		<H323Cfg>
 * 			<Alias>1258051</Alias>
 * 			<E164>1258051</E164>
 * 			<IsUseGk>1</IsUseGk>
 * 			<GKPwd>0</GKPwd>
 * 			<GkIp>172.16.79.8</GkIp>
 * 			<IsH239Enable>0</IsH239Enable>
 * 			<IsEnctyptEnable>1</IsEnctyptEnable>
 * 			<EncrptMode>3</EncrptMode>
 * 			<RoundTrip>30</RoundTrip>
 * 		</H323Cfg>
 * 		</CfgH323SettingConference>
 * </Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;
import android.util.Log;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.login.LoginFlowService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;

/**
 * @author chenjian
 * @date 2013-11-4
 */

public class CfgH323SettingConferenceCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		loadParseSingleXML(strXML);

		if (!result4SingleXML()) {
			// 正在注册GK
			if (LoginFlowService.mRegisteringGK) {
				LoginFlowService.mRegisteringGK = false;

				final Activity currActivity = AppStackManager.Instance().currentActivity();
//				if (currActivity != null && currActivity instanceof LoginActivity) {
//					((LoginActivity) currActivity).loginFailed(null, false);
//					return;
//				}
			}

			return;
		}

		String alias = getSingleObject("Alias");
		String e164 = getSingleObject("E164");
		String isUseGk = getSingleObject("IsUseGk");
		String gkPwd = getSingleObject("GKPwd");
		String gkIp = getSingleObject("GkIp");
		String isH239Enable = getSingleObject("IsH239Enable");
		String isEnctyptEnable = getSingleObject("IsEnctyptEnable");
		String encrptMode = getSingleObject("EncrptMode");
		String roundTrip = getSingleObject("RoundTrip");

		// 2776
		// 正在注册GK
		if (LoginFlowService.mRegisteringGK) {
			Log.i("Login flow", "SetFireWallCfgGK, Ip:" + gkIp);
			KdvMtBaseAPI.setFireWallCfg(true, gkIp, (short) 2776, (short) 0);
		}
	}
}
