package com.kedacom.mvc_demo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gkzxhn.gkprison.R;

public class IncomingCallDialog extends Dialog {
	
	private Button mAcceptButton; 
	private Button mRefuseButton;

	public IncomingCallDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setTitle("会议来电");
		setContentView(R.layout.dialog_incomingcall);
		
		findViewById(R.id.btnRefuse).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
	}
	
	

}
