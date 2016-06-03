/**
 * @ S.java 2013-11-26
 */

package com.kedacom.mvc_demo.vconf.modle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.kedacom.mvc_demo.vconf.bean.MtInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 申请管理员\主讲人弹出框适配器
 * @author chenj
 * @date 2014-6-6
 */
public class ApplyAdapter extends BaseAdapter implements OnItemClickListener {

	private Context mContext;
	private ListView mListView;
	private List<MtInfo> mApplyPersonInfos;
	private int mItemHeight;

	public ApplyAdapter(Context context, List<MtInfo> persons) {
		this.mContext = context;
		mApplyPersonInfos = new ArrayList<MtInfo>();
		if (null != persons && !persons.isEmpty()) {
			mApplyPersonInfos.addAll(persons);
		}
		mItemHeight = mContext.getResources().getDimensionPixelSize(R.dimen.default_setting_height);
	}

	public void setMtInfo(List<MtInfo> persons) {
		mApplyPersonInfos.clear();

		if (null != persons && !persons.isEmpty()) {
			mApplyPersonInfos.addAll(persons);
		}
	}

	public void setListView(ListView view) {
		this.mListView = view;
	}

	private int miniZise = 3;

	@Override
	public int getCount() {
		if (null == mApplyPersonInfos || mApplyPersonInfos.isEmpty()) {
			return miniZise;
		}

		if (mApplyPersonInfos.size() < miniZise) {
			return miniZise;
		}

		return mApplyPersonInfos.size();
	}

	@Override
	public MtInfo getItem(int position) {
		if (position < 0 || position >= getCount()) {
			return null;
		}
		try {
			return mApplyPersonInfos.get(position);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) LayoutInflater.from(mContext).inflate(R.layout.vconf_apply_item, null);
		MtInfo info = getItem(position);

		String alias = info != null ? info.mAlias : "";
		view.setText(alias);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
		view.setLayoutParams(params);
		if (position == selectedPosition) {
			view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.selected, 0);
		} else {
			view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}

		return view;
	}

	private int selectedPosition;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MtInfo info = getItem(position);
		if (null == info) {
			return;
		}

		selectedPosition = position;

		notifyDataSetChanged();
	}

	public MtInfo getSelectItem() {
		return getItem(selectedPosition);
	}

}
