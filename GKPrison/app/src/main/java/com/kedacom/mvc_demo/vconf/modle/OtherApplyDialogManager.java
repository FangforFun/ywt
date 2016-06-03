/**
 * @(#)OtherApplyDialogManager.java   2014-6-6
 * Copyright 2014  it.kedacom.com, Inc. All rights reserved.
 */

package com.kedacom.mvc_demo.vconf.modle;

import android.app.Activity;

import com.kedacom.mvc_demo.vconf.bean.MtInfo;
import com.kedacom.mvc_demo.vconf.ui.OtherApplyDialog;

/**
 * @author chenj
 * @date 2014-6-6
 */

public class OtherApplyDialogManager {

	// 申请管理员
	public static OtherApplyDialog mOtherApplyChairDialog;
	public static OtherApplyDialog mOtherApplySpeakDialog;

	/**
	 * 申请主席权限弹出框
	 * @param activity
	 * @param mtInfo
	 */
	public static void showOtherApplyChairDialog(final Activity activity, final MtInfo mtInfo) {
		if (null == mOtherApplyChairDialog) {
			mOtherApplyChairDialog = new OtherApplyDialog(true);
		}

		if (mOtherApplySpeakDialog != null) {
			mOtherApplySpeakDialog.cancelDialog();
		}

		mOtherApplyChairDialog.showApplyDialog(activity, mtInfo);
	}

	/**
	 * 释放申请主席权限弹出框
	 */
	public static void releaseOtherApplyChairDialog() {
		if (null == mOtherApplyChairDialog) return;

		mOtherApplyChairDialog.release();
		mOtherApplyChairDialog = null;
	}

	/**
	 * 申请主讲权限弹出框
	 * @param activity
	 * @param mtInfo
	 */
	public static void showOtherApplySpeakDialog(final Activity activity, final MtInfo mtInfo) {
		if (null == mOtherApplySpeakDialog) {
			mOtherApplySpeakDialog = new OtherApplyDialog();
		}

		if (mOtherApplyChairDialog != null) {
			mOtherApplyChairDialog.cancelDialog();
		}

		mOtherApplySpeakDialog.showApplyDialog(activity, mtInfo);
	}

	/**
	 * 释放申请主讲权限弹出框
	 */
	public static void releaseOtherApplySpeakDialog() {
		if (null == mOtherApplySpeakDialog) return;

		mOtherApplySpeakDialog.release();
		mOtherApplySpeakDialog = null;
	}

}
