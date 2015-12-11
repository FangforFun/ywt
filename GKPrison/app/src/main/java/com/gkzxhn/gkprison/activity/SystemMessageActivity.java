package com.gkzxhn.gkprison.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

public class SystemMessageActivity extends BaseActivity {

    private ListView lv_system_msg;
    private final String[] LEFT_TVS = {"您的探监申请已通过", "您的探监申请未通过", "您的探监申请已通过", "您的探监申请未通过", "系统更新", "监狱长信箱有新的回复"};

    @Override
    protected View initView() {
        View view = View.inflate(mContext, R.layout.activity_system_message, null);
        lv_system_msg = (ListView) view.findViewById(R.id.lv_system_msg);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("系统消息");
        setBackVisibility(View.VISIBLE);
        lv_system_msg.setAdapter(new SystemMsgAdapter());
        lv_system_msg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showToastMsgShort("点击查看详情...");
            }
        });
    }

    private class SystemMsgAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return LEFT_TVS.length;
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
            SystemMsgViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(mContext, R.layout.system_msg_item, null);
                holder = new SystemMsgViewHolder();
                holder.tv_system_msg_left = (TextView) convertView.findViewById(R.id.tv_system_msg_left);
                holder.tv_system_msg_right = (TextView) convertView.findViewById(R.id.tv_system_msg_right);
                convertView.setTag(holder);
            }else {
                holder = (SystemMsgViewHolder) convertView.getTag();
            }
            holder.tv_system_msg_left.setText(LEFT_TVS[position]);
            if(position % 2 == 0){
                holder.tv_system_msg_right.setText("点击查看详情");
                holder.tv_system_msg_right.setTextColor(getResources().getColor(R.color.theme));
            }else {
                holder.tv_system_msg_right.setText("已查看");
                holder.tv_system_msg_right.setTextColor(getResources().getColor(R.color.tv_mid));
            }
            return convertView;
        }
    }

    private static class SystemMsgViewHolder{
        TextView tv_system_msg_left;
        TextView tv_system_msg_right;
    }
}
