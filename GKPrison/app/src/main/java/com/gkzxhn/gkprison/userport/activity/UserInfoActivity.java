package com.gkzxhn.gkprison.userport.activity;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

/**
 * created by huangzhengneng on 2016/1/14
 * 账户信息页面
 */
public class UserInfoActivity extends BaseActivity {

    private final String[] USER_INFO_LEFT_TVS = {"姓名", "身份证号", "联系电话",  "与服刑人员关系","服刑人员性别", "服刑人员囚号", "服刑人员刑期"};
    private final String[] USER_INFO_RIGHT_TVS = {"叶美惠", "34523423423423534", "13232324328", "女", "母子", "217459123", "3年3个月"};

    private ListView lv_user_info;
    private SharedPreferences sp;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_user_info, null);
        lv_user_info = (ListView) view.findViewById(R.id.lv_user_info);
        return view;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        setTitle("账号信息");
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
                convertView = View.inflate(getApplicationContext(), R.layout.user_info_item, null);
                holder = new ViewHolder();
                holder.tv_user_info_left = (TextView) convertView.findViewById(R.id.tv_user_info_left);
                holder.tv_user_info_right = (TextView) convertView.findViewById(R.id.tv_user_info_right);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_user_info_left.setText(USER_INFO_LEFT_TVS[position]);
            holder.tv_user_info_right.setText(USER_INFO_RIGHT_TVS[position]);
            switch (position){
                case 0:// 姓名
                    holder.tv_user_info_right.setText(sp.getString("name", "叶美惠"));
                    break;
                case 1:// 身份证号
                    holder.tv_user_info_right.setText(sp.getString("password", "").substring(0, 4) + "******" + sp.getString("password", "").substring(sp.getString("password", "").length() - 4, sp.getString("password", "").length()));
                    break;
                case 2:// 联系电话
                    holder.tv_user_info_right.setText(sp.getString("username", ""));
                    break;
                case 3:// 与服刑人员关系
                    holder.tv_user_info_right.setText(sp.getString("relationship", ""));
                    break;
                case 4:// 服刑人员性别
                    if(sp.getString("gender", "").equals("m")) {
                            holder.tv_user_info_right.setText("男");
                        }else if(sp.getString("gender", "").equals("f")){
                            holder.tv_user_info_right.setText("女");
                        }
                    break;
                case 5:// 服刑人员囚号
                    holder.tv_user_info_right.setText(sp.getString("prisoner_number", ""));
                    break;
                case 6:// 服刑人员刑期
                    holder.tv_user_info_right.setText(sp.getString("prison_term_started_at", "") + " 至 " + sp.getString("prison_term_ended_at", ""));
                    break;
            }
            return convertView;
        }
    }

    private static class ViewHolder{
        TextView tv_user_info_left;
        TextView tv_user_info_right;
    }
}
