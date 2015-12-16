package com.gkzxhn.gkprison.activity;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

public class SystemMessageActivity extends BaseActivity {

    private ListView lv_system_msg;
    private final String[] LEFT_TVS = {"您的探监申请已通过", "您的探监申请未通过", "您的会见申请已通过", "您的会见申请未通过", "系统更新", "监狱长信箱有新的回复"};

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_system_message, null);
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            SystemMsgViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(getApplicationContext(), R.layout.system_msg_item, null);
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
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    switch (position){
                        case 0:
                            intent = new Intent(SystemMessageActivity.this, ApplyResultActivity.class);
                            intent.putExtra("type", "探监已通过");
                            startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(SystemMessageActivity.this, ApplyResultActivity.class);
                            intent.putExtra("type", "探监未通过");
                            startActivity(intent);
                            break;
                        case 2:
                            intent = new Intent(SystemMessageActivity.this, ApplyResultActivity.class);
                            intent.putExtra("type", "会见已通过");
                            startActivity(intent);
                            break;
                        case 3:
                            intent = new Intent(SystemMessageActivity.this, ApplyResultActivity.class);
                            intent.putExtra("type", "会见未通过");
                            startActivity(intent);
                            break;
                        case 4:
                            intent = new Intent(SystemMessageActivity.this, VersionUpdateActivity.class);
                            startActivity(intent);
                            break;
                        case 5:
                            intent = new Intent(SystemMessageActivity.this, ApplyResultActivity.class);
                            intent.putExtra("type", "探监未通过");
                            startActivity(intent);
                            break;
                    }
                }
            });
            return convertView;
        }
    }

    private static class SystemMsgViewHolder{
        TextView tv_system_msg_left;
        TextView tv_system_msg_right;
    }
}
