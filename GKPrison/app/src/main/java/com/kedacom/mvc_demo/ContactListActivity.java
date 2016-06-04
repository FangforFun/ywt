package com.kedacom.mvc_demo;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.kdv.mt.sdkapi.KdvMtBaseAPI;
import com.kedacom.mvc_demo.vconf.bean.TagTMTAddr;

import java.util.List;


public class ContactListActivity extends ListActivity {

	private ListView mListView;
	private P2PCallDialog mP2PCallDialog;
	private ContactAdapter mContactAdapter;
	public CallingDialog mCallingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppStackManager.Instance().pushActivity(this);
		mListView = getListView();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (mContactAdapter == null) {
			return;
		}
		TagTMTAddr tmtAddr = mContactAdapter.getItem(position);
		if (null == tmtAddr) {
			return;
		}
		if (mP2PCallDialog == null) {
			mP2PCallDialog = new P2PCallDialog(ContactListActivity.this);
		}
		mP2PCallDialog.setE164(tmtAddr.mE164Info.mAlias);
		mP2PCallDialog.show();
	}

	@Override
	protected void onDestroy() {
		AppStackManager.Instance().popActivity(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public void showList(final List<TagTMTAddr> contactLis) {
		if (null == mListView) {
			return;
		}
		if (null == contactLis || contactLis.isEmpty()) {
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mContactAdapter = new ContactAdapter(ContactListActivity.this,
						contactLis);
				mListView.setAdapter(mContactAdapter);
			}
		});
	}

	public void closeCallingDialog() {
		if (mCallingDialog == null) {
			return;
		}
		mCallingDialog.dismiss();
	}
}
