package com.gkzxhn.gkprison.userport.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.bean.SystemMessage;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * created by hzn 2016/1/1
 * 系统消息页面
 */
public class SystemMessageActivity extends BaseActivity {

    private ListView lv_system_msg;
    private final String[] LEFT_TVS = {"您的探监申请已通过", "您的探监申请未通过", "您的会见申请已通过", "您的会见申请未通过", "系统更新", "监狱长信箱有新的回复"};
    private List<SystemMessage> messageList = new ArrayList<>();
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private SystemMsgAdapter msgAdapter;

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_system_message, null);
        lv_system_msg = (ListView) view.findViewById(R.id.lv_system_msg);
        return view;
    }

    @Override
    protected void initData() {
        messageList.clear();
//        if("notification_click".equals(getIntent().getStringExtra("type"))){
//            String notification_content = getIntent().getStringExtra("content");
//            Log.i("新的系统消息", notification_content);
//            if(!TextUtils.isEmpty(notification_content)) {
//                Gson gson = new Gson();
//                SystemMessage systemMessage = gson.fromJson(notification_content, SystemMessage.class);
//                ContentValues values = new ContentValues();
//                values.put("apply_date", systemMessage.getApply_date());
//                values.put("type_id", systemMessage.getType_id());
//                values.put("name", systemMessage.getName());
//                values.put("is_read", systemMessage.is_read());
//                values.put("result", systemMessage.getResult());
//                values.put("meeting_date", systemMessage.getMeeting_date());
//                values.put("reason", systemMessage.getReason());
//                db.insert("sysmsg", null, values);
//            }
////            parseContent(notification_content);
//        }
        setTitle("系统消息");
        setBackVisibility(View.VISIBLE);
        getMessageList();
        for (SystemMessage systemMessage : messageList){
            Log.i("消息数据。。。", systemMessage.toString());
        }
        if(msgAdapter == null) {
            msgAdapter = new SystemMsgAdapter();
            lv_system_msg.setAdapter(msgAdapter);
        }else {
            msgAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取消息列表
     */
    private void getMessageList() {
        Cursor cursor = db.query("sysmsg", null, null, null, null, null, null);
        while (cursor.moveToNext()){
            SystemMessage systemMessage = new SystemMessage();
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String apply_date = cursor.getString(cursor.getColumnIndex("apply_date"));
            int type_id = cursor.getInt(cursor.getColumnIndex("type_id"));
            String is_read = cursor.getString(cursor.getColumnIndex("is_read"));
            String result = cursor.getString(cursor.getColumnIndex("result"));
            String meeting_date = cursor.getString(cursor.getColumnIndex("meeting_date"));
            String reason = cursor.getString(cursor.getColumnIndex("reason"));
            systemMessage.setReason(reason);
            systemMessage.setName(name);
            systemMessage.setApply_date(apply_date);
            systemMessage.setType_id(type_id);
            systemMessage.setIs_read(Boolean.parseBoolean(is_read));
            systemMessage.setResult(result);
            systemMessage.setMeeting_date(meeting_date);
            if(!messageList.contains(systemMessage)) {
                messageList.add(systemMessage);
            }
        }
        cursor.close();
        if(msgAdapter == null) {
            msgAdapter = new SystemMsgAdapter();
            lv_system_msg.setAdapter(msgAdapter);
        }else {
            msgAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 解析消息内容
     */
    private void parseContent(String content) {
        SystemMessage systemMessage = new SystemMessage();
        try {
            JSONObject jsonObject = new JSONObject(content);
            String apply_date = jsonObject.getString("apply_date");
            String name = jsonObject.getString("name");
            String result = jsonObject.getString("result");
            systemMessage.setApply_date(apply_date);
            systemMessage.setName(name);
            systemMessage.setResult(result);
            systemMessage.setIs_read(jsonObject.getBoolean("is_read"));
            systemMessage.setMeeting_date(jsonObject.getString("meeting_date"));
            systemMessage.setType_id(jsonObject.getInt("type_id"));
            systemMessage.setReason(jsonObject.getString("reason"));
            messageList.add(systemMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(msgAdapter == null) {
            msgAdapter = new SystemMsgAdapter();
            lv_system_msg.setAdapter(msgAdapter);
        }else {
            msgAdapter.notifyDataSetChanged();
        }
    }

    private class SystemMsgAdapter extends BaseAdapter{

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
            if(convertView == null){
                convertView = View.inflate(getApplicationContext(), R.layout.system_msg_item, null);
                holder = new SystemMsgViewHolder();
                holder.tv_system_msg_left = (TextView) convertView.findViewById(R.id.tv_system_msg_left);
                holder.tv_system_msg_right = (TextView) convertView.findViewById(R.id.tv_system_msg_right);
                convertView.setTag(holder);
            }else {
                holder = (SystemMsgViewHolder) convertView.getTag();
            }
            if(messageList.get(position).getType_id() == 1) {// 会见
                if(messageList.get(position).getResult().contains("已通过")){
                    holder.tv_system_msg_left.setText(LEFT_TVS[2]);
                }else {
                    holder.tv_system_msg_left.setText(LEFT_TVS[3]);
                }
            }else if(messageList.get(position).getType_id() == 2){// 探监
                if(messageList.get(position).getResult().contains("已通过")){
                    holder.tv_system_msg_left.setText(LEFT_TVS[0]);
                }else {
                    holder.tv_system_msg_left.setText(LEFT_TVS[1]);
                }
            }else {
                // ToDo
            }
            if(!messageList.get(position).is_read()){
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
                    if(messageList.get(position).getType_id() == 1 || messageList.get(position).getType_id() == 2){
                        messageList.get(position).setIs_read(true);
                        ContentValues values = new ContentValues();
                        values.put("is_read", "true");
                        db.update("sysmsg", values, " apply_date = ? and meeting_date = ? ", new String[]{messageList.get(position).getApply_date(), messageList.get(position).getMeeting_date()});
//                        db.close();
                        intent = new Intent(SystemMessageActivity.this, ApplyResultActivity.class);
                        intent.putExtra("type_id", messageList.get(position).getType_id());
                        intent.putExtra("result", messageList.get(position).getResult());
                        intent.putExtra("apply_date", messageList.get(position).getApply_date());
                        intent.putExtra("meeting_date", messageList.get(position).getMeeting_date());
                        intent.putExtra("name", messageList.get(position).getName());
                        startActivity(intent);
                    }else {

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
