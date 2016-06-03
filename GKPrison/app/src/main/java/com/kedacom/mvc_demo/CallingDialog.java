package com.kedacom.mvc_demo;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.gkzxhn.gkprison.R;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.mvc_demo.vconf.modle.service.VideoCapServiceManager;

public class CallingDialog extends Dialog {

	public CallingDialog(Context context) {
		super(context);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setTitle("呼叫中...请稍候");
		setContentView(R.layout.dialog_calling);
		VideoCapServiceManager.bindService();
		findViewById(R.id.btnCancel).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (VideoConferenceService.isCSVConf()) {
							return;
						}
				KdvMtBaseAPI.cancelCalling();
						VideoConferenceService.quitConfAction(false);

						dismiss();
					}
				});
	}
}
