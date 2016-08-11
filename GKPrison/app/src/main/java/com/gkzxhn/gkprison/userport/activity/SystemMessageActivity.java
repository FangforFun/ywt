package com.gkzxhn.gkprison.userport.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.bean.SystemMessage;
import com.gkzxhn.gkprison.utils.SPUtil;
import com.gkzxhn.gkprison.utils.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * created by hzn 2016/1/1
 * 系统消息页面
 */
public class SystemMessageActivity extends BaseActivity {

    private ListView lv_system_msg;
    private final String[] LEFT_TVS = {"您的探监申请已通过", "您的探监申请未通过", "您的会见申请已通过", "您的会见申请未通过", "系统更新", "监狱长信箱有新的回复"};
    private List<SystemMessage> messageList = new ArrayList<>();
    private String database_path = getFilesDir().getPath() + "/databases/chaoshi.db";
    private SQLiteDatabase db = SQLiteDatabase.openDatabase(database_path, null, SQLiteDatabase.OPEN_READWRITE);
    private SystemMsgAdapter msgAdapter;
    private ImageView iv_no_system_message;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_system_message, null);
        lv_system_msg = (ListView) view.findViewById(R.id.lv_system_msg);
        iv_no_system_message = (ImageView) view.findViewById(R.id.iv_no_system_message);
        return view;
    }

    @Override
    protected void initData() {
        SPUtil.put(this, "has_new_notification", false);
        messageList.clear();
        setTitle("系统消息");
        setBackVisibility(View.VISIBLE);
        getMessageList();
        for (SystemMessage systemMessage : messageList) {
            Log.i("消息数据。。。", systemMessage.toString());
        }
        if (messageList.size() > 0) {
            Collections.reverse(messageList);
        }
        if (msgAdapter == null) {
            msgAdapter = new SystemMsgAdapter();
            lv_system_msg.setAdapter(msgAdapter);
        } else {
            msgAdapter.notifyDataSetChanged();
        }
        if (messageList.size() == 0) {
            iv_no_system_message.setVisibility(View.VISIBLE);
        } else {
            iv_no_system_message.setVisibility(View.GONE);
        }
    }

    /**
     * 获取消息列表
     */
    private void getMessageList() {
        Cursor cursor = db.query("sysmsg", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            SystemMessage systemMessage = new SystemMessage();
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String apply_date = cursor.getString(cursor.getColumnIndex("apply_date"));
            int type_id = cursor.getInt(cursor.getColumnIndex("type_id"));
            String is_read = cursor.getString(cursor.getColumnIndex("is_read"));
            String result = cursor.getString(cursor.getColumnIndex("result"));
            String meeting_date = cursor.getString(cursor.getColumnIndex("meeting_date"));
            String reason = cursor.getString(cursor.getColumnIndex("reason"));
            String receive_time = cursor.getString(cursor.getColumnIndex("receive_time"));
            String user_id = cursor.getString(cursor.getColumnIndex("user_id"));
            if (user_id.equals(SPUtil.get(SystemMessageActivity.this, "username", "") + "")) {
                systemMessage.setMsg_receive_time(receive_time);
                systemMessage.setReason(reason);
                systemMessage.setName(name);
                systemMessage.setApply_date(apply_date);
                systemMessage.setType_id(type_id);
                systemMessage.setIs_read(Boolean.parseBoolean(is_read));
                systemMessage.setResult(result);
                systemMessage.setMeeting_date(meeting_date);
                systemMessage.setUser_id(user_id);
                if (!messageList.contains(systemMessage)) {
                    messageList.add(systemMessage);
                }
            }
        }
        cursor.close();
        if (msgAdapter == null) {
            msgAdapter = new SystemMsgAdapter();
            lv_system_msg.setAdapter(msgAdapter);
        } else {
            msgAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messageList.size() == 0) {
            iv_no_system_message.setVisibility(View.VISIBLE);
        } else {
            iv_no_system_message.setVisibility(View.GONE);
        }
        if (msgAdapter == null) {
            msgAdapter = new SystemMsgAdapter();
            lv_system_msg.setAdapter(msgAdapter);
        } else {
            msgAdapter.notifyDataSetChanged();
        }
    }

    private class SystemMsgAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return messageList.size();
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
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.system_msg_item, null);
                holder = new SystemMsgViewHolder();
                holder.tv_system_msg_left = (TextView) convertView.findViewById(R.id.tv_system_msg_left);
                holder.tv_system_msg_right = (TextView) convertView.findViewById(R.id.tv_system_msg_right);
                holder.tv_system_msg_time = (TextView) convertView.findViewById(R.id.tv_system_msg_time);
                convertView.setTag(holder);
            } else {
                holder = (SystemMsgViewHolder) convertView.getTag();
            }
            String msg_time = messageList.get(position).getMsg_receive_time();
            long msg_mills = 0;
            try {
                msg_mills = StringUtils.formatToMill(msg_time, "yyyy-MM-dd HH:mm:ss");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (StringUtils.isCurrentYear(msg_mills)) {// 是否是今年的消息
                if (StringUtils.isToday(msg_mills)) {// 是否是今天的消息
                    holder.tv_system_msg_time.setText(StringUtils.formatTime(msg_mills, "HH:mm"));
                } else {// 不是今天的精确到某月某日
                    holder.tv_system_msg_time.setText(StringUtils.formatTime(msg_mills, "MM-dd HH:mm"));
                }
            } else {// 不是今年的时间显示精确到年份
                holder.tv_system_msg_time.setText(StringUtils.formatTime(msg_mills, "yyyy-MM-dd HH:mm"));
            }
            if (messageList.get(position).getType_id() == 1) {// 会见
                if (messageList.get(position).getResult().contains("已通过")) {
                    holder.tv_system_msg_left.setText(LEFT_TVS[2]);
                } else {
                    holder.tv_system_msg_left.setText(LEFT_TVS[3]);
                }
            } else if (messageList.get(position).getType_id() == 2) {// 探监
                if (messageList.get(position).getResult().contains("已通过")) {
                    holder.tv_system_msg_left.setText(LEFT_TVS[0]);
                } else {
                    holder.tv_system_msg_left.setText(LEFT_TVS[1]);
                }
            } else {
            }
            if (!messageList.get(position).is_read()) {
                holder.tv_system_msg_right.setText("点击查看详情");
                holder.tv_system_msg_right.setTextColor(getResources().getColor(R.color.theme));
            } else {
                holder.tv_system_msg_right.setText("已查看");
                holder.tv_system_msg_right.setTextColor(getResources().getColor(R.color.tv_mid));
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (messageList.get(position).getType_id() == 1 || messageList.get(position).getType_id() == 2) {
                        messageList.get(position).setIs_read(true);
                        ContentValues values = new ContentValues();
                        values.put("is_read", "true");
                        db.update("sysmsg", values, " apply_date = ? and meeting_date = ? ", new String[]{messageList.get(position).getApply_date(), messageList.get(position).getMeeting_date()});
                        intent = new Intent(SystemMessageActivity.this, ApplyResultActivity.class);
                        intent.putExtra("type_id", messageList.get(position).getType_id());
                        intent.putExtra("result", messageList.get(position).getResult());
                        intent.putExtra("apply_date", messageList.get(position).getApply_date());
                        intent.putExtra("meeting_date", messageList.get(position).getMeeting_date());
                        intent.putExtra("name", messageList.get(position).getName());
                        intent.putExtra("reason", messageList.get(position).getReason());
                        startActivity(intent);
                    } else {

                    }
                }
            });
            return convertView;
        }
    }

    private static class SystemMsgViewHolder {
        TextView tv_system_msg_left;
        TextView tv_system_msg_right;
        TextView tv_system_msg_time;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
