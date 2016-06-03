package com.kedacom.mvc_demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.kedacom.mvc_demo.vconf.bean.TagTMTAddr;
import com.pc.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends BaseAdapter {
	private Context mContext;
	private List<TagTMTAddr> contactLis;

	public ContactAdapter(Context _Context, List<TagTMTAddr> _ContactLis) {
		mContext = _Context;
		contactLis = new ArrayList<TagTMTAddr>();
		if (_ContactLis != null && !_ContactLis.isEmpty()) {
			contactLis.addAll(_ContactLis);
		}
	}

	@Override
	public int getCount() {

		return contactLis.size();
	}

	@Override
	public TagTMTAddr getItem(int position) {

		return contactLis.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(mContext).inflate(
				R.layout.contact_item, null);
		TextView text1 = (TextView) convertView.findViewById(R.id.text1);
		TagTMTAddr tmAddr = getItem(position);
		if (null == tmAddr) {
			return convertView;
		}

		String alias = "";
		if (tmAddr.mE164Info != null) {
			alias = tmAddr.mE164Info.mAlias;
		}

		if (StringUtils.isNull(alias) && tmAddr.mH323IdInfo != null) {
			alias = tmAddr.mH323IdInfo.mAlias;
		}

		text1.setText(alias);

		return convertView;
	}
}
