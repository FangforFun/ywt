package com.gkzxhn.gkprison.userport.ui.main.canteen.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

/**
 * Author: Huang ZN
 * Date: 2017/1/11
 * Email:943852572@qq.com
 * Description:选择分类条目
 */

public class AllChooseAdapter extends BaseAdapter {

    private static final String[] CHOOSE_ITEM = {"全部分类", "洗涤日化", "食品", "服饰鞋帽"};

    private Context mContext;

    public AllChooseAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return CHOOSE_ITEM.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.choose_item, null);
            holder = new Holder();
            holder.textView = (TextView) convertView.findViewById(R.id.tv_fenlei);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.textView.setText(CHOOSE_ITEM[position]);
        return convertView;
    }

    private class Holder {
        TextView textView;
    }
}
