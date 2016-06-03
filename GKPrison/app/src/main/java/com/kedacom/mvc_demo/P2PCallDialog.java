package com.kedacom.mvc_demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.pc.utils.ValidateUtils;

public class P2PCallDialog extends Dialog {

	private String e164;

	public P2PCallDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("是否进行点对点呼叫？");
		setContentView(R.layout.dialog_p2pcall);
		findViewById(R.id.btnCancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
		// 呼叫
		findViewById(R.id.btnP2PCall).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(!VideoConferenceService.isAvailableVCconf(true, true, true)) {
							dismiss();
							return;
						}
						if (ValidateUtils.isIP(e164)) {
							VideoConferenceService.makeCallVideo(e164, e164);
						} else {
							VideoConferenceService.makeCallVideo("", e164);
						}
						dismiss();
						Activity currActivity = AppStackManager.Instance().currentActivity();
//								if(currActivity instanceof MenuActivity) {
//									((MenuActivity)currActivity).mCallingDialog = new CallingDialog(currActivity);
//									((MenuActivity)currActivity).mCallingDialog.show();
//								} else
							if(currActivity instanceof ContactListActivity) {
							((ContactListActivity)currActivity).mCallingDialog = new CallingDialog(currActivity);
							((ContactListActivity)currActivity).mCallingDialog.show();
						}
					}
				});
	}

	public String getE164() {
		return e164;
	}

	public void setE164(String e164) {
		this.e164 = e164;
	}

}
