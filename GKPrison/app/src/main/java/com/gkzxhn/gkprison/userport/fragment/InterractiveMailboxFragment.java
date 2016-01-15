package com.gkzxhn.gkprison.userport.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Reply;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * 监狱长信箱 --> 互动信箱
 */
public class InterractiveMailboxFragment extends Fragment {
    private String url = "";
    private SharedPreferences sp;
    private int family_id = 0;
    private String token = "";
    private List<Reply> replies = new ArrayList<Reply>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String message = (String)msg.obj;
                    if (message.equals("error")){
                        Toast.makeText(getActivity(), "同步数据失败", Toast.LENGTH_SHORT).show();
                    }else if (message.equals("success")){
                        Bundle bundle = msg.getData();
                        String result = bundle.getString("result");
                        replies = analysisReply(result);
                    }
                    break;
            }
        }
    };

    private ExpandableListView elv_my_mailbox_list;
    private List<String> my_mailbox_list_title = new ArrayList<String>(){
        {
            add("回复：关于监狱用餐问题的建议");
            add("关于住宿问题的建议");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interractive_mailbox,null);
        elv_my_mailbox_list = (ExpandableListView) view.findViewById(R.id.elv_my_mailbox_list);
        initData();
        return view;
    }

    private void initData(){
        sp = getActivity().getSharedPreferences("config", getActivity().MODE_PRIVATE);
        family_id = sp.getInt("family_id", 1);
        token = sp.getString("token", "");
        url = Constants.URL_HEAD+"comments?access_token="+token+"&family_id="+family_id;
        getReply();
    }



    private void getReply(){
        new Thread(){
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet get = new HttpGet(url);
                try {
                    HttpResponse response = httpClient.execute(get);
                    if (response.getStatusLine().getStatusCode()==200){
                        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                        msg.obj = "success";
                        Bundle bundle = new Bundle();
                        bundle.putString("result",result);
                        msg.setData(bundle);
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }else {
                        msg.obj = "error";
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private List<Reply> analysisReply(String s){
        List<Reply> replies = new ArrayList<Reply>();
        try {
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0;i < jsonArray.length();i++){
                Reply reply = new Reply();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                reply.setTitle(jsonObject.getString("title"));
                reply.setContents(jsonObject.getString("contents"));
                reply.setReplies(jsonObject.getString("replies"));
                replies.add(reply);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return replies;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        elv_my_mailbox_list.setAdapter(new MyAdapter());
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return replies.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(getActivity(), R.layout.prison_warden_item, null);
                holder = new GroupViewHolder();
                holder.tv_reply_item_title = (TextView) convertView.findViewById(R.id.tv_reply_item_title);
                holder.iv_reply_item = (ImageView) convertView.findViewById(R.id.iv_reply_item);
                convertView.setTag(holder);
            }else {
                holder = (GroupViewHolder) convertView.getTag();
            }
            holder.tv_reply_item_title.setText(replies.get(groupPosition).getTitle());
            if (isExpanded){
                holder.iv_reply_item.setImageResource(R.drawable.up_gray);
            }else {
                holder.iv_reply_item.setImageResource(R.drawable.down_gray);
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(getActivity(), R.layout.interactive_mailbox_child, null);
                holder = new ChildViewHolder();
                holder.tv_reply_content = (TextView) convertView.findViewById(R.id.tv_reply_content);
                holder.tv_warden_signature = (TextView) convertView.findViewById(R.id.tv_warden_signature);
                holder.tv_message_time = (TextView) convertView.findViewById(R.id.tv_message_time);
                convertView.setTag(holder);
            }else {
                holder = (ChildViewHolder) convertView.getTag();
            }
            holder.tv_reply_content.setText("\b\b\b\b\b\b\b\b"+replies.get(childPosition).getContents());
            holder.tv_message_time.setText(replies.get(childPosition).getReplies());
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private static class GroupViewHolder{
        TextView tv_reply_item_title;
        ImageView iv_reply_item;
    }

    private static class ChildViewHolder{
        TextView tv_reply_content;
        TextView tv_warden_signature;
        TextView tv_message_time;
    }
}
