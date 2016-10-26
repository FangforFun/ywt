package com.keda.vconf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kedacom.kdv.mt.api.Conference;
import com.kedacom.kdv.mt.bean.TMTEntityInfo;
import com.kedacom.kdv.mt.bean.TMtId;
import com.gkzxhn.gkprison.R;

public class ApplyDialog extends Dialog {

	private TextView msgView;
	private String msg;
	private TMTEntityInfo info;
	private boolean isApplyChairMan;

	public ApplyDialog(Context context, TMTEntityInfo info, boolean isApplyChairMan) {
		super(context);
		this.info = info;
		this.isApplyChairMan = isApplyChairMan;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isApplyChairMan) {
			setTitle("申请主席");
		} else {
			setTitle("申请主讲");
		}
		setContentView(R.layout.dialog_apply);

		msgView = (EditText) findViewById(R.id.msg);
		if (null != info.tMtAlias) msgView.setText(info.tMtAlias.getAlias());

		findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		// 同意
		findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (null != info) {
					TMtId mtId = new TMtId(info.dwMcuId, info.dwTerId);

					if (isApplyChairMan) {
						Conference.confChairSpecNewChairCmd(mtId);
					} else {
						Conference.confChairSpecSpeakerCmd(mtId);
					}
				}
				dismiss();
			}
		});
	}
}
