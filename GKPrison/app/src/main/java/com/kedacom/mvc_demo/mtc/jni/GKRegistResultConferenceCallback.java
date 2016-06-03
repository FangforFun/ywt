/**
 * @(#)GKRegistResultConferenceCallback.java 2013-6-7 Copyright 2013
 *                                           it.kedacom.com, Inc. All rights
 *                                           reserved.
 * 
 *                                           <pre>
 *   ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * 		<TrueTouchAndroid>
 * 		<EventID>
 * 29298
 * </EventID>
 * 		<Message>
 * <GKRegistResultConference>
 * 		<Result>1</Result>
 * 		<FailedID>0</FailedID>
 * 		<Description></Description>
 * 		<Accessable>%d</Accessable>
 * 		<NonAccessableReason>%d</NonAccessableReason>
 * </GKRegistResultConference>
 * </Message>
 * 		</TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */

package com.kedacom.mvc_demo.mtc.jni;

import android.app.Activity;
import android.util.Log;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.gkzxhn.gkprison.application.MyApplication;
import com.kedacom.truetouch.mtc.MtcLib;
import com.pc.utils.NetWorkUtils;
import com.pc.utils.StringUtils;
import com.kedacom.mvc_demo.login.LoginFlowService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;
import com.kedacom.truetouch.mtc.EmGKRegFailedReason;

/**
 * @author chenjian
 * @date 2013-6-7
 */

public class GKRegistResultConferenceCallback extends BaseCallbackHandler {

	/**
	 * @see com.kedacom.mobilemvc.mtc.jni.BaseCallbackHandler#addCallback(String)
	 */
	@Override
	public void addCallback(String strXML) {
		Log.e("GKRegistResultConference", strXML);

		loadParseSingleXML(strXML);

		boolean preRegisterGK = LoginFlowService.mRegisterGK;
		boolean preRegisteringGK = LoginFlowService.mRegisteringGK;

		boolean result = result4SingleXML();
		boolean initialResult = result;

		// 授权是否成功，当result=1时才有效
		boolean accessable = StringUtils.equals(getSingleObject("Accessable"), "1");

		// 开启会商时，无需判断授权（即，授权始终成功）
		if (!accessable) {
			accessable = true;
		}

		// 授权失败,以注册失败处理
		if (initialResult && !accessable) {
			result = false;
		}

		LoginFlowService.mRegisterGK = result;

		if (!result) {
			// 如果之前GK注册成功，现在注册失败
			if (preRegisterGK) {
				// SlidingMenuManager.resetDialTitle(false, true);
			}

			int failedID = StringUtils.str2Int(getSingleObject("FailedID"), 0);

			// 以注册成功授权失败的方式进入GK注册失败流程时，failedID设置为emRegNumberFull
			if (initialResult && !accessable) {
				failedID = 7;
			}

			// String failed = ErrorCode.getGKRegistError(failedID);

			// 正在注册GK
			final Activity currActivity = AppStackManager.Instance().currentActivity();
//			if (currActivity != null && currActivity instanceof LoginActivity) {
//				if (LoginFlowService.mRegisteringGK) {
//					((LoginActivity) currActivity).loginFailed(null, false);
//				}

//				LoginFlowService.mRegisteringGK = false;
//				return;
//			}

			LoginFlowService.mRegisteringGK = false;

			if (failedID == EmGKRegFailedReason.emRegNumberFull.ordinal()) {
			}

			// GK被抢登
			else if (failedID == EmGKRegFailedReason.emUnRegGKReq.ordinal()) {
			} else if (NetWorkUtils.isAvailable(MyApplication.mOurApplication)) {
				if (failedID == EmGKRegFailedReason.emUnknown.ordinal() || failedID == EmGKRegFailedReason.emLRRQTimeout.ordinal()
						|| failedID == EmGKRegFailedReason.emResourceUnavailable.ordinal() || failedID == EmGKRegFailedReason.emInvalidCallAddress.ordinal()) {
					LoginFlowService.reRegGk();
				}
			}

			// SlidingMenuManager.updateContentTopFrame4GK();
			return;
		}

		LoginFlowService.mRegisteringGK = false;

		final Activity currActivity = AppStackManager.Instance().currentActivity();

		// 如果之前GK注册失败，现在注册成功
		if (!preRegisterGK) {
			// SlidingMenuManager.resetDialTitle(false, true);

//			 MtcLib.setNonSymmetricNet();

//			if (currActivity != null && currActivity instanceof LoginActivity && preRegisteringGK) {
			LoginFlowService.loginSuccessOrFail(true);
			Log.i("GK注册----", "注册成功啦");
//
//				Log.i("Login flow", "重新注册GK成功");
//
//				return;
//			}

			if (LoginFlowService.mSelectorDialog != null) {
				LoginFlowService.mSelectorDialog.dismiss();
				LoginFlowService.mSelectorDialog = null;
			}
		}

		if (!preRegisterGK) {
			// SlidingMenuManager.updateContentTopFrame4GK();
		}

		Log.i("GK注册.....", result + "啊啊啊啊啊 啊啊啊");
	}

}
