package com.gkzxhn.gkprison.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

public class UserInfoActivity extends BaseActivity {

    private final String[] USER_INFO_LEFT_TVS = {"姓名", "性别", "出生年月", "身份证号", "与服刑人员关系", "服刑人员囚号", "服刑人员刑期", "联系电话"};
    private final String[] USER_INFO_RIGHT_TVS = {"叶美惠", "女", "1968年09月09日", "4304787387887788", "母子", "217459123", "3年3个月", "13232324328"};

    private ListView lv_user_info;

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.activity_user_info, null);
        lv_user_info = (ListView) view.findViewById(R.id.lv_user_info);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("个人信息");
        setBackVisibility(View.VISIBLE);
        lv_user_info.setAdapter(new MyAdapter());
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return USER_INFO_LEFT_TVS.length;
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
            ViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(mContext, R.layout.user_info_item, null);
                holder = new ViewHolder();
                holder.tv_user_info_left = (TextView) convertView.findViewById(R.id.tv_user_info_left);
                holder.tv_user_info_right = (TextView) convertView.findViewById(R.id.tv_user_info_right);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_user_info_left.setText(USER_INFO_LEFT_TVS[position]);
            holder.tv_user_info_right.setText(USER_INFO_RIGHT_TVS[position]);
            return convertView;
        }
    }

    private static class ViewHolder{
        TextView tv_user_info_left;
        TextView tv_user_info_right;
    }
}
